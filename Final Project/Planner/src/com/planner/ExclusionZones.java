package com.planner;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Stef
 * Date: 12/7/13
 * Time: 8:26 PM
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

    public void addAll(ExclusionZones addZones) {
        this.addAll(addZones.getExclusions());
    }

    //searchSafe = do you want it safe (true) or unsafe (false)
    //Positive direction returns closest in positive (counter-clockwise) direction, negative only in negative direction, zero returns absolute closest
    public float findClosestHeading(float heading, boolean searchSafe, int direction) {
        float safeHeading = -180.0f;
        for(int ndx = 0; ndx < exclusions.size(); ndx++) {
            ExcludePoint excludePoint = exclusions.get(ndx);
            if(excludePoint.getHeading() > heading) {
                //If first larger one ends safe zone, then we can just go straight ahead
                if(!(excludePoint.isSafe() ^ searchSafe)) {
                    safeHeading = heading;
                } else if(exclusions.size() > 1) { //otherwise we need to find the closest safe zone endpoint, forward or backward
                    int forward_ndx = (ndx+1 == exclusions.size() && (exclusions.get(1).isSafe() ^ searchSafe)) ? (1) : (ndx);
                    int backward_ndx =(ndx == 0 && (exclusions.get(exclusions.size()-1).isSafe() ^ searchSafe)) ? (exclusions.size()-2) : ((ndx+exclusions.size()-1)%exclusions.size());
                    float forward_diff = exclusions.get(forward_ndx).getHeading() - heading;
                    if(forward_diff < 0.0f) {
                        forward_diff += 360.0f;
                    }
                    float backward_diff = heading - exclusions.get(backward_ndx).getHeading();
                    if(backward_diff < 0.0f) {
                        backward_diff += 360.0f;
                    }
                    if(direction > 0 || (direction == 0 && forward_diff < backward_diff)) {
                        safeHeading = exclusions.get(forward_ndx).getHeading();
                    } else {
                        safeHeading = exclusions.get(backward_ndx).getHeading();
                    }
                }
                break;
            }
        }
        return safeHeading;
    }

    public float findClosestSafeHeading(float heading) {
        return this.findClosestHeading(heading, true, 0);
    }

	public Float checkForSafeFireHeading(ArrayList<Float> fireHeadings)
	{
		Float safeHeading = null;
		int index = 0;
		for(int ndx = 0; ndx < exclusions.size() && index < fireHeadings.size(); ndx++)
		{
			ExcludePoint excludePoint = exclusions.get(ndx);
			// See if the heading
			if(excludePoint.getHeading() > fireHeadings.get(index))
			{
				// Check for safe heading
				if (excludePoint.isSafe())
				{
					// Found a safe heading
					safeHeading = fireHeadings.get(index);
					break;
				}
				// Unsafe heading
				else
				{
					// Move to the next index in fire headings
					index++;
					ndx = 0;
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

    public ArrayList<ExcludePoint> getExclusions() {
        return exclusions;
    }
}
