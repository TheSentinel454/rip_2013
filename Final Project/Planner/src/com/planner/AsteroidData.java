package com.planner;

/**
 * Created with IntelliJ IDEA.
 * User: Stef
 * Date: 12/7/13
 * Time: 10:36 PM
 * To change this template use File | Settings | File Templates.
 */
//Distance to asteroid
//Time to impact for asteroid
//Angle to asteroid
//Within some heading range?
//Crossed during slew maneuver?

public class AsteroidData {
    private float distance;
    private float impactTime;
    private float angle;
    private boolean inRange;
    private boolean transit;

    public AsteroidData(float distance, float impactTime, float angle) {
        this.distance = distance;
        this.impactTime = impactTime;
        this.angle = angle;
    }

    public void setInRange(boolean inRange) {
        this.inRange = inRange;
    }

    public void setTransit(boolean transit) {
        this.transit = transit;
    }

    public float getDistance() {
        return distance;
    }

    public float getImpactTime() {
        return impactTime;
    }

    public float getAngle() {
        return angle;
    }

    public boolean isInRange() {
        return inRange;
    }

    public boolean isTransited() {
        return transit;
    }
}
