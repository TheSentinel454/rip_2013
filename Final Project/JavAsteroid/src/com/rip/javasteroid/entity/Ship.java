package com.rip.javasteroid.entity;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;

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

	private static final float SHIP_LINEAR_ACCELERATION = 105.0f;
	private static final float SHIP_MAX_LINEAR_VELOCITY = 40.0f;
    private static final float SHIP_DRAG_COEFFICIENT = 0.0f;

	//private static final float SHIP_ANGULAR_ACCELERATION = 105.0f;
	private static final float SHIP_ANGULAR_VELOCITY = 5.0f;

	public void setMoving(boolean moving)
	{
		this.m_Moving = moving;
	}
	public void setRotatingLeft(boolean rotatingLeft)
	{
		this.m_RotatingLeft = rotatingLeft;
	}
	public void setRotatingRight(boolean rotatingRight)
	{
		this.m_RotatingRight = rotatingRight;
	}

	private boolean m_Moving = false;
	private boolean m_RotatingLeft = false;
	private boolean m_RotatingRight = false;
	private float m_Velocity = 0.0f;

	/**
	 *
	 * @param pos
	 * @param world
	 */
	public Ship(Vector2 pos, World world)
	{
		super(pos, world, BodyDef.BodyType.DynamicBody);
		// Create the fixture
		makeCircleFixture(SHIP_RADIUS, SHIP_DENSITY, SHIP_RESTITUTION);
		// Set initial velocity
		m_Body.setLinearVelocity(0f,0f);
		// Load the texture
		loadTexture("data/Ship_OFF.png");
	}

	@Override
	public void update(float dt)
	{
		// Update the Angular velocity
		updateAngularVelocity(dt);
		// Update the Linear velocity
		updateLinearVelocity(dt);
		// Update velocity
		super.update(dt);
	}

	private void updateLinearVelocity(float dt)
	{
        Vector2 velocity = m_Body.getLinearVelocity(); //Pull current velocity
        m_Velocity = velocity.len();

        //Apply drag on ship proportional to current velocity squared and opposite current direction of motion
        Vector2 dragV = new Vector2(velocity);
        dragV.nor();
        dragV.scl(-1*(float)(SHIP_DRAG_COEFFICIENT * m_Velocity * m_Velocity * dt ));
        velocity.add(dragV);

        //If moving (based on keyboard input), apply change in velocity in line with ship body axis
        if(m_Moving) {
            Vector2 deltaV = new Vector2();
            deltaV.set((float)Math.cos(m_Body.getAngle()), (float)Math.sin(m_Body.getAngle()));
            deltaV.scl((float)(SHIP_LINEAR_ACCELERATION * dt));
            velocity.add(deltaV);
        }

        //Make sure new velocity is at or below max velocity
        if(velocity.len() > SHIP_MAX_LINEAR_VELOCITY) {
            //Scale down to max velocity but maintain direction to allow for turning even at max velocity
            velocity.nor();
            velocity.scl(SHIP_MAX_LINEAR_VELOCITY);
        }
        //Update body field
        m_Body.setLinearVelocity(velocity);
    }

	private void updateAngularVelocity(float dt)
	{
		System.out.println("AngleR: " + m_Body.getAngle());
		System.out.println("AngleD: " + getAngle());
		// Update the velocity based on the flags
		if (m_RotatingLeft && !m_RotatingRight)
			m_Velocity = SHIP_ANGULAR_VELOCITY;
		else if (!m_RotatingLeft && m_RotatingRight)
			m_Velocity = -SHIP_ANGULAR_VELOCITY;
		else
			m_Velocity = 0.0f;
		m_Body.setAngularVelocity(m_Velocity);
		System.out.println("Angle Velocity: " + m_Body.getAngularVelocity());
	}

	private float getAngle()
	{
		float angle = (m_Body.getAngle() * MathUtils.radiansToDegrees % 360);
		if (angle < 0)
			angle = angle + 360;
		return angle;
	}

	public void fire()
	{
	}
}
