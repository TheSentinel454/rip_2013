package com.planner;

import com.badlogic.gdx.math.Vector2;
import com.rip.javasteroid.GameData;
import com.rip.javasteroid.remote.EntityData;
import com.rip.javasteroid.remote.QueryInterface;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created with IntelliJ IDEA.
 * User: Luke
 * Date: 11/30/13
 * Time: 11:59 AM
 */
public class PlanExecutor extends Thread
{
	/* Private Attributes */
	private boolean m_Alive = true;
	private QueryInterface m_Server;
	private ConcurrentLinkedQueue<PlanAction> m_Plan;

  private static final float SAFE_DISTANCE =  600.0f;
	private static final float SAFETY_FACTOR = 0.05f;
	private static Vector2 goal;

	/**
	 * Plan Executor Constructor
	 */
	public PlanExecutor(QueryInterface server)
	{
		// Initialize the plan queue
		m_Plan = new ConcurrentLinkedQueue<PlanAction>();
		// Save the server
		m_Server = server;
	}

	/**
	 * Execute the plan
	 */
	@Override
	public void run()
	{
		// Keep running while alive
		while(m_Alive)
		{
			try
			{
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
                
					// See if we need to execute any plan actions
					while(m_Plan.peek() != null && m_Plan.peek().getTime() < System.currentTimeMillis())
					{
						// Execute the action
						switch (m_Plan.poll().getAction())
						{
							case startForward:
								m_Server.startForward();
								break;
							case stopForward:
								m_Server.stopForward();
								break;
							case startRight:
								m_Server.startRight();
								break;
							case stopRight:
								m_Server.stopRight();
								break;
							case startLeft:
								m_Server.startLeft();
								break;
							case stopLeft:
								m_Server.stopLeft();
								break;
							case fire:
								m_Server.fire();
								break;
						}
					}

					// Sleep for a millisecond
					Thread.sleep(1);
			}
			catch(Exception e)
			{
				System.out.println("PlanExecutor.run(): " + e.getMessage());
			}
		}
		System.out.println("Plan executor exiting!");
	}

	/**
	 * Kill the plan executor
	 */
	public void kill()
	{
		m_Alive = false;
	}

	/**
	 * Set the plan for the executor
	 * @param plan - Plan to be set
	 */
	public void setPlan(ArrayList<PlanAction> plan)
	{
		// Clear the current queue
		m_Plan.clear();
		// Add to the plan till the sizes match
		while(m_Plan.size() != plan.size())
		{
			PlanAction earliestAction = null;
			// Go through the plan and insert into the plan
			for(PlanAction action: plan)
			{
				// Find the lowest time action
				if (earliestAction == null || earliestAction.getTime() > action.getTime())
					earliestAction = action;
			}
			// Add the action to the plan
			m_Plan.add(earliestAction);
		}
	}

	/**
	 * Set the plan for the executor
	 * @param plan - Plan to be set
	 */
	public void setPlan(ConcurrentLinkedQueue<PlanAction> plan)
	{
		// Clear the current queue
		m_Plan.clear();
		// Set the plan
		m_Plan = plan;
	}
}
