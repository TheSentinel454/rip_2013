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
