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
	    this.percent_safe = 0.0f;
    }

    public void addAsteroid(float distance, float angle, float impact, float HEADING_RANGE) {
        AsteroidData newAst = new AsteroidData(distance, angle, impact);
        newAst.setInRange(angle <= HEADING_RANGE || angle >= 360.0f - HEADING_RANGE);
        asteroidMetrics.add(newAst);
    }

    public void addPlanMetrics(Vector2 pos, Vector2 vel, float turn_angle) {
        this.position = pos;
        this.velocity = vel;

        boolean transit;
        for(AsteroidData ast : asteroidMetrics) {
            if(turn_angle >= 0.0f) {
                transit = ast.getAngle() < turn_angle;
            } else {
                transit = ast.getAngle() > (turn_angle + 360.0f);
            }
            ast.setTransit(transit);
        }
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

    public void setPercent_safe(float percent_safe) {
        this.percent_safe = percent_safe;
    }
}
