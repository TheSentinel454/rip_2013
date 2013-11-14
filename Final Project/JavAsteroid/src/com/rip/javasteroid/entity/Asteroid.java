package com.rip.javasteroid.entity;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;

import static java.lang.Math.*;

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
	private static final float MINIMUM_RADIUS = 1;
	private static final float MAXIMUM_RADIUS = 10;
	private static final float MINIMUM_VELOCITY = -40;
	private static final float MAXIMUM_VELOCITY = 40;

	/**
	 * Initialize an Asteroid
	 * @param pos - Position of the Asteroid
	 * @param radius - Radius of the Asteroid
	 * @param world - World to create the Asteroid in
	 */
	public Asteroid(Vector2 pos, float radius, World world)
	{
		super(pos, world, BodyDef.BodyType.DynamicBody);
		// Create the fixture
		makeCircleFixture(radius, ASTEROID_DENSITY, ASTEROID_RESTITUTION);
		// Set the velocity
		setRandomVelocity();
		// Load the texture
		loadTexture("data/Asteroid_M2.png");
	}

	/**
	 * Initialize an Asteroid
	 * The radius is randomly generated
	 * @param pos - Position of the Asteroid
	 * @param world - World to create the Asteroid in
	 */
	public Asteroid(Vector2 pos, World world)
	{
		this(pos, (float)(MINIMUM_RADIUS + ((MAXIMUM_RADIUS - MINIMUM_RADIUS) * random())), world);
	}

	/**
	 * Randomly set the velocity of the Asteroid
	 */
	private void setRandomVelocity()
	{
		// Set the Linear velocity
		m_Body.setLinearVelocity((float)(MINIMUM_VELOCITY + ((MAXIMUM_VELOCITY - MINIMUM_VELOCITY) * random())),
				(float)(MINIMUM_VELOCITY + ((MAXIMUM_VELOCITY - MINIMUM_VELOCITY) * random())));
		// Set the Angular velocity
		m_Body.setAngularVelocity((float)(MINIMUM_VELOCITY + ((MAXIMUM_VELOCITY - MINIMUM_VELOCITY) * random())));
	}
}
