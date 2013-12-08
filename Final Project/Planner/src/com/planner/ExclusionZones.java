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

                //Collapse redundant headings
                newPoint = minPoint.merge(newPoint);

                //Add to list if necessary
                if(newPoint != null) {
                    newList.add(newPoint);
                }
            }
            this.exclusions = newList;
        }
    }
}
