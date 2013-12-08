package com.planner;

import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

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

    public void addAsteroid(float distance, float angle, float impact, float HEADING_RANGE) {
        AsteroidData newAst = new AsteroidData(distance, angle, impact);
        newAst.setInRange(angle <= HEADING_RANGE || angle >= 360.0f - HEADING_RANGE);
        asteroidMetrics.add(newAst);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Vector2 getPosition() {
        return position;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public float getPercent_safe() {
        return percent_safe;
    }

    public ArrayList<AsteroidData> getAsteroidMetrics() {
        return asteroidMetrics;
    }
}
