import com.planner.PlanExecutor;
import com.rip.javasteroid.GameData;
import com.rip.javasteroid.entity.BaseEntity;
import com.rip.javasteroid.remote.QueryInterface;
import com.rip.javasteroid.remote.RmiServer;

import java.rmi.Naming;
import java.util.ArrayList;

public class Main
{
	private static QueryInterface m_Server;
	private static PlanExecutor m_Executor;
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
		}
		catch(Exception e)
		{
			System.out.println("Main.main(): " + e.getMessage());
		}

		GameData data = m_Server.getGameData();
		EntityData ship = data.getShipData();
		ArrayList<EntityData> asteroids = data.getAsteroidData();

		Vector2 shippos = ship.getPosition();

		//Calculate delta-V curve
		int granularity = 1000;
		float[] delta_theta = new float[2*granularity];
		Vector2[] deltaV = new Vector2[2*granularity];
		float delta_t = Math.pi / Ship.SHIP_ANGULAR_VELOCITY + 0.25f * Ship.SHIP_MAX_VELOCITY / Ship.SHIP_LINEAR_ACCELERATION;

		for(int ndx = 0; ndx < deltaV.length ; ndx++) {
			if(ndx < granularity) {
				delta_theta[ndx] = ndx * Math.pi / granularity;
				deltaV[ndx] = new Vector2(1f,0f);
				deltaV[ndx].setAngle(delta_theta[ndx]);
				deltaV[ndx].scl(Ship.SHIP_LINEAR_ACCELERATION * (delta_t - delta_theta[ndx] / Ship.SHIP_ANGULAR_VELOCITY));
			} else {
				delta_theta[ndx] = (ndx-granularity) * -1 * Math.pi / granularity;
				deltaV[ndx] = new Vector2(1f,0f);
				deltaV[ndx].setAngle(delta_theta[ndx]);
				deltaV[ndx].scl(Ship.SHIP_LINEAR_ACCELERATION * (delta_t + delta_theta[ndx] / Ship.SHIP_ANGULAR_VELOCITY));
			}
		}

		for(EntityData asteroid : asteroids) {

			Vector2 astpos = asteroid.getPosition();
			Vector2 relpos = new Vector2(astpos);
			relpos.sub(shippos);

			float config_radius = ship.getRadius() * (1.0f + SAFETY_FACTOR) + asteroid.getRadius();
			Vector2[] occ_point = new Vector2[2];
			Vector2 relv = null;

			Vector2[] reachableV = new Vector2[deltaV.length];
			float[] theta = new float[deltaV.length];

			if(relpos.len() - config_radius < SAFE_DISTANCE) {
				//Calculate occlusion points
				occ_point[0] = new Vector2(1f,0f);
				occ_point[0].rotate((float)(-1*Math.asin(config_radius/relpos.len())*180/Math.PI));
				occ_point[0].scl((float)(Math.sqrt(relpos.len() * relpos.len() - config_radius * config_radius)));
				occ_point[1] = new Vector2(occ_point[0]);
				occ_point[1].rotate((float)(2*Math.asin(config_radius/relpos.len())*180/Math.PI));

				//Calculate relative velocity
				relv = new Vector2(asteroid.getVelocity());
				relv.sub(ship.getVelocity());

				//Add delta-V curve to relative velocity
				for(int ndx = 0; ndx < deltaV.length; ndx++) {
				reachableV[ndx] = deltaV[ndx].cpy().rotate(ship.getAngle()).add(relv);
				theta[ndx] = reachableV[ndx].getAngle();
				}

				//Determine excluded headings from delta-V ellipse based on occlusion points of velocity obstacle
				//Excluded headings come from intersection of occlusion point rays with curve - rays are theta = constant
				//Find thetas on delta-V curve bracketing ray theta, linearly interpolate to find intersection vector
				//Up to 2 intersections per ray (0-4 per obstacle)
				//To determine headings, translate vector back to origin (subtract relv), rotate back to x-axis (rotate -ship.angle)
				//use getAngle to get headings of intersection points

				//Store excluded headings

				//Future improvement - use area of delta-V curve and calculate intersection with exclusion cones
				//rather than simplistic heading exclusion
			}
		}

		//Combine exclusion headings to determine allowable headings

		//Select heading closest to line to goal					

		//Determine combination of turning and thrusting to achieve that heading
		
		try
		{
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
}
