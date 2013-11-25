package com.rip.javasteroid;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.rip.javasteroid.engine.AsteroidEngine;
import com.rip.javasteroid.entity.Asteroid;
import com.rip.javasteroid.entity.Ship;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Luke
 * Date: 11/22/13
 * Time: 9:28 PM
 */
public class GameData
{
	/* Private Attributes */
	private int m_Score = 0;
	private int m_Lives = 3;
	private Ship m_Ship;
	private ArrayList<Asteroid> m_Asteroids;

	/**
	 * Get the Ship
	 * @return Ship
	 */
	public Ship getShip()
	{
		return m_Ship;
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
		return m_Score;
	}

	/**
	 * Get the Asteroids
	 * @return List of Asteroids
	 */
	public ArrayList<Asteroid> getAsteroids()
	{
		return m_Asteroids;
	}

	/**
	 * Get a specific Asteroid
	 * @param index - Index of the Asteroid to get
	 * @return Asteroid
	 */
	public Asteroid getAsteroid(int index)
	{
		return (index < m_Asteroids.size() ? m_Asteroids.get(index) : null);
	}

	/**
	 * Add an Asteroid
	 * @param asteroid - Asteroid to be added
	 */
	public void addAsteroid(Asteroid asteroid)
	{
		m_Asteroids.add(asteroid);
	}

	/**
	 * Game data constructor
	 * @param world - Box2D world
	 */
	public GameData(World world)
	{
		reset(world);
	}

	public void dispose()
	{

	}

	/**
	 * Reset the game data to defaults
	 * @param world
	 */
	public void reset(World world)
	{
		m_Ship = new Ship(new Vector2(AsteroidEngine.WIDTH / 2, AsteroidEngine.HEIGHT / 2), world);
		m_Asteroids = new ArrayList<Asteroid>();
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
}
