import com.badlogic.gdx.math.Vector2;
import com.planner.*;
import com.planner.machinelearning.DecisionTree;
import com.planner.machinelearning.LeafData;
import com.planner.machinelearning.SearchCriteria;
import com.planner.machinelearning.TrainingData;
import com.rip.javasteroid.GameData;
import com.rip.javasteroid.entity.Bullet;
import com.rip.javasteroid.entity.Ship;
import com.rip.javasteroid.remote.EntityData;
import com.rip.javasteroid.remote.QueryInterface;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;

public class Main
{
	/* Constants */
	private static final int    GAMES_TO_PLAY = 100;
	private static float        SAFE_DISTANCE;
	private static float        SAFETY_FACTOR;
	private static float        DELTA_T;
	private static float        HEADING_RANGE;
	private static float        WRAP_MARGIN = 25.0f;
	private static boolean      KILL_GAME = true;

	/* Private Attributes */
	private static QueryInterface           m_Server;
	private static PlanExecutor             m_Executor;
	private static GameData                 m_GameData;
	private static ArrayList<Metrics>       m_Metrics;
	private static DecisionTree             m_DecisionTree;
	private static ArrayList<TrainingData>  m_TrainingData;

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

			// Initialize the decision tree for the machine learning
			m_DecisionTree = new DecisionTree();
			m_DecisionTree.printTree();

			m_Metrics = new ArrayList<Metrics>();
			m_TrainingData = new ArrayList<TrainingData>();

			int iGameCount = 1;
			do
			{
				try
				{
					// Update with the latest game data
					m_GameData = m_Server.getGameData();
					// Check to see if the game is over
					if (m_GameData.isGameOver())
					{
						// Quickly resolve all pending training data to be unsuccessful
						trainDecisionTree(true, (m_Metrics.size() == 0 ? new Metrics(System.currentTimeMillis()) : m_Metrics.get(m_Metrics.size() - 1)));
						// Save out the decision tree data
						m_DecisionTree.saveTree();
						m_DecisionTree.saveCsvTree(m_GameData, iGameCount);
						// Reset the game
						m_Server.reset();
						// Wait till the game is reset
						while(m_Server.getGameData().isGameOver())
							Thread.sleep(1);
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
					m_Executor.setPlan(determinePlan());
					Thread.sleep(1);
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
			// Kill the Game
			m_Server.quit();
		}
		catch (Throwable e)
		{
			System.out.println("Main.main(): " + e.getMessage());
		}
		finally
		{
			if (m_Server != null)
			{
				try
				{
					if (KILL_GAME)
						// Kill the Game
						m_Server.quit();
				}
				catch (RemoteException e)
				{}
			}
		}
	}

	/**
	 * Execute the planner to determine the best plan
	 *
	 * @return Queue of planned actions
	 */
	private static ArrayList<PlanAction> determinePlan()
	{
		ArrayList<PlanAction> plan = new ArrayList<PlanAction>();
		try
		{
			long pullTime = System.currentTimeMillis();
			EntityData ship = m_GameData.getShipData();
			ArrayList<EntityData> asteroids = m_GameData.getAsteroidData();

			wrapScreen(asteroids);

			ExclusionZones exclusions;

			SAFETY_FACTOR = 0.20f;
			SAFE_DISTANCE = 20.0f * Ship.SHIP_MAX_LINEAR_VELOCITY;
			HEADING_RANGE = 5.0f;

			Metrics new_met = new Metrics(pullTime);

			// Evaluate the Decision tree to see if we need to add any extra actions
			ArrayList<Float> asteroidsToDestroy = evaluateDecisionTree();
			for(Float asteroidAngle: asteroidsToDestroy)
				plan.add(new PlanAction(System.currentTimeMillis(), PlanAction.Action.fire));

			float target_h;
			//Safe distance gets decreased each time search fails to find target heading
			do
			{
				SAFE_DISTANCE = SAFE_DISTANCE * 0.50f;

				//Calculate safe/unsafe headings
				exclusions = calculateExclusions(ship, asteroids, new_met);

				//Select heading
				target_h = selectHeading(ship, asteroids, exclusions, asteroidsToDestroy);

			} while (target_h < 0.0f && SAFE_DISTANCE > ship.getRadius() * (1.0f + SAFETY_FACTOR));
			//Don't let safe distance become arbitrarily small

			//Do nothing if no safe heading found - going to crash anyway
			if (target_h >= 0.0f)
			{
				//Determine combination of turning and thrusting to achieve that heading
				calculatePlan(plan, ship, asteroids, exclusions, target_h, pullTime, new_met);

				m_Metrics.add(new_met);

				//Shift plan times to account for execution time
				long planDiff = System.currentTimeMillis() - pullTime;
				for (PlanAction action : plan)
				{
					action.shiftTime(planDiff);
				}
			}

			// Train the Decision Tree
			trainDecisionTree(false, new_met);
		}
		catch (Throwable t)
		{
			System.out.println("DeterminePlan(): " + t.getMessage());
		}
		// Return the plan
		return plan;
	}

	/**
	 * Evaluate the Decision Tree
	 * @return The IDs of the Asteroids that we should shoot at
	 */
	private static ArrayList<Float> evaluateDecisionTree()
	{
		ArrayList<Float> asteroidAngles = new ArrayList<Float>();
		try
		{
			synchronized (m_TrainingData)
			{
				// See if we have any metrics
				if (m_Metrics.size() > 0)
				{
					// Get the latest metric
					Metrics metric = m_Metrics.get(m_Metrics.size() - 1);
					// Go through each of the metrics for each asteroid
					for(AsteroidData asteroidData: metric.getAsteroidMetrics())
					{
						// Check the decision tree for this asteroid metric
						SearchCriteria criteria = new SearchCriteria(asteroidData.isTransited(), asteroidData.getDistance(), asteroidData.getImpactTime() / 1000.0f, asteroidData.getAngle());
						// Get the prediction
						boolean prediction = ((LeafData)m_DecisionTree.find(criteria).getUserObject()).getPrediction();
						// See if we predicted that we need to fire
						if (prediction)
							// Add asteroid to list
							asteroidAngles.add(asteroidData.getAngle());
						// Add Training Data to list so that it can be evaluated later
						TrainingData trainData = new TrainingData(System.currentTimeMillis(), criteria, metric.getPercent_safe(), prediction, 0, m_GameData.getScore(), m_GameData.getLives());
						m_TrainingData.add(trainData);
					}
				}
				// Sort the training data (by time)
				Collections.sort(m_TrainingData);
				// Sort the angles
				Collections.sort(asteroidAngles);
			}
		}
		catch (Exception e)
		{
			System.out.println("Main.evaluateDecisionTree(): " + e.getMessage());
		}
		return asteroidAngles;
	}

	/**
	 * Train the decision tree if there are any previous actions that need to be evaluated
	 */
	private static void trainDecisionTree(boolean gameOver, Metrics metrics)
	{
		try
		{
			synchronized (m_TrainingData)
			{
				boolean success;
				// Go through the list of training data
				for(int i = m_TrainingData.size() - 1; i >= 0; i--)
				{
					TrainingData data = m_TrainingData.get(i);
					// Is this training data up for evaluation (If the game is over, they all are)
					if (gameOver || data.isTimeUp())
					{
						// Let's determine the "Success" of this training
						success = ((data.getScore() > m_GameData.getScore() && data.getLives() >= m_GameData.getLives()) || data.getSafePercentage() <= metrics.getPercent_safe());
						// Let's look up the node, and train it based on the outcome
						m_DecisionTree.train(data.getSearchCriteria(), success);
						// Now lets remove the training data
						m_TrainingData.remove(i);
					}
				}
			}
		}
		catch (Exception e)
		{
			System.out.println("Main.trainDecisionTree(): " + e.getMessage());
		}
	}

	private static void wrapScreen(ArrayList<EntityData> asteroids)
	{
		ArrayList<EntityData> wrapped_asteroids = new ArrayList<EntityData>();

		for (EntityData ast : asteroids)
		{
			float world_width = m_GameData.getWidth() + 2 * ast.getRadius();
			float world_height = m_GameData.getHeight() + 2 * ast.getRadius();

			boolean wrap_right = ast.getPosition().x > (m_GameData.getWidth() - WRAP_MARGIN);
			boolean wrap_left = ast.getPosition().x < WRAP_MARGIN;
			boolean wrap_up = ast.getPosition().y > (m_GameData.getHeight() - WRAP_MARGIN);
			boolean wrap_down = ast.getPosition().y < WRAP_MARGIN;

			Vector2 newPos;
			if (wrap_right)
			{
				newPos = new Vector2(ast.getPosition());
				newPos.sub(world_width, 0.0f);
				wrapped_asteroids.add(new EntityData(new Vector2(newPos), new Vector2(ast.getVelocity()), ast.getAngle(), ast.getRadius()));

				if (wrap_up)
				{
					newPos.sub(0.0f, world_height);
					wrapped_asteroids.add(new EntityData(new Vector2(newPos), new Vector2(ast.getVelocity()), ast.getAngle(), ast.getRadius()));
				} else if (wrap_down)
				{
					newPos.add(0.0f, world_height);
					wrapped_asteroids.add(new EntityData(new Vector2(newPos), new Vector2(ast.getVelocity()), ast.getAngle(), ast.getRadius()));
				}
			} else if (wrap_left)
			{
				newPos = new Vector2(ast.getPosition());
				newPos.add(world_width, 0.0f);
				wrapped_asteroids.add(new EntityData(new Vector2(newPos), new Vector2(ast.getVelocity()), ast.getAngle(), ast.getRadius()));

				if (wrap_up)
				{
					newPos.sub(0.0f, world_height);
					wrapped_asteroids.add(new EntityData(new Vector2(newPos), new Vector2(ast.getVelocity()), ast.getAngle(), ast.getRadius()));
				} else if (wrap_down)
				{
					newPos.add(0.0f, world_height);
					wrapped_asteroids.add(new EntityData(new Vector2(newPos), new Vector2(ast.getVelocity()), ast.getAngle(), ast.getRadius()));
				}
			}

			if (wrap_up)
			{
				newPos = new Vector2(ast.getPosition());
				newPos.sub(0.0f, world_height);
				wrapped_asteroids.add(new EntityData(new Vector2(newPos), new Vector2(ast.getVelocity()), ast.getAngle(), ast.getRadius()));
			} else if (wrap_down)
			{
				newPos = new Vector2(ast.getPosition());
				newPos.add(0.0f, world_height);
				wrapped_asteroids.add(new EntityData(new Vector2(newPos), new Vector2(ast.getVelocity()), ast.getAngle(), ast.getRadius()));
			}
		}

		asteroids.addAll(wrapped_asteroids);
	}

	private static ExclusionZones calculateExclusions(EntityData ship, ArrayList<EntityData> asteroids, Metrics metrics)
	{
		ExclusionZones exclusions = new ExclusionZones();

		float shipAngle = ship.getAngle();

		//Time step to examine - default to time required to do 180 degree turn and accelerate to 25% of max velocity
		DELTA_T = (float) (Math.PI / Ship.SHIP_ANGULAR_VELOCITY + 0.25f * Ship.SHIP_MAX_LINEAR_VELOCITY / Ship.SHIP_LINEAR_ACCELERATION);

        //Calculate bullet velocity circle
        int granularity = 1000; //per semicircle
        float[] delta_theta = new float[2 * granularity + 1];
        Vector2[] deltaV = new Vector2[delta_theta.length];

        for (int ndx = 0; ndx < deltaV.length; ndx++)
        {
            delta_theta[ndx] = (360.0f * ndx) / (2 * granularity);
            deltaV[ndx] = new Vector2(1f, 0f);
            deltaV[ndx].rotate(delta_theta[ndx]);
            deltaV[ndx].scl(Bullet.BULLET_VELOCITY);
        }

		//Asteroid state metrics
		for (EntityData asteroid : asteroids)
		{
			//Calculate relative position
			Vector2 relpos = new Vector2(asteroid.getPosition());
			relpos.sub(ship.getPosition());

			float config_radius = ship.getRadius() * (1.0f + SAFETY_FACTOR) + asteroid.getRadius();

			//Calculate occlusion points
			float rotate_angle;
			if (config_radius > relpos.len())
			{
				rotate_angle = 90.0f;
			} else
			{
				rotate_angle = (float) Math.toDegrees(Math.asin(config_radius / relpos.len()));
			}

			float[] occ_angle = new float[2];
			occ_angle[0] = relpos.angle() - rotate_angle;
			occ_angle[1] = occ_angle[0] + 2 * rotate_angle;
			for (int ndx = 0; ndx < occ_angle.length; ndx++)
			{
				while (occ_angle[ndx] < 0.0f)
				{
					occ_angle[ndx] += 360.0f;
				}
				while (occ_angle[ndx] >= 360.0f)
				{
					occ_angle[ndx] -= 360.0f;
				}
			}

			//Calculate relative velocity
			Vector2 relv = new Vector2(ship.getVelocity());
			relv.sub(asteroid.getVelocity());

			float angle = relpos.angle() - shipAngle;
			while (angle < 0.0f)
			{
				angle += 360.0f;
			}
			while (angle >= 360.0f)
			{
				angle -= 360.0f;
			}
			float distance = relpos.len();
			float impactTime = Float.MAX_VALUE;
			boolean intersect;

			//is the relative velocity between the exclusion points?
			if (occ_angle[0] < occ_angle[1])
			{
				intersect = relv.angle() >= occ_angle[0] && relv.angle() <= occ_angle[1];
			} else
			{
				intersect = relv.angle() >= occ_angle[0] || relv.angle() <= occ_angle[1];
			}

			if (intersect)
			{
				impactTime = (distance - config_radius) / relv.len();
			}

			//Time step is minimum of default delta-t and smallest time to impact
			DELTA_T = Math.min(impactTime, DELTA_T);

            Vector2[] reachableV = new Vector2[deltaV.length];
            float[] theta = new float[deltaV.length];

            //Add bullet curve to relative velocity
            for (int ndx = 0; ndx < deltaV.length; ndx++)
            {
                reachableV[ndx] = new Vector2(deltaV[ndx]);
                reachableV[ndx].rotate(shipAngle);
                reachableV[ndx].sub(asteroid.getVelocity());
                theta[ndx] = reachableV[ndx].angle();
            }

            //Determine excluded headings from delta-V curve based on occlusion points of velocity obstacle
            //Excluded headings come from intersection of occlusion point rays with curve - rays are theta = constant
            //Find thetas on delta-V curve bracketing ray theta, interval bisection to find intersection vector
            //Up to 4 intersections per ray (0-8 per obstacle)

            ArrayList<Float> occ_intersect = new ArrayList<Float>();

            float[][] theta_diff = new float[2][theta.length];

            // Occlusion point boundaries are rays of constant theta
            for (int ndx = 0; ndx < theta.length; ndx++)
            {
                for (int occ_ndx = 0; occ_ndx < 2; occ_ndx++)
                {
                    theta_diff[occ_ndx][ndx] = theta[ndx] - occ_angle[occ_ndx];
                    // Locate crossings by finding sign changes of theta - occlusion_theta
                    if (theta_diff[occ_ndx][ndx] == 0.0f)
                    {
                        occ_intersect.add(delta_theta[ndx]);
                    } else if (ndx > 0 && theta_diff[occ_ndx][ndx] * theta_diff[occ_ndx][ndx - 1] < 0.0f)
                    {
                        //TODO: Convert to zero-finding with interval bisection
                        float interp = (delta_theta[ndx] - delta_theta[ndx - 1]) / (theta_diff[occ_ndx][ndx] - theta_diff[occ_ndx][ndx - 1]) * theta_diff[occ_ndx][ndx] + delta_theta[ndx];
                        occ_intersect.add(interp);
                    }
                }
            }

            //Future improvement - use area of delta-V curve and calculate intersection with exclusion cones
            //rather than simplistic heading exclusion

            if (occ_intersect.isEmpty())
            {
                occ_intersect.add(360.0f);
            }

            //March around delta_theta, at each occlusion heading check if it starts or ends an exclusion zone
            Collections.sort(occ_intersect);
            if (occ_intersect.get(occ_intersect.size() - 1) != 360.0f)
            {
                occ_intersect.add(360.0f);
            }

            ArrayList<ExcludePoint> new_excludes = new ArrayList<ExcludePoint>();

            float inc_angle = occ_angle[1] - occ_angle[0] + ((occ_angle[1] - occ_angle[0] >= 0)
                    ? (0)
                    : (360.0f));

            for (int ndx = 0; ndx < occ_intersect.size(); ndx++)
            {
                float ang = 0.5f * (((ndx == 0) ? (0.0f) : (occ_intersect.get(ndx - 1))) + occ_intersect.get(ndx));

                Vector2 dv = new Vector2(1f, 0f);
                dv.rotate(ang);
                dv.scl(Bullet.BULLET_VELOCITY);
                dv.rotate(shipAngle);
                dv.sub(asteroid.getVelocity());

                ang = occ_angle[1] - dv.angle() + ((occ_angle[1] - dv.angle() >= 0)
                        ? (0)
                        : (360.0f));
                ang += dv.angle() - occ_angle[0] + ((dv.angle() - occ_angle[0] >= 0)
                        ? (0)
                        : (360.0f));

                //Safe if sum of angles is greater than angle between occlusion points
                new_excludes.add(new ExcludePoint(occ_intersect.get(ndx), ang > inc_angle));
            }

            //Merge new exclusion headings with existing
            ExclusionZones fireZones = new ExclusionZones();
            fireZones.addAll(new_excludes);

			metrics.addAsteroid(distance, angle, impactTime, HEADING_RANGE, fireZones);
		}

		//Calculate delta-V curve
		granularity = 1000; //per semicircle
		delta_theta = new float[2 * granularity + 1];
		deltaV = new Vector2[delta_theta.length];

		for (int ndx = 0; ndx < deltaV.length; ndx++)
		{
			delta_theta[ndx] = (360.0f * ndx) / (2 * granularity);
			deltaV[ndx] = new Vector2(1f, 0f);
			deltaV[ndx].rotate(delta_theta[ndx]);
			float scalar = Ship.SHIP_LINEAR_ACCELERATION * (DELTA_T - (float) Math.toRadians(delta_theta[ndx] < 180
			                                                                                 ? delta_theta[ndx]
			                                                                                 : Math.abs(delta_theta[ndx] - 360.0f)) / Ship.SHIP_ANGULAR_VELOCITY);
			deltaV[ndx].scl(Math.max(0.0f, scalar));
		}

		for (EntityData asteroid : asteroids)
		{
			Vector2 relpos = new Vector2(asteroid.getPosition());
			relpos.sub(ship.getPosition());

			float config_radius = ship.getRadius() * (1.0f + SAFETY_FACTOR) + asteroid.getRadius();
			Vector2[] occ_point = new Vector2[2];
			Vector2 relv;

			Vector2[] reachableV = new Vector2[deltaV.length];
			float[] theta = new float[deltaV.length];

			if (relpos.len() - config_radius < SAFE_DISTANCE)
			{
				//Calculate occlusion points
				float rotate_angle;
				float occ_len;
				if (config_radius > relpos.len())
				{
					rotate_angle = 90.0f;
					occ_len = config_radius;
				} else
				{
					rotate_angle = (float) Math.toDegrees(Math.asin(config_radius / relpos.len()));
					occ_len = (float) (Math.sqrt(relpos.len() * relpos.len() - config_radius * config_radius));
				}
				occ_point[0] = new Vector2(1f, 0f);
				occ_point[0].rotate(relpos.angle() - rotate_angle);
				occ_point[0].scl(occ_len);
				occ_point[1] = new Vector2(occ_point[0]);
				occ_point[1].rotate(2 * rotate_angle);

				//Calculate relative velocity
				relv = new Vector2(ship.getVelocity());
				relv.sub(asteroid.getVelocity());

				//Add delta-V curve to relative velocity
				for (int ndx = 0; ndx < deltaV.length; ndx++)
				{
					reachableV[ndx] = new Vector2(deltaV[ndx]);
					reachableV[ndx].rotate(shipAngle);
					reachableV[ndx].add(ship.getVelocity());
					if (reachableV[ndx].len() > Ship.SHIP_MAX_LINEAR_VELOCITY)
					{
						reachableV[ndx].nor();
						reachableV[ndx].scl(Ship.SHIP_MAX_LINEAR_VELOCITY);
					}
					reachableV[ndx].sub(asteroid.getVelocity());
					theta[ndx] = reachableV[ndx].angle();
				}

				//Determine excluded headings from delta-V curve based on occlusion points of velocity obstacle
				//Excluded headings come from intersection of occlusion point rays with curve - rays are theta = constant
				//Find thetas on delta-V curve bracketing ray theta, interval bisection to find intersection vector
				//Up to 4 intersections per ray (0-8 per obstacle)

				ArrayList<Float> occ_intersect = new ArrayList<Float>();

				float[][] theta_diff = new float[2][theta.length];

				// Occlusion point boundaries are rays of constant theta
				for (int ndx = 0; ndx < theta.length; ndx++)
				{
					for (int occ_ndx = 0; occ_ndx < 2; occ_ndx++)
					{
						theta_diff[occ_ndx][ndx] = theta[ndx] - occ_point[occ_ndx].angle();
						// Locate crossings by finding sign changes of theta - occlusion_theta
						if (theta_diff[occ_ndx][ndx] == 0.0f)
						{
							occ_intersect.add(delta_theta[ndx]);
						} else if (ndx > 0 && theta_diff[occ_ndx][ndx] * theta_diff[occ_ndx][ndx - 1] < 0.0f)
						{
							//TODO: Convert to zero-finding with interval bisection
							float interp = (delta_theta[ndx] - delta_theta[ndx - 1]) / (theta_diff[occ_ndx][ndx] - theta_diff[occ_ndx][ndx - 1]) * theta_diff[occ_ndx][ndx] + delta_theta[ndx];
							occ_intersect.add(interp);
						}
					}
				}

				//Future improvement - use area of delta-V curve and calculate intersection with exclusion cones
				//rather than simplistic heading exclusion

				if (occ_intersect.isEmpty())
				{
					occ_intersect.add(360.0f);
				}

				//March around delta_theta, at each occlusion heading check if it starts or ends an exclusion zone
				Collections.sort(occ_intersect);
				if (occ_intersect.get(occ_intersect.size() - 1) != 360.0f)
				{
					occ_intersect.add(360.0f);
				}

				ArrayList<ExcludePoint> new_excludes = new ArrayList<ExcludePoint>();

				float inc_angle = occ_point[1].angle() - occ_point[0].angle() + ((occ_point[1].angle() - occ_point[0].angle() >= 0)
				                                                                 ? (0)
				                                                                 : (360.0f));

				for (int ndx = 0; ndx < occ_intersect.size(); ndx++)
				{
					float ang = 0.5f * (((ndx == 0) ? (0.0f) : (occ_intersect.get(ndx - 1))) + occ_intersect.get(ndx));

					Vector2 dv = new Vector2(1f, 0f);
					dv.rotate(ang);
					dv.scl(Ship.SHIP_LINEAR_ACCELERATION * (DELTA_T - (float) Math.toRadians(ang < 180
					                                                                         ? ang
					                                                                         : Math.abs(ang - 360.0f)) / Ship.SHIP_ANGULAR_VELOCITY));
					dv.rotate(shipAngle).add(relv);

					ang = occ_point[1].angle() - dv.angle() + ((occ_point[1].angle() - dv.angle() >= 0)
					                                           ? (0)
					                                           : (360.0f));
					ang += dv.angle() - occ_point[0].angle() + ((dv.angle() - occ_point[0].angle() >= 0)
					                                            ? (0)
					                                            : (360.0f));

					//Safe if sum of angles is greater than angle between occlusion points
					new_excludes.add(new ExcludePoint(occ_intersect.get(ndx), ang > inc_angle));
				}

				//Merge new exclusion headings with existing
				exclusions.addAll(new_excludes);

			}
		}

		//Calculate safe percentage of headings
		metrics.setPercent_safe(exclusions.getPercentSafe());

		return exclusions;
	}

	private static float selectHeading(EntityData ship, ArrayList<EntityData> asteroids, ExclusionZones exclusions, ArrayList<Float> asteroidsToDestroy)
	{
		if (asteroidsToDestroy != null && asteroidsToDestroy.size() > 0)
		{
			Float heading = exclusions.checkForSafeFireHeading(asteroidsToDestroy);
			if (heading == null)
				heading = exclusions.findClosestSafeHeading(0.0f);
			return heading;
		}
		else
			return exclusions.findClosestSafeHeading(0.0f);
	}

	private static void calculatePlan(ArrayList<PlanAction> plan, EntityData ship, ArrayList<EntityData> asteroids, ExclusionZones exclusions, float target_h, long start_time, Metrics metrics)
	{
		float turn_angle = (target_h <= 180.0f) ? (target_h) : (target_h - 360.0f);
		float turn_time = (float) Math.toRadians(Math.abs(turn_angle)) / Ship.SHIP_ANGULAR_VELOCITY;
		float burntime = Math.max(DELTA_T - turn_time, 0.0f);

		//plan.add(new PlanAction(start_time, PlanAction.Action.fire));

		if (turn_angle > 0.0f)
		{
			if (m_GameData.getTurningRight())
			{
				plan.add(new PlanAction(start_time, PlanAction.Action.stopRight));
			}
			plan.add(new PlanAction(start_time, PlanAction.Action.startLeft));
			plan.add(new PlanAction(start_time + (long) Math.round(turn_time * 1000), PlanAction.Action.stopLeft));
		} else if (turn_angle < 0.0f)
		{
			if (m_GameData.getTurningLeft())
			{
				plan.add(new PlanAction(start_time, PlanAction.Action.stopLeft));
			}
			plan.add(new PlanAction(start_time, PlanAction.Action.startRight));
			plan.add(new PlanAction(start_time + (long) Math.round(turn_time * 1000), PlanAction.Action.stopRight));
		} else
		{
			if (m_GameData.getTurningRight())
			{
				plan.add(new PlanAction(start_time, PlanAction.Action.stopRight));
			}
			if (m_GameData.getTurningLeft())
			{
				plan.add(new PlanAction(start_time, PlanAction.Action.stopLeft));
			}
		}

		if (Math.abs(turn_angle) > 1.0f)
		{ //If already close to heading, just keep burning
			if (m_GameData.getMovingForward())
			{
				plan.add(new PlanAction(start_time, PlanAction.Action.stopForward));
			}
			plan.add(new PlanAction(start_time + (long) Math.round(turn_time * 1000), PlanAction.Action.startForward));
			plan.add(new PlanAction(start_time + (long) Math.round((turn_time + burntime) * 1000), PlanAction.Action.stopForward));
		} else
		{
			plan.add(new PlanAction(start_time, PlanAction.Action.startForward));
			plan.add(new PlanAction(start_time + (long) (DELTA_T * 1000), PlanAction.Action.stopForward));
		}

		Vector2 newV = new Vector2(1.0f, 0.0f);
		newV.rotate(ship.getAngle() + turn_angle);
		newV.scl(burntime * Ship.SHIP_LINEAR_ACCELERATION);
		if (newV.len() > Ship.SHIP_MAX_LINEAR_VELOCITY)
		{
			newV.nor();
			newV.scl(Ship.SHIP_MAX_LINEAR_VELOCITY);
		}
		newV.add(ship.getVelocity());

		Vector2 newPos = new Vector2(ship.getPosition());

		//Change in position during turn
		Vector2 deltaPos = new Vector2(ship.getVelocity());
		deltaPos.scl(turn_time);
		newPos.add(deltaPos);

		//Change in position during burn - assumes at final velocity for entire burn
		deltaPos = new Vector2(newV);
		deltaPos.scl(burntime);
		newPos.add(deltaPos);

		//TODO: fix final position calculation

		metrics.addPlanMetrics(newPos, newV, turn_angle);
	}
}
