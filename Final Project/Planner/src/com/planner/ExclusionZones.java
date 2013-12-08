package com.planner;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Stef
 * Date: 12/7/13
 * Time: 8:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExclusionZones {
    private ArrayList<ExcludePoint> exclusions;

    public ExclusionZones() {
        exclusions = new ArrayList<ExcludePoint>();
    }

    public void addAll(ArrayList<ExcludePoint> addList) {
        if(this.exclusions.isEmpty()) {
            this.exclusions.addAll(addList);
        } else {
            ExcludePoint[] existing = this.exclusions.toArray(new ExcludePoint[this.exclusions.size()]);
            ExcludePoint[] add = addList.toArray(new ExcludePoint[addList.size()]);

            ArrayList<ExcludePoint> newList = new ArrayList<ExcludePoint>();
            ExcludePoint lastPoint = null;

            int exist_ndx = 0;
            int add_ndx = 0;

            while(exist_ndx < existing.length && add_ndx < add.length) {
                float exist_h = existing[exist_ndx].getHeading();
                float add_h = add[add_ndx].getHeading();

                //Select smaller heading; if one of the two says it's unsafe, it's unsafe
                boolean exist_min = (existing[exist_ndx].compareTo(add[add_ndx]) <= 0);
                ExcludePoint minPoint = (exist_min) ? (existing[exist_ndx]) : (add[add_ndx]);
                ExcludePoint newPoint = new ExcludePoint(minPoint.getHeading(), existing[exist_ndx].isSafe() & add[add_ndx].isSafe());

                if(exist_min) {
                    exist_ndx++;
                } else {
                    add_ndx++;
                }

                if(lastPoint == null) {
                    lastPoint = newPoint;
                } else {
                    //Collapse redundant headings
                    newPoint = lastPoint.merge(newPoint);
                    //Add to list if necessary
                    if(newPoint != null) {
                        newList.add(lastPoint);
                        lastPoint = newPoint;
                    }
                }
            }
            newList.add(lastPoint);
            this.exclusions = newList;
        }
    }

    public float findClosestSafeHeading(float heading) {
        float safeHeading = -180.0f;
        for(int ndx = 0; ndx < exclusions.size(); ndx++) {
            ExcludePoint excludePoint = exclusions.get(ndx);
            if(excludePoint.getHeading() > heading) {
                //If first larger one ends safe zone, then we can just go straight ahead
                if(excludePoint.isSafe()) {
                    safeHeading = heading;
                } else if(exclusions.size() > 1) { //otherwise we need to find the closest safe zone endpoint, forward or backward
                    int forward_ndx = (ndx+1 == exclusions.size() && !exclusions.get(1).isSafe()) ? (1) : (ndx);
                    int backward_ndx =(ndx-1 < 0 && !exclusions.get(exclusions.size()-1).isSafe()) ? (exclusions.size()-2) : ((ndx+exclusions.size()-1)%exclusions.size());
                    safeHeading = Math.min(exclusions.get(forward_ndx).getHeading(), exclusions.get(backward_ndx).getHeading());
                }
            }
        }
        return safeHeading;
    }

    public int size() {
        return exclusions.size();
    }

    public float getPercentSafe() {
        float percentSafe = 0.0f;
        for(int ndx = 0; ndx < exclusions.size(); ndx++) {
            float arcSize = exclusions.get(ndx).getHeading() - ((ndx > 0) ? (exclusions.get(ndx-1).getHeading()) : (0.0f));
            percentSafe += (arcSize * ((exclusions.get(ndx).isSafe()) ? (1.0f) : (0.0f))) / 360.0f;
        }
        return percentSafe;
    }
}
