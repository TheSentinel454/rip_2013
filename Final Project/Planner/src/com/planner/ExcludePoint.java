package com.planner;

/**
 * Created with IntelliJ IDEA.
 * User: Stef
 * Date: 12/7/13
 * Time: 8:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExcludePoint implements Comparable<ExcludePoint>{
    private float heading;
    private boolean safe;

    public ExcludePoint(float heading, boolean safe) {
        this.heading = heading;
        this.safe = safe;
    }

    public float getHeading() {
        return heading;
    }

    public boolean isSafe() {
        return safe;
    }

    public ExcludePoint merge(ExcludePoint o) {
        ExcludePoint retval = null;
        if(!(this.safe ^ o.isSafe())) {
            this.heading = Math.max(this.heading, o.getHeading());
        }  else {
            retval = o;
        }
        return retval;
    }

    @Override
    public int compareTo(ExcludePoint o) {
        return (int)Math.signum(this.heading - o.getHeading());
    }
}
