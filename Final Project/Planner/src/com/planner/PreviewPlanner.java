// Author: Steffan Slater, Xinyan Yan
// Date: Dec 6, 2013
// Preview the future to decide which action to take.
// By tracing back an imaginary tree, we determine the next action 
// to take, which is contained in the path with maximal sum of 
// discounted rewards.

package com.planner;

import com.badlogic.gdx.math.Vector2;
import com.planner.*;
import com.rip.javasteroid.GameData;
import com.rip.javasteroid.entity.Ship;
import com.rip.javasteroid.remote.EntityData;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;

import java.util.ArrayList;
import java.util.Collections;

// Possible decisions
class Decision { 
//	public static final int SHOOT = 0;
	public static final int ANGLE0 = 0;
	public static final int ANGLE1 = 1;
	public static final int ANGLE2 = 2;
	public static final int ANGLE3 = 3;
    public static final int ANGLE4 = 4;
//    public static final int ANGLE5 = 5;
//    public static final int ANGLE6 = 6;
//    public static final int ANGLE7 = 7;
//    public static final int ANGLE8 = 8;

	public static final int NDECISIONS = 5;

}			


public class PreviewPlanner {

	private static final float BADSTATE = -100000.0f;
	private static final float NOPATH = -10000.0f;
	private static final float PERCENT_SAFE_BOUND = 0.05f;
	private static final float PERCENT_SAFE_DISCOUNT = 0.2f;
    private static final float TURN_SAFE_HALF_RANGE = 10.0f;
    private static final float TURNPENALTY = -0.05f;
    private static final float STICKBOUND = 6.0f;
    private static final float SHOOTBOUND = 20;


	private static float SAFE_DISTANCE;
	private static float SAFETY_FACTOR;
	private static float HEADING_RANGE;
	private static float WRAP_MARGIN = 25.0f;



	private long startTime;
	private int horizon; 			// number of steps in the future taken into account
	private int nEdges;				// number of edges
	private float discount;	// discount rate for future rewards
	private float[] edgeRewards; 	// discounted immediate rewards, later also used to calculate values in place
    private GameData m_GameData;
    private int lastDecision;
    private float HeadingUnit;
    private float HeadingStart;


	// Used to get the max value in part of an array
	class ArrayMax {
		int index;		// index of the max value in the array
		float maxVal;		// max value

		public ArrayMax(int i, float max) {
			index = i;
			maxVal = max;
		}

	}
	class State {
		Metrics met;
		ExclusionZones exclusions;
		int safeDistDecreaseTimes;
		float value;
        float targetUnit;
        float targetStart;
	}

    class DeltaVCurve {
        public float[] delta_theta;
        public Vector2[] deltaV;
        public DeltaVCurve(float[] dtheta, Vector2[] dV) {
            delta_theta = dtheta;
            deltaV = dV;
        }
    }


    private float CalculateAsteroidScore(ArrayList<AsteroidData> asD) {
        float minDist = Float.MAX_VALUE;
        float dist = 0.0f;
        for( AsteroidData asM : asD ) {
            dist = asM.getDistance();
            if (dist < minDist)
                minDist = dist;
        }
        return 10*(float)Math.log(minDist/150);
    }

	private float CalculateStateValue(State state) {
		float score = 0;
		float percentSafeScore = state.met.getPercent_safe() * (float)Math.pow(PERCENT_SAFE_DISCOUNT, state.safeDistDecreaseTimes) * 100;
		score += percentSafeScore;
    	float asteroidsScore = CalculateAsteroidScore(state.met.getAsteroidMetrics());
//        score += asteroidsScore;
//        System.out.printf("Percentage score: %f, minDist score: %f%n", percentSafeScore, asteroidsScore);
		return score;
	}




	private State CalculateEvaluation(EntityData ship, ArrayList<EntityData> asteroids) {

		State state;

		SAFETY_FACTOR = 0.20f;
		SAFE_DISTANCE = 10.0f * Ship.SHIP_MAX_LINEAR_VELOCITY;
		HEADING_RANGE = 5.0f;

		Metrics met = new Metrics(0);
		float percentSafe;
		int safeDistDecreaseTimes = -1;
        ExclusionZones exclusions;


        DeltaVCurve dvc = CalculateAsteriodStateMetricsDeltaVCurve(ship, asteroids, met);

        float turn_angle_half_range = (met.getDeltaT()) * (float)Math.toDegrees(Ship.SHIP_ANGULAR_VELOCITY);
        if (turn_angle_half_range > 180) turn_angle_half_range = 180;
        if (turn_angle_half_range > TURN_SAFE_HALF_RANGE)
            turn_angle_half_range -= TURN_SAFE_HALF_RANGE;


		do {

			safeDistDecreaseTimes += 1;

			//Calculate safe/unsafe headings
			exclusions = calculateExclusions(ship, asteroids, dvc, met);
    		percentSafe = met.getPercent_safe();

			//Safe distance gets decreased each time search fails to find target heading
			SAFE_DISTANCE = SAFE_DISTANCE * 0.50f;

		} while ( percentSafe < PERCENT_SAFE_BOUND && SAFE_DISTANCE > ship.getRadius() * (1.0f + SAFETY_FACTOR));


		if (percentSafe < PERCENT_SAFE_BOUND)
			state = null;
		else {
			state = new State();
			state.exclusions = exclusions;
			state.met = met;
			state.safeDistDecreaseTimes = safeDistDecreaseTimes;
			state.value = CalculateStateValue(state);
			state.targetUnit = 2 * turn_angle_half_range / (Decision.NDECISIONS-1);
            state.targetStart = -turn_angle_half_range;
		}

		return state;
	}


	private State Propagate(State state, int action, EntityData ship, ArrayList<EntityData> asteroids, 
								EntityData nextShip, ArrayList<EntityData> nextAsteroids) {
		
		// Closest degree
        float heading = state.targetStart + action*(state.targetUnit);
        if (heading < 0) heading += 360;
		float target_h = state.exclusions.findClosestSafeHeading(heading);
		
		// Ship new velocity and position
		PropagateShip(ship, target_h, state.met);
		nextShip.setPosition(state.met.getPosition());
		nextShip.setVelocity(state.met.getVelocity());

        float angle = target_h+nextShip.getAngle();

        if (angle > 360) angle -= 360;
        if (angle < 0) angle += 360;
        nextShip.setAngle(angle);

		if (nextShip.getAngle() < 0.0f || nextShip.getAngle() > 360.0f) {
            System.out.printf("Ship angle %f out of range! Exit", nextShip.getAngle());
            System.exit(1);
        }

		// Asteriods new positions
//		float dt = CalculatePlanTime(target_h, state.met.getDeltaT());
        float dt = state.met.getDeltaT();
		PropagateAsteroids(nextAsteroids, dt);

//		nextAsteroids = wrapScreen(nextAsteroids);

		// Evaluation for next state
		State nextState = CalculateEvaluation(nextShip, nextAsteroids);
		return nextState;
	}



//	private float CalculatePlanTime(float target_h, float deltaT) {
//
//		float dt = deltaT;
//	    float turn_angle = (target_h <= 180.0f) ? (target_h) : -(360.0f - target_h);
//
//        if (Math.abs(turn_angle) > 1.0f) {
//			float turn_time = (float) Math.toRadians(Math.abs(turn_angle)) / Ship.SHIP_ANGULAR_VELOCITY;
//			dt = Math.max(DELTA_T, turn_time);
//		}
//
//		return dt;
//	}


	// Constructor
	public PreviewPlanner(int h, float d) {
		horizon = h; 
		discount = d;
		nEdges = ((int)Math.pow(Decision.NDECISIONS,horizon+1)-1)/(Decision.NDECISIONS-1)-1;
		edgeRewards = new float[nEdges];

        if (Decision.NDECISIONS % 2 == 0)
            lastDecision = (int)Decision.NDECISIONS/2;
        else
            lastDecision = (int)(Decision.NDECISIONS-1)/2;


	}

    // Make decision
    // Layer starts from 0, and includes the current state
    private int MakeDecision(EntityData ship, ArrayList<EntityData> asteroids) {

        // Fill in edgeRewards, which contains discounted rewards by taking an action under a state
        if (ComputeDiscountedRewards(ship, asteroids) < 0) return -1;

        // Trace back the tree
        ArrayMax arrayMax = new ArrayMax(0, 0);
        for (int layer = horizon-1; layer > 0; layer--) {

            int a = ((int)Math.pow(Decision.NDECISIONS,layer)-1)/(Decision.NDECISIONS-1)-1;	// index for the first edge into the current layer
            int b = ((int)Math.pow(Decision.NDECISIONS,(layer+1))-1)/(Decision.NDECISIONS-1)-1; // index for the first edge out of the current layer

            for (int i = 0; i < Math.pow(Decision.NDECISIONS,layer); i++) {
                arrayMax = GetMax(edgeRewards, b+i*Decision.NDECISIONS, b+(i+1)*Decision.NDECISIONS);
                edgeRewards[a+i] += arrayMax.maxVal;
                System.out.printf("Reset node %d to %f%n", a+i, edgeRewards[a+i]);
            }

        }

        for (int i = 0; i < Decision.NDECISIONS; i++)
//            System.out.printf("Path %d, value: %f%n", i, edgeRewards[i]);

        arrayMax = GetMax(edgeRewards, 0, Decision.NDECISIONS);
        if (arrayMax.maxVal < NOPATH) {
            System.out.printf("No valid path to choose%n");
            return -1;
        }

        if ( Math.abs(edgeRewards[lastDecision] - arrayMax.maxVal) < STICKBOUND) {
//           System.out.printf("Choose last decision %d%n", lastDecision);
            return lastDecision;
        }


//        System.out.printf("Choose path %d%n", arrayMax.index);
        lastDecision = arrayMax.index;

        return arrayMax.index;

    }


	// Compute discounted rewards
	// Use recursive method
	private int ComputeDiscountedRewards(EntityData ship, ArrayList<EntityData> asteroids) {

		State state = CalculateEvaluation(ship, asteroids);

        HeadingStart = state.targetStart;
        HeadingUnit = state.targetUnit;

		// We are in very bad situation, do nothing
		if (state == null) {
            System.out.println("Preview From bad state!");
			return -1;
        }

		int layer = 1;
		for (int i = 0; i < Decision.NDECISIONS; i++) 
			ComputeDiscountedRewardsWorker(layer, state, ship, asteroids, i);

        return 0;

	}

	private void ComputeDiscountedRewardsWorker(int layer, State state, EntityData ship, 
													ArrayList<EntityData> asteroids, int action) {
		// Index for the edge
		int index = ((int)Math.pow(Decision.NDECISIONS,layer)-1)/(Decision.NDECISIONS-1)-1 + action;

		// Vars for new state
		EntityData nextShip = new EntityData(ship);
		ArrayList<EntityData> nextAsteroids = new ArrayList<EntityData>(asteroids);

		// Deal with the current edge
		State nextState = Propagate(state, action, ship, asteroids, nextShip, nextAsteroids);
		if (nextState == null) {
			edgeRewards[index] = BADSTATE;
			return;
		}

        float reward = nextState.value - state.value;

//		if (layer == 1) {
//            float heading = state.targetStart + action*state.targetUnit;
//            if (heading < 0) heading += 360;
//			float target_h = state.exclusions.findClosestSafeHeading(heading);
//			calculatePlan(planArr[action], ship, asteroids, state.exclusions, target_h, startTime, state.met, reward);
//		}


		edgeRewards[index] = reward * (float)Math.pow(discount,layer-1) + TURNPENALTY * Math.abs(state.met.getTurnAngle());	// discounted reward


		// Termination condition
		if (layer == horizon) return;

		// Update layer
		layer += 1;

		// What if ship takes ith action
		for (int i = 0; i < Decision.NDECISIONS; i++)
			ComputeDiscountedRewardsWorker(layer, nextState, nextShip, nextAsteroids, i);

	}


	// Propagate asteroids state, based on current position, velocity and dt
	private void PropagateAsteroids(ArrayList<EntityData> asteroids, float dt) {

		for(EntityData asteroid : asteroids) {

			Vector2 pos = asteroid.getPosition();
			Vector2 vel = new Vector2(asteroid.getVelocity());

			asteroid.setPosition(pos.add(vel.scl(dt)));
		}
	}


	public float GetHeading(GameData gd) {

        m_GameData = gd;
		int action = 0;

        EntityData ship = m_GameData.getShipData();
        ArrayList<EntityData> asteroids = m_GameData.getAsteroidData();

		startTime = System.currentTimeMillis();

		try {

//            wrapScreen(asteroids);

            action = MakeDecision(ship, asteroids);

            if (action < 0) {
                System.out.println("No good heading to pick!");
                return -180;
            }



		} catch (Throwable t) {
			System.out.println("DeterminePlan(): " + t.getMessage());
            System.out.println("Caught exception!");
            return -180;
		}

		float target_h = action*HeadingUnit + HeadingStart;
//        if (target_h < 0) target_h += 360;

        return target_h;
	}





	// Get the max value in part of an array
	private ArrayMax GetMax(float[] array, int lowerBound, int upperBound) {
		float maxSoFar = array[lowerBound];
		int index = lowerBound;

		for (int i = lowerBound+1; i < upperBound; i++) {
			if (array[i] > maxSoFar) {
				maxSoFar = array[i];
				index = i;
			}
		}

		return new ArrayMax(index, maxSoFar);
	}


    private void wrapScreen(ArrayList<EntityData> asteroids)
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


    private DeltaVCurve CalculateAsteriodStateMetricsDeltaVCurve(EntityData ship, ArrayList<EntityData> asteroids, Metrics met) {

        float shipAngle = ship.getAngle();
        float deltaT;

        //Time step to examine - default to time required to do 180 degree turn and accelerate to 25% of max velocity
        deltaT = (float) (Math.PI / Ship.SHIP_ANGULAR_VELOCITY + 0.25f * Ship.SHIP_MAX_LINEAR_VELOCITY / Ship.SHIP_LINEAR_ACCELERATION);

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
            deltaT = Math.min(impactTime, deltaT);

            ExclusionZones ez = null;
            met.addAsteroid(distance, angle, impactTime, HEADING_RANGE, ez);
        }
        met.setDeltaT(deltaT);

        //Calculate delta-V curve
        int granularity = 1000; //per semicircle
        float[] delta_theta = new float[2 * granularity + 1];
        Vector2[] deltaV = new Vector2[delta_theta.length];

        for (int ndx = 0; ndx < deltaV.length; ndx++)
        {
            delta_theta[ndx] = (360.0f * ndx) / (2 * granularity);
            deltaV[ndx] = new Vector2(1f, 0f);
            deltaV[ndx].rotate(delta_theta[ndx]);
            float scalar = Ship.SHIP_LINEAR_ACCELERATION * (deltaT - (float) Math.toRadians(delta_theta[ndx] < 180
                    ? delta_theta[ndx]
                    : Math.abs(delta_theta[ndx] - 360.0f)) / Ship.SHIP_ANGULAR_VELOCITY);
            deltaV[ndx].scl(Math.max(0.0f, scalar));
        }

        DeltaVCurve dvc = new DeltaVCurve(delta_theta, deltaV);
        return dvc;

    }


	private ExclusionZones calculateExclusions(EntityData ship, ArrayList<EntityData> asteroids, DeltaVCurve dvc, Metrics metrics)
	{
        float shipAngle = ship.getAngle();
        float[] delta_theta = dvc.delta_theta;
        Vector2[] deltaV = dvc.deltaV;
        float deltaT = metrics.getDeltaT();

		ExclusionZones exclusions = new ExclusionZones();

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
                    dv.scl(Ship.SHIP_LINEAR_ACCELERATION * (metrics.getDeltaT() - (float) Math.toRadians(ang < 180
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



	private void PropagateShip(EntityData ship, float target_h, Metrics metrics)
	{
        float deltaT = metrics.getDeltaT();
		float turn_angle = (target_h <= 180.0f) ? (target_h) : (target_h-360);
		float turn_time = (float) Math.toRadians(Math.abs(turn_angle)) / Ship.SHIP_ANGULAR_VELOCITY;
		float burntime = Math.max(deltaT - turn_time, 0.0f);

		Vector2 newV = new Vector2(1.0f, 0.0f);
		newV.rotate(ship.getAngle() + turn_angle);
//        newV.rotate(ship.getAngle() + target_h);

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




