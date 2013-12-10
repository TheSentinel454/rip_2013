package com.rip.javasteroid.remote;

import com.badlogic.gdx.math.Vector2;
import com.rip.javasteroid.entity.BaseEntity;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: Luke
 * Date: 11/24/13
 * Time: 10:42 PM
 */
public class EntityData implements Serializable
{
	/* Private Attributes */
	private Vector2 m_Position;
	private Vector2 m_Velocity;
	private float m_Angle;
	private float m_Radius;

	public float getRadius()
	{
		return m_Radius;
	}

	public void setRadius(float radius)
	{
		this.m_Radius = radius;
	}

	public Vector2 getPosition()
	{
		return m_Position;
	}

	public void setPosition(Vector2 position)
	{
		this.m_Position = position;
	}

	public Vector2 getVelocity()
	{
		return m_Velocity;
	}

	public void setVelocity(Vector2 velocity)
	{
		this.m_Velocity = velocity;
	}

	public float getAngle()
	{
		return m_Angle;
	}

	public void setAngle(float angle)
	{
		this.m_Angle = angle;
	}

	/**
	 * Base constructor
	 */
	public EntityData()
	{
		// Base constructor
	}

	/**
	 * Constructor to set values
	 * @param pos - Position
	 * @param vel - Velocity
	 * @param angle - Angle
	 */
	public EntityData(Vector2 pos, Vector2 vel, float angle, float radius)
	{
		m_Position = pos;
		m_Velocity = vel;
		m_Angle = angle;
		m_Radius = radius;
	}


    public EntityData(EntityData ed)
    {
        m_Position = ed.getPosition();
        m_Velocity = ed.getVelocity();
        m_Angle = ed.getAngle();
        m_Radius = ed.getRadius();
    }

    /**
	 * Update the entity data with the entity information
	 * @param entity - Entity to update with
	 */
	public void update(BaseEntity entity)
	{
		this.m_Position = entity.getBody().getPosition();
		this.m_Velocity = entity.getBody().getLinearVelocity();
		this.m_Angle = entity.getBody().getAngle();
        this.m_Angle = (float)Math.toDegrees(this.m_Angle);
        while(this.m_Angle < 0.0f) {
            this.m_Angle += 360.0f;
        }
        while(this.m_Angle >= 360.0f) {
            this.m_Angle -= 360.0f;
        }
		this.m_Radius = entity.getRadius();
	}

	/**
	 *
	 * @param entity
	 * @return
	 */
	public static EntityData fromBaseEntity(BaseEntity entity)
	{
		return new EntityData(entity.getBody().getPosition(), entity.getBody().getLinearVelocity(), entity.getBody().getAngle(), entity.getRadius());
	}
}
