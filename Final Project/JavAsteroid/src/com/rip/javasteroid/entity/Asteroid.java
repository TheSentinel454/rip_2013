package com.rip.javasteroid.entity;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.rip.javasteroid.JavAsteroid;
import com.rip.javasteroid.engine.AsteroidEngine;

import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: Luke
 * Date: 11/9/13
 * Time: 10:52 PM
 */
public class Asteroid extends BaseEntity
{
	/* Constants */
	private static final float ASTEROID_DENSITY = 1.0f;
	private static final float ASTEROID_RESTITUTION = 1.0f;
	private static final float MAX_ANGULAR_VELOCITY = 1.5f;
	private static final float SMALL_VELOCITY = 120.0f;
	private static final float MEDIUM_VELOCITY = 50.0f;
	private static final float LARGE_VELOCITY = 20.0f;
	private static int ASTEROID_ID = 0;

	/* Private Attributes */
	private World m_World;
	private AsteroidSize m_Size;
	private int m_ID;

	public enum AsteroidSize
	{
		Small(17 / 2, AsteroidEngine.SMALL_ASTEROID_POINTS),
		Medium(27 / 2, AsteroidEngine.MEDIUM_ASTEROID_POINTS),
		Large(43 / 2, AsteroidEngine.LARGE_ASTEROID_POINTS);

		private float m_Radius;
		public float getRadius()
		{
			return m_Radius;
		}
		private int m_Value;
		public int getValue()
		{
			return m_Value;
		}

		private AsteroidSize(float radius, int value)
		{
			m_Radius = radius;
			m_Value = value;
		}
	}

	public AsteroidSize getSize()
	{
		return m_Size;
	}

	public int getValue()
	{
		return m_Size.getValue();
	}

	public int getID()
	{
		return m_ID;
	}

	/**
	 * Initialize an Asteroid
	 * @param pos - Position of the Asteroid
	 * @param size - Size of the Asteroid
	 * @param world - World to create the Asteroid in
	 */
	public Asteroid(Vector2 pos, AsteroidSize size, World world)
	{
		super(pos, world, BodyDef.BodyType.DynamicBody);
		// Save the size
		m_Size = size;
		// Save the world
		m_World = world;
		// Set the asteroid ID
		m_ID = ASTEROID_ID++;
		// Create the fixture
		makeCircleFixture(size.getRadius(), ASTEROID_DENSITY, ASTEROID_RESTITUTION);
		// Set the velocity
		setVelocity();
		// Build the image name
		String imgName = "data/Asteroid_";
		switch(size)
		{
			case Small:
				imgName += "S";
				break;
			case Medium:
				imgName += "M";
				break;
			case Large:
			default:
				imgName += "L";
				break;
		}
		// Add the random numeric
		imgName += Integer.toString(new Random().nextInt(3) + 1);
		imgName += ".png";
		// Load the texture
		loadTexture(imgName);
	}

	/**
	 * Set the velocity of the Asteroid
	 */
	private void setVelocity()
	{
		// Randomize velocity angle
		m_Body.setTransform(m_Body.getPosition(), (float)(Math.random() * (2 * Math.PI)));
		// Set velocity
		Vector2 velocity = new Vector2();
		velocity.set((float)Math.cos(m_Body.getAngle()), (float)Math.sin(m_Body.getAngle()));
		velocity.nor();
		switch (m_Size)
		{
			case Small:
				velocity.scl(SMALL_VELOCITY);
				break;
			case Medium:
				velocity.scl(MEDIUM_VELOCITY);
				break;
			case Large:
			default:
				velocity.scl(LARGE_VELOCITY);
				break;
		}
		m_Body.setLinearVelocity(velocity);
		// Set random angular velocity
		m_Body.setAngularVelocity((float)Math.random() * MAX_ANGULAR_VELOCITY);
	}

	/**
	 * Destroy the asteroid
	 */
	@Override
	public void destroy()
	{
		// Generate the new asteroids
		generateNewAsteroids();
		// Remove the body from the world
		m_World.destroyBody(m_Body);
	}

	/**
	 * Generate new asteroids based on the current asteroids information
	 */
	private void generateNewAsteroids()
	{
		// See if we need to split
		AsteroidSize newSize = null;
		// Check for medium
		if (m_Size == AsteroidSize.Medium)
			// Create 2 small asteroids at the same location
			newSize = AsteroidSize.Small;
		else if (m_Size == AsteroidSize.Large)
			// Create 2 medium asteroid at the same location
			newSize = AsteroidSize.Medium;
		// See if we have anything to do
		if (newSize != null)
		{
			try
			{
				// Create and add the new asteroid
				JavAsteroid.getEngine().addAsteroid(new Asteroid(m_Body.getPosition(), newSize, m_World));
				JavAsteroid.getEngine().addAsteroid(new Asteroid(m_Body.getPosition(), newSize, m_World));
			}
			catch(Throwable t)
			{
				System.out.println("Asteroid.generateNewAsteroids(): " + t.getMessage());
			}
		}
	}
}
