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
    private static final int GAMES_TO_PLAY = 1;
    private static float SAFE_DISTANCE;
    private static float SAFETY_FACTOR;
    private static float DELTA_T;
    private static float HEADING_RANGE;

    /* Private Attributes */
    private static QueryInterface	m_Server;
    private static PlanExecutor		m_Executor;
    private static GameData			m_GameData;
    private static ArrayList<PlanAction> m_Plan;
    private static ArrayList<Metrics> m_Metrics;
	private static DecisionTree m_DecisionTree;

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
	        m_DecisionTree.train(true, 0.0f, 0.0f, 0.0f, true);
	        m_DecisionTree.train(true, 1.0f, 0.0f, 0.0f, true);
	        m_DecisionTree.train(true, 4.0f, 0.0f, 0.0f, false);
	        m_DecisionTree.train(true, 3.0f, 0.0f, 0.0f, false);
	        m_DecisionTree.printTree();

            m_Plan = new ArrayList<PlanAction>();
            m_Metrics = new ArrayList<Metrics>();

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
                    m_Plan = determinePlan();
                    m_Executor.setPlan(m_Plan);
                    Thread.sleep(1);
                }
                catch(Throwable e)
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
        catch(Throwable e)
        {
            System.out.println("Main.main(): " + e.getMessage());
        }
    }

    /**
     * Execute the planner to determine the best plan
     * @return Queue of planned actions
     */
    //private static ConcurrentLinkedQueue<PlanAction> determinePlan()
    private static ArrayList<PlanAction> determinePlan() {
        ArrayList<PlanAction> plan = new ArrayList<PlanAction>();
        try
        {
            long pullTime = System.currentTimeMillis();
            EntityData ship = m_GameData.getShipData();
            ArrayList<EntityData> asteroids = m_GameData.getAsteroidData();

            ExclusionZones exclusions;

            SAFETY_FACTOR = 0.20f;
            SAFE_DISTANCE = 20.0f * Ship.SHIP_MAX_LINEAR_VELOCITY;
            HEADING_RANGE = 5.0f;

            Metrics new_met = new Metrics(pullTime);

            float target_h;

            //Safe distance gets decreased each time search fails to find target heading
            do {
                SAFE_DISTANCE = SAFE_DISTANCE * 0.50f;

                //Calculate safe/unsafe headings
                exclusions = calculateExclusions(ship, asteroids, new_met);

                //Select heading
                target_h = selectHeading(ship, asteroids, exclusions);
                System.out.println(target_h);
            } while(target_h < 0.0f && SAFE_DISTANCE > ship.getRadius() * (1.0f + SAFETY_FACTOR));
            //Don't let safe distance become arbitrarily small

            //Do nothing if no safe heading found - going to crash anyway
            if(target_h >= 0.0f) {
                //Determine combination of turning and thrusting to achieve that heading
                calculatePlan(plan, ship, asteroids, exclusions, target_h, pullTime, new_met);

                m_Metrics.add(new_met);

                //Shift plan times to account for execution time
                long planDiff = System.currentTimeMillis() - pullTime;
                for(PlanAction action : plan) {
                    action.shiftTime(planDiff);
                }
            }
        }
        catch(Throwable t)
        {
            System.out.println("DeterminePlan(): " + t.getMessage());
        }
        // Return the plan
        return plan;
    }

    private static ExclusionZones calculateExclusions(EntityData ship, ArrayList<EntityData> asteroids, Metrics metrics) {
        ExclusionZones exclusions = new ExclusionZones();

        float shipAngle = ship.getAngle();

        //Time step to examine - default to time required to do 180 degree turn and accelerate to 25% of max velocity
        DELTA_T = (float)(Math.PI / Ship.SHIP_ANGULAR_VELOCITY + 0.25f * Ship.SHIP_MAX_LINEAR_VELOCITY / Ship.SHIP_LINEAR_ACCELERATION);

        //Asteroid state metrics
        for(EntityData asteroid : asteroids) {
            //Calculate relative position
            Vector2 relpos = new Vector2(asteroid.getPosition());
            relpos.sub(ship.getPosition());

            float config_radius = ship.getRadius() * (1.0f + SAFETY_FACTOR) + asteroid.getRadius();

            //Calculate occlusion points
            float rotate_angle;
              if(config_radius > relpos.len()) {
                rotate_angle = 90.0f;
            } else {
                rotate_angle = (float)Math.toDegrees(Math.asin(config_radius/relpos.len()));
            }

            float[] occ_angle = new float[2];
            occ_angle[0] = relpos.angle() - rotate_angle;
            occ_angle[1] = occ_angle[0] + 2*rotate_angle;
            for(int ndx = 0; ndx < occ_angle.length; ndx++) {
                while(occ_angle[ndx] < 0.0f) {
                    occ_angle[ndx] += 360.0f;
                }
                while(occ_angle[ndx] >= 360.0f) {
                    occ_angle[ndx] -= 360.0f;
                }
            }

            //Calculate relative velocity
            Vector2 relv = new Vector2(ship.getVelocity());
            relv.sub(asteroid.getVelocity());

            float angle = relpos.angle() - shipAngle;
            while(angle < 0.0f) {
                angle += 360.0f;
            }
            while(angle >= 360.0f) {
                angle -= 360.0f;
            }
            float distance = relpos.len();
            float impactTime = -1.0f;
            boolean inRange = (angle <= HEADING_RANGE) || (angle >= 360.0f - HEADING_RANGE);
            boolean intersect;

            //is the relative velocity between the exclusion points?
            if(occ_angle[0] < occ_angle[1]) {
                intersect = relv.angle() >= occ_angle[0] && relv.angle() <= occ_angle[1];
            } else {
                intersect = relv.angle() >= occ_angle[0] || relv.angle() <= occ_angle[1];
            }

            if(intersect) {
                impactTime = distance / relv.len();
            }

            //Time step is minimum of default delta-t and smallest time to impact
            if(impactTime >= 0.0f && impactTime < DELTA_T) {
                DELTA_T = impactTime * 0.75f;
            }

            metrics.addAsteroid(distance, angle, impactTime, HEADING_RANGE);
        }

        //Calculate delta-V curve
        int granularity = 1000; //per semicircle
        float[] delta_theta = new float[2*granularity+1];
        Vector2[] deltaV = new Vector2[delta_theta.length];

        for(int ndx = 0; ndx < deltaV.length ; ndx++)
        {
            delta_theta[ndx] = (360.0f * ndx)/(2*granularity);
            deltaV[ndx] = new Vector2(1f,0f);
            deltaV[ndx].rotate(delta_theta[ndx]);
            float scalar = Ship.SHIP_LINEAR_ACCELERATION * (DELTA_T - (float)Math.toRadians(delta_theta[ndx] < 180 ? delta_theta[ndx] : Math.abs(delta_theta[ndx]-360.0f)) / Ship.SHIP_ANGULAR_VELOCITY);
            deltaV[ndx].scl(Math.max(0.0f, scalar));
        }

        for(EntityData asteroid : asteroids)
        {
            Vector2 relpos = new Vector2(asteroid.getPosition());
            relpos.sub(ship.getPosition());

            float config_radius = ship.getRadius() * (1.0f + SAFETY_FACTOR) + asteroid.getRadius();
            Vector2[] occ_point = new Vector2[2];
            Vector2 relv;

            Vector2[] reachableV = new Vector2[deltaV.length];
            float[] theta = new float[deltaV.length];

            if(relpos.len() - config_radius < SAFE_DISTANCE)
            {
                //Calculate occlusion points
                float rotate_angle;
                float occ_len;
                if(config_radius > relpos.len()) {
                    rotate_angle = 90.0f;
                    occ_len = config_radius;
                } else {
                    rotate_angle = (float)Math.toDegrees(Math.asin(config_radius/relpos.len()));
                    occ_len = (float)(Math.sqrt(relpos.len() * relpos.len() - config_radius * config_radius));
                }
                occ_point[0] = new Vector2(1f,0f);
                occ_point[0].rotate(relpos.angle() - rotate_angle);
                occ_point[0].scl(occ_len);
                occ_point[1] = new Vector2(occ_point[0]);
                occ_point[1].rotate(2*rotate_angle);

                //Calculate relative velocity
                relv = new Vector2(ship.getVelocity());
                relv.sub(asteroid.getVelocity());

                //Add delta-V curve to relative velocity
                for(int ndx = 0; ndx < deltaV.length; ndx++)
                {
                    reachableV[ndx] = new Vector2(deltaV[ndx]);
                    reachableV[ndx].rotate(shipAngle);
                    reachableV[ndx].add(ship.getVelocity());
                    if(reachableV[ndx].len() > Ship.SHIP_MAX_LINEAR_VELOCITY) {
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
                for(int ndx = 0; ndx < theta.length; ndx++) {
                    for(int occ_ndx = 0; occ_ndx < 2; occ_ndx++) {
                        theta_diff[occ_ndx][ndx] = theta[ndx] - occ_point[occ_ndx].angle();
                        // Locate crossings by finding sign changes of theta - occlusion_theta
                        if(theta_diff[occ_ndx][ndx] == 0.0f) {
                            occ_intersect.add(delta_theta[ndx]);
                        } else if(ndx > 0 && theta_diff[occ_ndx][ndx] * theta_diff[occ_ndx][ndx-1] < 0.0f) {
                            //TODO: Convert to zero-finding with interval bisection
                            float interp = (delta_theta[ndx] - delta_theta[ndx-1]) / (theta_diff[occ_ndx][ndx] - theta_diff[occ_ndx][ndx-1]) * theta_diff[occ_ndx][ndx] + delta_theta[ndx];
                            occ_intersect.add(interp);
                        }
                    }
                }

                //Future improvement - use area of delta-V curve and calculate intersection with exclusion cones
                //rather than simplistic heading exclusion

                if(occ_intersect.isEmpty()) {
                    occ_intersect.add(360.0f);
                }

                //March around delta_theta, at each occlusion heading check if it starts or ends an exclusion zone
                Collections.sort(occ_intersect);
                if(occ_intersect.get(occ_intersect.size()-1) != 360.0f) {
                    occ_intersect.add(360.0f);
                }

                ArrayList<ExcludePoint> new_excludes = new ArrayList<ExcludePoint>();

                float inc_angle = occ_point[1].angle() - occ_point[0].angle() + ((occ_point[1].angle() - occ_point[0].angle() >= 0) ? (0) : (360.0f));

                for(int ndx = 0; ndx < occ_intersect.size(); ndx++) {
                    float ang = 0.5f * (((ndx == 0) ? (0.0f) : (occ_intersect.get(ndx-1))) + occ_intersect.get(ndx));

                    Vector2 dv = new Vector2(1f,0f);
                    dv.rotate(ang);
                    dv.scl(Ship.SHIP_LINEAR_ACCELERATION * (DELTA_T - (float)Math.toRadians(ang < 180 ? ang : Math.abs(ang-360.0f)) / Ship.SHIP_ANGULAR_VELOCITY));
                    dv.rotate(shipAngle).add(relv);

                    ang = occ_point[1].angle() - dv.angle() + ((occ_point[1].angle() - dv.angle() >= 0) ? (0) : (360.0f));
                    ang += dv.angle() - occ_point[0].angle() + ((dv.angle() - occ_point[0].angle() >= 0) ? (0) : (360.0f));

                    //Safe if sum of angles is greater than angle between occlusion points
                    new_excludes.add(new ExcludePoint(occ_intersect.get(ndx),ang > inc_angle));
                }

                //Merge new exclusion headings with existing
                exclusions.addAll(new_excludes);

            }
        }

        //Calculate safe percentage of headings
        metrics.setPercent_safe(exclusions.getPercentSafe());

        return exclusions;
    }

    private static float selectHeading(EntityData ship, ArrayList<EntityData> asteroids, ExclusionZones exclusions) {
        return exclusions.findClosestSafeHeading(0.0f);
    }

    private static void calculatePlan(ArrayList<PlanAction> plan, EntityData ship, ArrayList<EntityData> asteroids, ExclusionZones exclusions, float target_h, long start_time, Metrics metrics) {
        float turn_angle = (target_h <= 180.0f) ? (target_h) : (360.0f - target_h);
        float turn_time = (float)Math.toRadians(Math.abs(turn_angle)) / Ship.SHIP_ANGULAR_VELOCITY;
        float burntime = DELTA_T - turn_time;
        PlanAction.Action turnstart = (turn_angle > 0) ? (PlanAction.Action.startLeft) : (PlanAction.Action.startRight);
        PlanAction.Action turnstop = (turn_angle > 0) ? (PlanAction.Action.stopLeft) : (PlanAction.Action.stopRight);

        //plan.add(new PlanAction(start_time, PlanAction.Action.fire));
        plan.add(new PlanAction(start_time, PlanAction.Action.stopLeft));
        plan.add(new PlanAction(start_time, PlanAction.Action.stopRight));
        plan.add(new PlanAction(start_time, PlanAction.Action.stopForward));
        plan.add(new PlanAction(start_time, turnstart));
        plan.add(new PlanAction(start_time+(long)Math.round(turn_time * 1000),turnstop));
        if(Math.abs(turn_angle) > 1.0f) { //If already close to heading, just keep burning
            plan.add(new PlanAction(start_time+(long)Math.round(turn_time * 1000), PlanAction.Action.startForward));
            plan.add(new PlanAction(start_time+(long)Math.round((turn_time+burntime)*1000), PlanAction.Action.stopForward));
        } else {
            plan.add(new PlanAction(start_time, PlanAction.Action.startForward));
            plan.add(new PlanAction(start_time + (long)(DELTA_T *1000), PlanAction.Action.stopForward));
        }

        Vector2 newV = new Vector2(1.0f, 0.0f);
        newV.rotate(ship.getAngle() + turn_angle);
        newV.scl(burntime * Ship.SHIP_LINEAR_ACCELERATION);
        if(newV.len() > Ship.SHIP_MAX_LINEAR_VELOCITY) {
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
