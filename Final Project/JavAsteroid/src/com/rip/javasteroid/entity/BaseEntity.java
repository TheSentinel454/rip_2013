package com.rip.javasteroid.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.rip.javasteroid.util.TextureWrapper;

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

	protected Body m_Body;
	protected Vector2 m_WorldPosition;
	protected TextureWrapper m_Texture;

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
		m_WorldPosition = new Vector2();
		createBody(world, pos, 0, bodyType);
		m_Body.setUserData(this);
	}

	/**
	 * Create a m_Body based on the position, angle, and m_Body type specified
	 * @param world World to use in creating the m_Body
	 * @param pos
	 * @param angle
	 * @param bodyType
	 */
	public void createBody(World world, Vector2 pos, float angle, BodyDef.BodyType bodyType)
	{
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = bodyType;
		bodyDef.position.set(pos.x, pos.y);// ConvertToBox(pos.x), ConvertToBox(pos.y));
		bodyDef.angle = angle;
		m_Body = world.createBody(bodyDef);
	}

	/**
	 * Make a rectangular fixture of specified width, height, density, and restitution
	 * @param width Width of the rectangular fixture
	 * @param height Height of the rectangular fixture
	 * @param density Density of the fixture
	 * @param restitution Restitution of the fixture
	 */
	protected void makeRectFixture(float width, float height, float density, float restitution)
	{
		PolygonShape bodyShape = new PolygonShape();

		float w = ConvertToBox(width / 2f);
		float h = ConvertToBox(height / 2f);
		bodyShape.setAsBox(w, h);

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.density = density;
		fixtureDef.restitution = restitution;
		fixtureDef.shape = bodyShape;

		m_Body.createFixture(fixtureDef);
		bodyShape.dispose();
	}

	/**
	 * Make a circular fixture of specified radius, density, and restitution
	 * @param radius Radius of the circular fixture
	 * @param density Density of the fixture
	 * @param restitution Restitution of the fixture
	 */
	protected void makeCircleFixture(float radius, float density, float restitution)
	{
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.density = density;
		fixtureDef.restitution = restitution;
		fixtureDef.shape = new CircleShape();
		fixtureDef.shape.setRadius(radius);//ConvertToBox(radius));

		m_Body.createFixture(fixtureDef);
		fixtureDef.shape.dispose();
	}

	/**
	 * Update the internal world position based on the m_Body's current position
	 */
	public void updateWorldPosition()
	{
		m_WorldPosition.set(m_Body.getPosition().x, m_Body.getPosition().y);//ConvertToWorld(m_Body.getPosition().x), ConvertToWorld(m_Body.getPosition().y));
	}

	/**
	 * Draw the texture of this entity
	 * @param sp - Sprite batch to draw with
	 */
	public void draw(SpriteBatch sp)
	{
		m_Texture.draw(sp);
	}

	/**
	 * Update the position (world/box2d) and rotation
	 * @param dt - Delta Time
	 */
	public void update(float dt)
	{
		// Update the world position
		updateWorldPosition();
		// Set the updated world position
		m_Texture.setPosition(m_WorldPosition);
		// Set the correct rotation
		m_Texture.setRotation(m_Body.getAngle() * MathUtils.radiansToDegrees);
	}

	/**
	 *
	 * @param fileName
	 */
	protected void loadTexture(String fileName)
	{
		m_Texture = new TextureWrapper(new Texture(Gdx.files.internal(fileName)), m_WorldPosition);
	}
}
