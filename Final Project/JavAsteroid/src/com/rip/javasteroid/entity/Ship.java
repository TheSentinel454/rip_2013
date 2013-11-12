package com.rip.javasteroid.entity;

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
	/**
	 *
	 * @param pos
	 * @param world
	 */
	public Ship(Vector2 pos, World world)
	{
		super(pos, world, BodyDef.BodyType.DynamicBody);
		// Set initial velocity
		m_Body.setLinearVelocity(0f,0f);
	}
}
