package com.rip.javasteroid.entity;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

/**
 * Created with IntelliJ IDEA.
 * User: Luke
 * Date: 11/9/13
 * Time: 10:44 PM
 */
public abstract class BaseEntity
{
	static final float WORLD_TO_BOX = 0.01f;
	static final float BOX_TO_WORLD = 100f;

	protected Body body;
	protected Vector2 worldPosition;

	float ConvertToBox(float x)
	{
		return x * WORLD_TO_BOX;
	}

	float ConvertToWorld(float x)
	{
		return x * BOX_TO_WORLD;
	}

	/**
	 *
	 * @param pos
	 * @param world
	 * @param bodyType
	 */
	public BaseEntity(Vector2 pos, World world, BodyDef.BodyType bodyType)
	{
		//userData = new BoxUserData(boxIndex, collisionGroup);
		worldPosition = new Vector2();
		CreateBody(world, pos, 0, bodyType);
		//body.setUserData(userData);
	}

	/**
	 *
	 * @param world
	 * @param pos
	 * @param angle
	 * @param bodyType
	 */
	public void CreateBody(World world, Vector2 pos, float angle, BodyDef.BodyType bodyType)
	{
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = bodyType;
		bodyDef.position.set(ConvertToBox(pos.x), ConvertToBox(pos.y));
		bodyDef.angle = angle;
		body = world.createBody(bodyDef);
	}

	/**
	 *
	 * @param width
	 * @param height
	 * @param density Density of the fixture
	 * @param restitution Restitution of the fixture
	 */
	private void MakeRectFixture(float width, float height, float density, float restitution)
	{
		PolygonShape bodyShape = new PolygonShape();

		float w = ConvertToBox(width / 2f);
		float h = ConvertToBox(height / 2f);
		bodyShape.setAsBox(w, h);

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.density = density;
		fixtureDef.restitution = restitution;
		fixtureDef.shape = bodyShape;

		body.createFixture(fixtureDef);
		bodyShape.dispose();
	}

	/**
	 * Make a circular fixture of specified radius, density, and restitution
	 * @param radius Radius of the circular fixture
	 * @param density Density of the fixture
	 * @param restitution Restitution of the fixture
	 */
	void MakeCircleFixture(float radius, float density, float restitution)
	{
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.density = density;
		fixtureDef.restitution = restitution;
		fixtureDef.shape = new CircleShape();
		fixtureDef.shape.setRadius(ConvertToBox(radius));

		body.createFixture(fixtureDef);
		fixtureDef.shape.dispose();
	}

	/**
	 * Update the internal world position based on the body's current position
	 */
	public void UpdateWorldPosition()
	{
		worldPosition.set(ConvertToWorld(body.getPosition().x), ConvertToWorld(body.getPosition().y));
	}
}
