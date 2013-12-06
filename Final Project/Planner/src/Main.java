import com.badlogic.gdx.math.Vector2;
import com.planner.PlanAction;
import com.planner.PlanExecutor;
import com.rip.javasteroid.GameData;
import com.rip.javasteroid.entity.BaseEntity;
import com.rip.javasteroid.entity.Ship;
import com.rip.javasteroid.remote.EntityData;
import com.rip.javasteroid.remote.QueryInterface;
import com.rip.javasteroid.remote.RmiServer;

import java.rmi.Naming;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Main
{
	/* Constants */
	private static final int GAMES_TO_PLAY = 2000;
	private static final float SAFE_DISTANCE =  600.0f;
	private static final float SAFETY_FACTOR = 0.05f;

	/* Private Attributes */
	private static QueryInterface	m_Server;
	private static PlanExecutor		m_Executor;
	private static GameData			m_GameData;
	private static Vector2			m_Goal;

	/**
	 * Main entry point for the application
	 * @param args - Planner Arguments
	 */
	public static void main(String[] args)
	{
		try
		{
			// Connect to the Asteroids server via RMI
			m_Server = (QueryInterface) Naming.lookup("rmi://localhost/Query");
			// Initialize the plan executor
			m_Executor = new PlanExecutor(m_Server);
			// Start the plan executor
			m_Executor.start();

			int iGameCount = 0;
			do
			{
				try
				{
					// Update with the latest game data
					m_GameData = m_Server.getGameData();
					// Check to see if the game is over
					if (m_GameData.isGameOver())
					{
						// TODO: Save out any relevant machine learning data
						// TODO: Reset the planner
						// TODO: Save out results (Score, etc.)
						// Reset the game
						m_Server.reset();
						// Refresh the game data
						m_GameData = m_Server.getGameData();
						// Increment counter to know we just did another round
						iGameCount++;
						// Check counter to see if we have planned enough
						if (iGameCount > GAMES_TO_PLAY)
							// We are done planning
							break;
					}
					// Execute the planner and set the plan
					m_Executor.setPlan(determinePlan());
				}
				catch(Exception e)
				{
					System.out.println("Main Loop(): " + e.getMessage());
				}
			}
			while(true);

			// Kill the plan executor
			m_Executor.kill();
			// Wait for the thread (5 seconds)
			m_Executor.join(5000);
		}
		catch(Exception e)
		{
			System.out.println("Main.main(): " + e.getMessage());
		}
	}

	/**
	 * Execute the planner to determine the best plan
	 * @return Queue of planned actions
	 */
	private static ConcurrentLinkedQueue<PlanAction> determinePlan()
	{
		ConcurrentLinkedQueue<PlanAction> plan = new ConcurrentLinkedQueue<PlanAction>();
		try
		{
			EntityData ship = m_GameData.getShipData();
			ArrayList<EntityData> asteroids = m_GameData.getAsteroidData();

			Vector2 shippos = ship.getPosition();

			//Calculate delta-V curve
			int granularity = 1000; //per semicircle
			float[] delta_theta = new float[2*granularity+1];
			Vector2[] deltaV = new Vector2[delta_theta.length];

            //Determine time step to examine - currently time required to do 180 degree turn and accelerate to 25% of max velocity
			float delta_t = (float)(Math.PI / Ship.SHIP_ANGULAR_VELOCITY + 0.25f * Ship.SHIP_MAX_LINEAR_VELOCITY / Ship.SHIP_LINEAR_ACCELERATION);

			for(int ndx = 0; ndx < deltaV.length ; ndx++)
			{
                delta_theta[ndx] = (360.0f * ndx)/(2*granularity);
                deltaV[ndx] = new Vector2(1f,0f);
                deltaV[ndx].rotate(delta_theta[ndx]);
                deltaV[ndx].scl(Ship.SHIP_LINEAR_ACCELERATION * (delta_t - (float)Math.toRadians(delta_theta[ndx] < 180 ? delta_theta[ndx] : Math.abs(delta_theta[ndx]-360.0f)) / Ship.SHIP_ANGULAR_VELOCITY));
            }

			for(EntityData asteroid : asteroids)
			{
				Vector2 astpos = asteroid.getPosition();
				Vector2 relpos = new Vector2(astpos);
				relpos.sub(shippos);

				float config_radius = ship.getRadius() * (1.0f + SAFETY_FACTOR) + asteroid.getRadius();
				Vector2[] occ_point = new Vector2[2];
				Vector2 relv;

				Vector2[] reachableV = new Vector2[deltaV.length];
				float[] theta = new float[deltaV.length];

				if(relpos.len() - config_radius < SAFE_DISTANCE)
				{
					//Calculate occlusion points
					occ_point[0] = new Vector2(1f,0f);
					occ_point[0].rotate((float)Math.toDegrees(-1*Math.asin(config_radius/relpos.len())));
					occ_point[0].scl((float)(Math.sqrt(relpos.len() * relpos.len() - config_radius * config_radius)));
					occ_point[1] = new Vector2(occ_point[0]);
					occ_point[1].rotate((float)(Math.toDegrees(2*Math.asin(config_radius/relpos.len()))));

					//Calculate relative velocity
					relv = new Vector2(asteroid.getVelocity());
					relv.sub(ship.getVelocity());

					//Add delta-V curve to relative velocity
					for(int ndx = 0; ndx < deltaV.length; ndx++)
					{
						reachableV[ndx] = deltaV[ndx].cpy().rotate(ship.getAngle()).add(relv);
						theta[ndx] = reachableV[ndx].angle();
					}

					//Determine excluded headings from delta-V curve based on occlusion points of velocity obstacle
					//Excluded headings come from intersection of occlusion point rays with curve - rays are theta = constant
					//Find thetas on delta-V curve bracketing ray theta, interval bisection to find intersection vector
					//Up to 4 intersections per ray (0-8 per obstacle)

                    ArrayList<Float> occ_intersect = new ArrayList<Float>();

                    float[][] theta_diff = new float[2][theta.length];

                    // Occlusion point boundaries are rays of constant theta
                    for(int ndx = 0; ndx < theta.length; ndx++) {
                        for(int occ_ndx = 0; occ_ndx < 2; occ_ndx++) {
                            theta_diff[occ_ndx][ndx] = theta[ndx] - occ_point[occ_ndx].angle();
                        }
                    }

                    // Locate crossings by finding sign changes of theta - occlusion_theta
                    float prev;
                    for(int occ_ndx = 0; occ_ndx < 2; occ_ndx++) {
                        prev = Math.signum(theta_diff[occ_ndx][0]);
                        for(int ndx = 0; ndx < theta_diff.length; ndx++) {
                            if(theta_diff[occ_ndx][ndx] == 0) {
                                occ_intersect.add(new Float(delta_theta[ndx]));
                            } else if(Math.signum(theta_diff[occ_ndx][ndx]) != prev) {
                                //TODO: Convert to zero-finding with interval bisection
                                float interp = (delta_theta[ndx] - delta_theta[ndx-1]) / (theta_diff[occ_ndx][ndx] - theta_diff[occ_ndx][ndx-1]) * theta_diff[occ_ndx][ndx] + delta_theta[ndx];
                                occ_intersect.add(new Float(interp));
                            }
                            prev = Math.signum(theta_diff[occ_ndx][ndx]);
                        }
                    }

                    //March around delta_theta, at each occlusion heading check if it starts or ends an exclusion zone
                    //Store as bitmask - 0 = not safe, 1 = safe
                    Collections.sort(occ_intersect);
                    if(occ_intersect.get(occ_intersect.size()-1) == 360.0f) {
                        occ_intersect.remove(occ_intersect.size()-1);
                    }
                    boolean[] safe = new boolean[occ_intersect.size()+1];

                    float inc_angle = occ_point[1].angle() - occ_point[0].angle() + ((occ_point[1].angle() - occ_point[0].angle() >= 0) ? (0) : (360.0f));

                    for(int ndx = 0; ndx < safe.length; ndx++) {
                        float ang = 0.5f * (((ndx == 0) ? (0.0f) : (occ_intersect.get(ndx))) + ((ndx+1 == safe.length) ? (360.0f) : (occ_intersect.get(ndx+1))));

                        Vector2 dv = new Vector2(1f,0f);
                        dv.rotate(ang);
                        dv.scl(Ship.SHIP_LINEAR_ACCELERATION * (delta_t - (float)Math.toRadians(ang < 180 ? ang : Math.abs(ang-360.0f)) / Ship.SHIP_ANGULAR_VELOCITY));
                        dv.rotate(ship.getAngle()).add(relv);

                        ang = occ_point[1].angle() - dv.angle() + ((occ_point[1].angle() - dv.angle() >= 0) ? (0) : (360.0f));
                        ang += dv.angle() - occ_point[1].angle() + ((dv.angle() - occ_point[1].angle() >= 0) ? (0) : (360.0f));

                        //Safe if sum of angles is greater than angle between occlusion points
                        safe[ndx] = ang > inc_angle;
                    }

					//Store excluded headings

					//Future improvement - use area of delta-V curve and calculate intersection with exclusion cones
					//rather than simplistic heading exclusion
				}
			}

			//Combine exclusion headings to determine allowable headings

			//Select heading closest to line to goal

			//Determine combination of turning and thrusting to achieve that heading

		}
		catch(Throwable t)
		{
			System.out.println("DeterminePlan(): " + t.getMessage());
		}
		// Return the plan
		return plan;
	}
}
