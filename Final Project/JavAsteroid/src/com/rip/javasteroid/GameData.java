package com.rip.javasteroid;

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
	private Boolean m_GameOver = false;
	private Integer m_Score = 0;
	private Integer m_Lives = 3;
	private EntityData m_ShipData;
	private ArrayList<EntityData> m_AsteroidData;
	private Boolean m_TurningRight = false;
	private Boolean m_TurningLeft = false;
	private Boolean m_MovingForward = false;

	/**
	 * Update the ship data
	 * @param ship - Ship to update with
	 */
	public void updateShipData(Ship ship)
	{
		synchronized (m_ShipData)
		{
			// Update the ship data
			m_ShipData.update(ship);
			// Update the flags
			m_MovingForward = ship.isMoving();
			m_TurningLeft = ship.isTurningLeft();
			m_TurningRight = ship.isTurningRight();
		}
	}

	/**
	 * Update the Asteroid data
	 * @param asteroids - Asteroids to update with
	 */
	public void updateAsteroidData(ArrayList<Asteroid> asteroids)
	{
		synchronized (m_AsteroidData)
		{
			m_AsteroidData.clear();
			for(Asteroid asteroid: asteroids)
				m_AsteroidData.add(EntityData.fromBaseEntity(asteroid));
		}
	}

	/**
	 * Update the current game state
	 * @param gameOver - True if the game is over, False otherwise
	 */
	public void updateGameState(boolean gameOver)
	{
		synchronized (m_GameOver)
		{
			m_GameOver = gameOver;
		}
	}

	/**
	 * Get the current life count
	 * @return Life count
	 */
	public int getLives()
	{
		synchronized (m_Score)
		{
			return m_Lives;
		}
	}

	/**
	 * Get the score
	 * @return Score
	 */
	public int getScore()
	{
		synchronized (m_Score)
		{
			return m_Score;
		}
	}

	/**
	 * Is the ship currently turning right?
	 * @return True if the ship is turning right, False otherwise
	 */
	public Boolean getTurningRight()
	{
		synchronized (m_ShipData)
		{
			return m_TurningRight;
		}
	}

	/**
	 * Is the ship currently turning left?
	 * @return True if the ship is turning left, False otherwise
	 */
	public Boolean getTurningLeft()
	{
		synchronized (m_ShipData)
		{
			return m_TurningLeft;
		}
	}

	/**
	 * Is the ship currently moving forward?
	 * @return True if the ship is moving forward, False otherwise
	 */
	public Boolean getMovingForward()
	{
		synchronized (m_ShipData)
		{
			return m_MovingForward;
		}
	}

	/**
	 * Is the game over?
	 * @return True if the game is over, False otherwise
	 */
	public boolean isGameOver()
	{
		synchronized (m_GameOver)
		{
			return m_GameOver;
		}
	}

	/**
	 * Add to the score
	 * @param points - Points to add to the score
	 */
	public void addScore(int points)
	{
		synchronized (m_Score)
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
		synchronized (m_Lives)
		{
			m_Lives--;
			return (m_Lives == 0);
		}
	}

    /**
     * Get the ship data
     * @return ship data
     */
    public EntityData getShipData()
	{
		synchronized (m_ShipData)
		{
			return m_ShipData;
		}
	}

    /**
     * Get the asteroid data
     * @return asteroid data
     */
	public ArrayList<EntityData> getAsteroidData()
	{
		synchronized (m_AsteroidData)
		{
			return m_AsteroidData;
		}
	}
}
