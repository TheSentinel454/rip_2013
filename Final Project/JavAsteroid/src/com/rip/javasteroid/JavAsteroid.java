package com.rip.javasteroid;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

import java.io.Console;

public class JavAsteroid extends Game implements ContactListener
{
	private static final float  BOX_STEP                = 1/60f;
	private static final int    BOX_VELOCITY_ITERATIONS = 6;
	private static final int    BOX_POSITION_ITERATIONS = 2;
	private static final float  WORLD_TO_BOX            = 0.01f;
	private static final float  BOX_WORLD_TO            = 100f;

	private OrthographicCamera m_Camera;
//	private SpriteBatch batch;
//	private Texture texture;
//	private Sprite sprite;
	private World m_World;
	private Box2DDebugRenderer m_DebugRenderer;

	@Override
	public void create()
	{
		m_World = new World(new Vector2(0, 0), true);
		m_Camera = new OrthographicCamera();
		m_Camera.viewportHeight = 320;
		m_Camera.viewportWidth = 480;
		m_Camera.position.set(m_Camera.viewportWidth * .5f, m_Camera.viewportHeight * .5f, 0f);
		m_Camera.update();

		// Ground body
		BodyDef groundBodyDef =new BodyDef();
		groundBodyDef.position.set(new Vector2(0, 10));
		Body groundBody = m_World.createBody(groundBodyDef);
		PolygonShape groundBox = new PolygonShape();
		groundBox.setAsBox((m_Camera.viewportWidth) * 2, 10.0f);
		groundBody.createFixture(groundBox, 0.0f);

		groundBodyDef.position.set(new Vector2(0, 310));
		Body groundBody2 = m_World.createBody(groundBodyDef);
		groundBody2.createFixture(groundBox, 0.0f);
		groundBox.dispose();

		// Dynamic Body
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.DynamicBody;
		bodyDef.position.set(m_Camera.viewportWidth / 2, m_Camera.viewportHeight / 2 - 50);
		bodyDef.linearVelocity.set(0, 50);
		Body body = m_World.createBody(bodyDef);
		CircleShape dynamicCircle = new CircleShape();
		dynamicCircle.setRadius(5f);
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = dynamicCircle;
		fixtureDef.density = 1.0f;
		fixtureDef.friction = 0.0f;
		fixtureDef.restitution = 1.0f;
		body.createFixture(fixtureDef);

		// Dynamic Body 2
		bodyDef.position.set(m_Camera.viewportWidth / 2, m_Camera.viewportHeight / 2 + 50);
		bodyDef.linearVelocity.set(0, -50);
		Body body2 = m_World.createBody(bodyDef);
		body2.createFixture(fixtureDef);
		dynamicCircle.dispose();

		m_DebugRenderer = new Box2DDebugRenderer();
		m_World.setContactListener(this);
	}

	@Override
	public void dispose()
	{
		m_World.dispose();
		m_DebugRenderer.dispose();
	}

	@Override
	public void render()
	{
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		m_DebugRenderer.render(m_World, m_Camera.combined);
		m_World.step(BOX_STEP, BOX_VELOCITY_ITERATIONS, BOX_POSITION_ITERATIONS);
	}

	@Override
	public void resize(int width, int height)
	{
	}

	@Override
	public void pause()
	{
	}

	@Override
	public void resume()
	{
	}

	@Override
	public void beginContact(Contact contact)
	{
		//To change body of implemented methods use File | Settings | File Templates.
		System.out.println("Begin Contact");
	}

	@Override
	public void endContact(Contact contact)
	{
		// Shouldn't need this...
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold)
	{
		// Shouldn't need this...
	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse)
	{
		// Shouldn't need this...
	}
}
