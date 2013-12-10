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
    private float distance; //distance to asteroid
    private float impactTime; //time to asteroid impact
    private float angle; //angle from front of ship to asteroid
    private ExclusionZones fireZones; //"unsafe" zones are good for firing
    private boolean inRange;
    private boolean transit;

    public AsteroidData(float distance, float angle, float impactTime, ExclusionZones fireZones) {
        this.distance = distance;
        this.impactTime = impactTime;
        this.angle = angle;
        this.fireZones = fireZones;
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

    public ExclusionZones getFireZones() {
        return fireZones;
    }
}
