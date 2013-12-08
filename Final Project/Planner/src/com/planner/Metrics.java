package com.planner;

import com.badlogic.gdx.math.Vector2;
import com.rip.javasteroid.remote.EntityData;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Stef
 * Date: 12/7/13
 * Time: 10:12 PM
 * To change this template use File | Settings | File Templates.
 */

//Metrics
//Position and velocity after executing plan
//Percentage of safe headings
//Data for individual asteroids

public class Metrics {
    private long timestamp;
    private Vector2 position;
    private Vector2 velocity;
    private float percent_safe;
    private ArrayList<AsteroidData> asteroidMetrics;

    public Metrics(long time) {
        this.timestamp = time;
        this.asteroidMetrics = new ArrayList<AsteroidData>();
    }

    public void addAsteroid() {

    }
}
