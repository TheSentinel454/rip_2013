package com.rip.javasteroid.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.rip.javasteroid.engine.AsteroidEngine;
import com.rip.javasteroid.util.TextureWrapper;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created with IntelliJ IDEA.
 * User: Luke
 * Date: 11/9/13
 * Time: 11:05 PM
 */
public class Ship extends BaseEntity
{
	/* Constants */
	private static final float SHIP_DENSITY = 1.0f;
	private static final float SHIP_RESTITUTION = 0.3f;
	private static final float SHIP_RADIUS = 27.0f / 2;
	private static final float SHIP_LINEAR_ACCELERATION = 125.0f;
	private static final float SHIP_MAX_LINEAR_VELOCITY = 60.0f;
	private static final float SHIP_DRAG_COEFFICIENT = 0.0f;
	private static final float FIRE_COOLDOWN = 0.15f;
	private static final float SHIP_ANGULAR_VELOCITY = 1.5f;

	public void setMoving(boolean moving)
	{
		this.m_Moving = moving;
		this.m_Texture = (moving ? m_MoveTexture : m_StopTexture);
	}
	public void setRotatingLeft(boolean rotatingLeft)
	{
		this.m_RotatingLeft = rotatingLeft;
	}
	public void setRotatingRight(boolean rotatingRight)
	{
		this.m_RotatingRight = rotatingRight;
	}
	public boolean isActive()
	{
		return m_Active;
	}

	private World 				m_World;
	private boolean				m_Moving		= false;
	private boolean				m_RotatingLeft	= false;
	private boolean				m_RotatingRight	= false;
	private float				m_Velocity		= 0.0f;
	private ArrayList<Bullet>	m_Bullets		= new ArrayList<Bullet>();
	private boolean				m_AbleToShoot	= true;
	private float				m_Cooldown		= FIRE_COOLDOWN;
	private boolean 			m_Active		= true;

	private TextureWrapper		m_MoveTexture;
	private TextureWrapper		m_StopTexture;

	/**
	 * Ship constructor
	 * @param pos - Ship position
	 * @param world - World to create the ship in
	 */
	public Ship(Vector2 pos, World world)
	{
		super(pos, world, BodyDef.BodyType.KinematicBody);
		// Save the world for bullet creation
		m_World = world;
		// Create the fixture
		makeCircleFixture(SHIP_RADIUS, SHIP_DENSITY, SHIP_RESTITUTION);
		// Set initial velocity
		m_Body.setLinearVelocity(0f,0f);
		m_Body.setLinearDamping(0.4f);
		// Load the textures
		m_StopTexture = new TextureWrapper(new Texture(Gdx.files.internal("data/Ship_OFF.png")), m_Body.getPosition());
		m_MoveTexture = new TextureWrapper(new Texture(Gdx.files.internal("data/Ship_ON.png")), m_Body.getPosition());
		setTexture(m_StopTexture);
	}

	/**
	 * Update the ship entity data
	 * @param dt - Delta Time
	 */
	@Override
	public void update(float dt)
	{
		// Update the Angular velocity
		updateAngularVelocity(dt);
		// Update the Linear velocity
		updateLinearVelocity(dt);
		// Update the bullets
		updateBullets(dt);
		// Update fire cool down
		updateCooldown(dt);
		// Update velocity
		super.update(dt);
	}

	/**
	 * Update the ship's linear velocity
	 * @param dt - Delta Time
	 */
	private void updateLinearVelocity(float dt)
	{
		// Pull the current velocity
		Vector2 velocity = m_Body.getLinearVelocity();
		m_Velocity = velocity.len();

		// Apply drag on ship proportional to current velocity squared and opposite current direction of motion
		Vector2 dragV = new Vector2(velocity);
		dragV.nor();
		dragV.scl(-1* SHIP_DRAG_COEFFICIENT * m_Velocity * m_Velocity * dt);
		velocity.add(dragV);

		// If moving (based on keyboard input), apply change in velocity in line with ship body axis
		if(m_Moving)
		{
			Vector2 deltaV = new Vector2();
			deltaV.set((float)Math.cos(m_Body.getAngle()), (float)Math.sin(m_Body.getAngle()));
			deltaV.scl(SHIP_LINEAR_ACCELERATION * dt);
			velocity.add(deltaV);
		}

		// Make sure new velocity is at or below max velocity
		if(velocity.len() > SHIP_MAX_LINEAR_VELOCITY)
		{
			// Scale down to max velocity but maintain direction to allow for turning even at max velocity
			velocity.nor();
			velocity.scl(SHIP_MAX_LINEAR_VELOCITY);
		}
		// Update body field
		m_Body.setLinearVelocity(velocity);
	}

	/**
	 * Update the ship's angular velocity
	 * @param dt - Delta Time
	 */
	private void updateAngularVelocity(float dt)
	{
		// Update the velocity based on the flags
		if (m_RotatingLeft && !m_RotatingRight)
			m_Body.setAngularVelocity(SHIP_ANGULAR_VELOCITY);
		else if (!m_RotatingLeft && m_RotatingRight)
			m_Body.setAngularVelocity(-SHIP_ANGULAR_VELOCITY);
		else
			m_Body.setAngularVelocity(0.0f);
	}

	/**
	 * Update the bullets
	 * @param dt - Delta Time
	 */
	private void updateBullets(float dt)
	{
		// Iterate through the bullets and update
		for(int i = m_Bullets.size() - 1; i > 0; i--)
		{
			// Update the bullet
			m_Bullets.get(i).update(dt);
			// Is the bullet dead?
			if (m_Bullets.get(i).isDead())
			{
				m_World.destroyBody(m_Bullets.get(i).getBody());
				// Dispose the bullet
				m_Bullets.get(i).dispose();
				// Remove the bullet from the list
				m_Bullets.remove(i);
			}
		}
	}

	/**
	 * Update the cool down
	 * @param dt - Delta Time
	 */
	private void updateCooldown(float dt)
	{
		// See if we need to update cool down info
		if (!m_AbleToShoot)
		{
			// Decrease the cool down timer
			m_Cooldown -= dt;
			// Check to see if cool down is complete
			if (m_Cooldown < 0)
			{
				// Reset the cool down and set the flag
				m_Cooldown = FIRE_COOLDOWN;
				m_AbleToShoot = true;
			}
		}
	}

	/**
	 * Fire a bullet if possible
	 */
	public void fire()
	{
		// See if we can fire
		if (m_AbleToShoot)
		{
			// Initialize a new bullet
			Vector2 velocity = new Vector2();
			velocity.set((float)Math.cos(m_Body.getAngle()), (float)Math.sin(m_Body.getAngle()));
			velocity.nor();
			velocity.scl(Bullet.BULLET_VELOCITY);
			Bullet bullet = new Bullet(m_Body.getPosition(), velocity, m_World);
			// Add to the list
			m_Bullets.add(bullet);
			// Start cool down
			m_AbleToShoot = false;
			m_Cooldown = FIRE_COOLDOWN;
		}
	}

	/**
	 * Destroy the ship
	 */
	@Override
	public void destroy()
	{
		try
		{
			// Set to inactive
			m_Active = false;
			// Start active timer (3 seconds)
			Timer timer = new Timer();
			timer.schedule(new TimerTask()
			{
				@Override
				public void run()
				{
					m_Active = true;
				}
			}, 3*1000);
			// Set the location back to the center
			m_Body.setTransform(new Vector2(AsteroidEngine.WIDTH / 2, AsteroidEngine.HEIGHT / 2), 0.0f);
			// Reset the firing flags
			m_AbleToShoot = true;
			m_Cooldown = 0.0f;
			// Reset the velocities
			m_Body.setLinearVelocity(0.0f,0.0f);
			m_Body.setAngularVelocity(0.0f);
			// Reset movement flags
			m_Moving = false;
			m_RotatingLeft = false;
			m_RotatingRight = false;
		}
		catch (Exception e)
		{
			System.out.println("Exception: " + e.getMessage());
		}
	}
}
