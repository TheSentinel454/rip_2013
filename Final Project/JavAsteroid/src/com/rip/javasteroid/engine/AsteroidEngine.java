package com.rip.javasteroid.engine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.rip.javasteroid.entity.Asteroid;
import com.rip.javasteroid.entity.BaseEntity;
import com.rip.javasteroid.entity.Ship;
import com.rip.javasteroid.remote.RmiServer;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Luke
 * Date: 11/11/13
 * Time: 8:58 PM
 */
public class AsteroidEngine implements Screen, ContactListener
{
	private static final float  BOX_STEP                = 1/60f;
	private static final int    BOX_VELOCITY_ITERATIONS = 6;
	private static final int    BOX_POSITION_ITERATIONS = 2;

	private OrthographicCamera m_Camera;
	private World m_World;
	private SpriteBatch m_SpriteBatch;
	private Box2DDebugRenderer m_DebugRenderer;
	private InputHandler m_Handler;
	private RmiServer m_Server;

	private ArrayList<BaseEntity> m_Entities = new ArrayList<BaseEntity>();

	public AsteroidEngine()
	{
		try
		{
			m_Server = new RmiServer();
		}
		catch(Exception e)
		{
			System.out.println("RmiServer Initialization Exception: " + e.getMessage());
		}
		m_World = new World(new Vector2(0, 0), true);
		m_SpriteBatch = new SpriteBatch();
		m_Camera = new OrthographicCamera();
		m_Camera.viewportHeight = 320;
		m_Camera.viewportWidth = 480;
		m_Camera.position.set(m_Camera.viewportWidth * .5f, m_Camera.viewportHeight * .5f, 0f);
		m_Camera.update();

		// Ground
		BodyDef groundBodyDef =new BodyDef();
		groundBodyDef.position.set(new Vector2(0, 10));
		Body groundBody = m_World.createBody(groundBodyDef);
		PolygonShape groundBox = new PolygonShape();
		groundBox.setAsBox((m_Camera.viewportWidth) * 2, 10.0f);
		groundBody.setUserData(new Ship(new Vector2(0, 0), m_World));
		groundBody.createFixture(groundBox, 0.0f);

		groundBodyDef.position.set(new Vector2(0, 310));
		Body groundBody2 = m_World.createBody(groundBodyDef);
		groundBody2.setUserData(new Ship(new Vector2(0,0), m_World));
		groundBody2.createFixture(groundBox, 0.0f);

		groundBox.setAsBox(10.0f, (m_Camera.viewportHeight) * 2);
		groundBodyDef.position.set(new Vector2(10, 0));
		Body groundBody3 = m_World.createBody(groundBodyDef);
		groundBody3.setUserData(new Ship(new Vector2(0,0), m_World));
		groundBody3.createFixture(groundBox, 0.0f);

		groundBodyDef.position.set(new Vector2(470, 0));
		Body groundBody4 = m_World.createBody(groundBodyDef);
		groundBody4.setUserData(new Ship(new Vector2(0,0), m_World));
		groundBody4.createFixture(groundBox, 0.0f);

		groundBox.dispose();

		for(int i = 0; i < 10; i++)
		{
			m_Entities.add(new Asteroid(new Vector2(m_Camera.viewportWidth / 2, m_Camera.viewportHeight / 2 + (10 * i)), m_World));
			m_Entities.add(new Asteroid(new Vector2(m_Camera.viewportWidth / 2, m_Camera.viewportHeight / 2 - (10 * i)), m_World));
		}
		/*
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
		*/

		m_DebugRenderer = new Box2DDebugRenderer();
		m_World.setContactListener(this);

		m_Handler = new InputHandler();
	}

	@Override
	public void render(float delta)
	{
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		m_DebugRenderer.render(m_World, m_Camera.combined);
		m_SpriteBatch.begin();
		for(BaseEntity be: m_Entities)
			be.draw(m_SpriteBatch);
		m_SpriteBatch.end();

		for(BaseEntity be: m_Entities)
			be.update(delta);
		m_World.step(BOX_STEP, BOX_VELOCITY_ITERATIONS, BOX_POSITION_ITERATIONS);
	}

	@Override
	public void resize(int width, int height)
	{
	}

	@Override
	public void show()
	{
		// Set the input processor
		Gdx.input.setInputProcessor(m_Handler);
	}

	@Override
	public void hide()
	{
		// Clear the input processor
		Gdx.input.setInputProcessor(null);
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
	public void dispose()
	{
		m_World.dispose();
		m_DebugRenderer.dispose();
		m_SpriteBatch.dispose();
	}

	@Override
	public void beginContact(Contact contact)
	{
		// Check for Asteroid/Ship collision
		if((contact.getFixtureA().getBody().getUserData() instanceof Ship &&
			contact.getFixtureB().getBody().getUserData() instanceof Asteroid) ||
			(contact.getFixtureB().getBody().getUserData() instanceof Ship &&
			contact.getFixtureA().getBody().getUserData() instanceof Asteroid))
		{
			System.out.println("Ship collision!");
			// Ship collided with an Asteroid
			// TODO: React to Ship/Asteroid collision
		}
	}

	@Override
	public void endContact(Contact contact)
	{
		// Don't need this...
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold)
	{
		// Don't need this...
	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse)
	{
		// Don't need this...
	}
}
