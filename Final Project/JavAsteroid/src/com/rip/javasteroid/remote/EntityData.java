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
	public EntityData(Vector2 pos, Vector2 vel, float angle)
	{
		m_Position = pos;
		m_Velocity = vel;
		m_Angle = angle;
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
	}

	/**
	 *
	 * @param entity
	 * @return
	 */
	public static EntityData fromBaseEntity(BaseEntity entity)
	{
		return new EntityData(entity.getBody().getPosition(), entity.getBody().getLinearVelocity(), entity.getBody().getAngle());
	}
}
