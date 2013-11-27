package com.rip.javasteroid;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.rip.javasteroid.engine.AsteroidEngine;
import com.rip.javasteroid.entity.Asteroid;
import com.rip.javasteroid.entity.Ship;
import com.rip.javasteroid.remote.EntityData;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Luke
 * Date: 11/22/13
 * Time: 9:28 PM
 */
public class GameData implements Serializable
{
	/* Private Attributes */
	private int m_Score = 0;
	private Object m_ScoreLock = new Object();
	private int m_Lives = 3;
	private EntityData m_ShipData;
	private ArrayList<EntityData> m_AsteroidData;

	/**
	 * Update the ship data
	 * @param ship - Ship to update with
	 */
	public void updateShipData(Ship ship)
	{
		// Update the ship data
		m_ShipData.update(ship);
	}

	/**
	 * Update the Asteroid data
	 * @param asteroids
	 */
	public void updateAsteroidData(ArrayList<Asteroid> asteroids)
	{
		m_AsteroidData.clear();
		for(Asteroid asteroid: asteroids)
			m_AsteroidData.add(EntityData.fromBaseEntity(asteroid));
	}

	/**
	 * Get the current life count
	 * @return Life count
	 */
	public int getLives()
	{
		return m_Lives;
	}

	/**
	 * Get the score
	 * @return Score
	 */
	public int getScore()
	{
		synchronized (m_ScoreLock)
		{
			return m_Score;
		}
	}

	/**
	 * Add to the score
	 * @param points - Points to add to the score
	 */
	public void addScore(int points)
	{
		synchronized (m_ScoreLock)
		{
			m_Score += points;
		}
	}

	/**
	 * Game data constructor
	 */
	public GameData()
	{
		reset();
	}

	/**
	 * Dispose the game data
	 */
	public void dispose()
	{

	}

	/**
	 * Reset the game data to defaults
	 */
	public void reset()
	{
		m_ShipData = new EntityData();
		m_AsteroidData = new ArrayList<EntityData>();
		m_Score = 0;
		m_Lives = 3;
	}

	/**
	 * Take a life
	 * @return True if game over, False otherwise.
	 */
	public boolean takeLife()
	{
		m_Lives--;
		return (m_Lives == 0);
	}

    /**
     * Get the ship data
     * @return ship data
     */
    public EntityData getShipData() {
        return m_ShipData;
    }

    /**
     * Get the asteroid data
     * @return asteroid data
     */
    public ArrayList<EntityData> getAsteroidData() {
        return m_AsteroidData;
    }
}
