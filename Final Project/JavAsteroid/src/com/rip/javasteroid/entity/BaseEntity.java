package com.rip.javasteroid.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.rip.javasteroid.engine.AsteroidEngine;
import com.rip.javasteroid.util.TextureWrapper;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: Luke
 * Date: 11/9/13
 * Time: 10:44 PM
 */
public abstract class BaseEntity
{
	/* Private Attributes */
	protected Body				m_Body;
	protected TextureWrapper	m_Texture;
	protected float				m_Radius;

	public Body getBody()
	{
		return m_Body;
	}

	/**
	 * Base Entity constructor
	 * @param pos - Position of the entity
	 * @param world - World to create the entity in
	 * @param bodyType - Body type of the entity
	 */
	public BaseEntity(Vector2 pos, World world, BodyDef.BodyType bodyType)
	{
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
		bodyDef.position.set(pos.x, pos.y);
		bodyDef.angle = angle;
		m_Body = world.createBody(bodyDef);
	}

	/**
	 * Make a circular fixture of specified radius, density, and restitution
	 * @param radius Radius of the circular fixture
	 * @param density Density of the fixture
	 * @param restitution Restitution of the fixture
	 */
	protected void makeCircleFixture(float radius, float density, float restitution)
	{
		m_Radius = radius;
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.density = density;
		fixtureDef.restitution = restitution;
		fixtureDef.shape = new CircleShape();
		fixtureDef.shape.setRadius(m_Radius);

		m_Body.createFixture(fixtureDef);
		fixtureDef.shape.dispose();
	}

	/**
	 * Update the body position based on the user's movement
	 */
	public void updatePosition()
	{
		float old_x = m_Body.getPosition().x;
		float old_y = m_Body.getPosition().y;

		if (old_x > (AsteroidEngine.WIDTH + m_Radius))
			m_Body.setTransform(-m_Radius, old_y, m_Body.getAngle());
		else if (old_x < -m_Radius)
			m_Body.setTransform((AsteroidEngine.WIDTH + m_Radius), old_y, m_Body.getAngle());

		if (old_y > (AsteroidEngine.HEIGHT+ m_Radius))
			m_Body.setTransform(m_Body.getPosition().x, -m_Radius, m_Body.getAngle());
		else if (old_y < -m_Radius)
			m_Body.setTransform(m_Body.getPosition().x, (AsteroidEngine.HEIGHT + m_Radius), m_Body.getAngle());
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
		// Update the position Toroidally
		updatePosition();
		// Make sure we have a texture
		if (m_Texture != null)
		{
			// Set the updated world position
			m_Texture.setPosition(m_Body.getPosition());
			// Set the correct rotation
			m_Texture.setRotation((m_Body.getAngle() * MathUtils.radiansToDegrees)+180);
		}
	}

	/**
	 * Load and set the texture
	 * @param fileName - Filename of the texture to load
	 */
	protected void loadTexture(String fileName)
	{
		m_Texture = new TextureWrapper(new Texture(Gdx.files.internal(fileName)), m_Body.getPosition());
	}

	/**
	 * Set the texture
	 * @param texture - Texture to be used
	 */
	protected void setTexture(TextureWrapper texture)
	{
		m_Texture = texture;
	}
}
