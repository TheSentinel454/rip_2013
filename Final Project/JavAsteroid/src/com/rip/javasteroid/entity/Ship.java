package com.rip.javasteroid.entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.rip.javasteroid.util.TextureWrapper;

/**
 * Created with IntelliJ IDEA.
 * User: Luke
 * Date: 11/9/13
 * Time: 11:05 PM
 */
public class Ship extends BaseEntity
{
	TextureWrapper texture;

	/**
	 * @param pos
	 * @param world
	 * @param bodyType
	 */
	public Ship(Vector2 pos, World world, BodyDef.BodyType bodyType)
	{
		super(pos, world, bodyType);
	}

	/**
	 *
	 * @param sp
	 */
	public void Draw(SpriteBatch sp)
	{
		texture.Draw(sp);
	}

	/**
	 *
	 * @param dt
	 */
	public void Update(float dt)
	{
		UpdateWorldPosition();
		texture.SetPosition(worldPosition);
		// Set the correct rotation
		texture.SetRotation(body.getAngle() * MathUtils.radiansToDegrees);
	}
}
