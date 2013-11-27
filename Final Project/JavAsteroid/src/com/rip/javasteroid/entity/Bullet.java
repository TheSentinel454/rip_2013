package com.rip.javasteroid.entity;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;

/**
 * Created with IntelliJ IDEA.
 * User: Luke
 * Date: 11/24/13
 * Time: 6:54 PM
 */
public class Bullet extends BaseEntity
{
	/* Constants */
	public static final float BULLET_VELOCITY 	= 500.0f;
	private static final float LIFE_TIME 		= 3.0f;	// 3 seconds
	private static final float BULLET_RADIUS 	= 1.5f;

	/* Private Attributes */
	private float m_Life = 0.0f;
	private boolean m_Dead = false;

	/**
	 * Bullet Constructor
	 * @param pos
	 * @param vel
	 * @param world
	 */
	public Bullet(Vector2 pos, Vector2 vel, World world)
	{
		super(pos, world, BodyDef.BodyType.KinematicBody);
		// Create the fixture
		makeCircleFixture(BULLET_RADIUS, 1.0f, 1.0f);
		// Set initial velocities
		m_Body.setLinearVelocity(vel);
		m_Body.setAngularVelocity(0.0f);
	}

	/**
	 * Is the bullet dead?
	 * @return True if dead, False otherwise
	 */
	public boolean isDead()
	{
		return m_Dead;
	}

	/**
	 *
	 * @param dt - Delta Time
	 */
	@Override
	public void update(float dt)
	{
		// Add to the bullet lifetime
		m_Life += dt;
		// Check to see if the bullet dead
		if (m_Life >= LIFE_TIME)
			// Mark the bullet as dead
			m_Dead = true;
		// Call the base update
		super.update(dt);
	}

	@Override
	public void destroy()
	{
		// Mark the bullet as dead
		m_Dead = true;
	}

	public void dispose()
	{
		System.out.println("Kill bullet");
	}
}
