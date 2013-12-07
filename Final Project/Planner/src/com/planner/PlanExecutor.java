package com.planner;

import com.badlogic.gdx.math.Vector2;
import com.rip.javasteroid.GameData;
import com.rip.javasteroid.entity.Ship;
import com.rip.javasteroid.remote.EntityData;
import com.rip.javasteroid.remote.QueryInterface;
import java.util.ArrayList;
import java.util.Collections;
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

	private ConcurrentLinkedQueue<PlanAction> getPlan()
	{
		synchronized (m_Plan)
		{
			return m_Plan;
		}
	}

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
				while(getPlan().peek() != null && getPlan().peek().getTime() < System.currentTimeMillis())
				{
					// Execute the action
					switch (getPlan().poll().getAction())
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
		synchronized (m_Plan)
		{
			// Clear the current queue
			m_Plan.clear();
			// Sort it
			Collections.sort(plan);
			// Add to the queue
			m_Plan.addAll(plan);
		}
	}

	/**
	 * Set the plan for the executor
	 * @param plan - Plan to be set
	 */
	public void setPlan(ConcurrentLinkedQueue<PlanAction> plan)
	{
		synchronized (m_Plan)
		{
			// Clear the current queue
			m_Plan.clear();
			// Set the plan
			m_Plan = plan;
		}
	}
}
