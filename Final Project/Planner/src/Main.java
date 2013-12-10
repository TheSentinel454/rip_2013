import com.badlogic.gdx.math.Vector2;
import com.planner.*;
import com.planner.machinelearning.DecisionTree;
import com.rip.javasteroid.GameData;
import com.rip.javasteroid.entity.Ship;
import com.rip.javasteroid.remote.EntityData;
import com.rip.javasteroid.remote.QueryInterface;

import java.rmi.Naming;
import java.util.ArrayList;
import java.util.Collections;

public class Main
{
	/* Constants */
	private static final int GAMES_TO_PLAY = 10;

	/* Private Attributes */
	private static QueryInterface m_Server;
	private static PlanExecutor m_Executor;
	private static GameData m_GameData;
	private static ArrayList<PlanAction> m_Plan;
	private static ArrayList<Metrics> m_Metrics;
	private static DecisionTree m_DecisionTree;

	/**
	 * Main entry point for the application
	 *
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

			// Initialize the decision tree for the machine learning
			m_DecisionTree = new DecisionTree();
			m_DecisionTree.printTree();
			m_DecisionTree.train(true, 0.0f, 0.0f, 0.0f, true);
			m_DecisionTree.train(true, 1.0f, 0.0f, 0.0f, true);
			m_DecisionTree.train(true, 4.0f, 0.0f, 0.0f, false);
			m_DecisionTree.train(true, 3.0f, 0.0f, 0.0f, false);
			m_DecisionTree.printTree();

			m_Plan = new ArrayList<PlanAction>();
			m_Metrics = new ArrayList<Metrics>();

			int iGameCount = 1;
			PreviewPlanner planner = new PreviewPlanner(1, 0.3f);
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
						{
							// We are done planning
							break;
						}
					}
					// Execute the planner and set the plan
					EntityData ship = m_GameData.getShipData();
					ArrayList<EntityData> asteroids = m_GameData.getAsteroidData();

					m_Plan = planner.determinePlan(ship, asteroids, m_GameData);
					m_Executor.setPlan(m_Plan);
					Thread.sleep(200);
				}
				catch (Throwable e)
				{
					System.out.println("Main Loop(): " + e.getMessage());
				}
			}
			while (true);

			// Kill the plan executor
			m_Executor.kill();
			// Wait for the thread (5 seconds)
			m_Executor.join(5000);
		}
		catch (Throwable e)
		{
			System.out.println("Main.main(): " + e.getMessage());
		}
	}

}
