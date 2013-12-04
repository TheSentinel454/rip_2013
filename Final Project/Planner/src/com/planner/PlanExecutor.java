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
