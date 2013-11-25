package com.rip.javasteroid.engine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.rip.javasteroid.GameData;
import com.rip.javasteroid.entity.Asteroid;
import com.rip.javasteroid.entity.BaseEntity;
import com.rip.javasteroid.entity.Bullet;
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
	/* Constants */
	public static final int		HEIGHT					= 480;
	public static final int		WIDTH					= 640;
	public static final int		LARGE_ASTEROID_POINTS	= 20;
	public static final int		MEDIUM_ASTEROID_POINTS	= 50;
	public static final int		SMALL_ASTEROID_POINT	= 100;
	private static final float	BOX_STEP				= 1/60f;
	private static final int	BOX_VELOCITY_ITERATIONS	= 6;
	private static final int	BOX_POSITION_ITERATIONS	= 2;


	private OrthographicCamera	m_Camera;
	private World				m_World;
	private SpriteBatch			m_SpriteBatch;
	private Box2DDebugRenderer	m_DebugRenderer;
	private InputHandler		m_Handler;
	private RmiServer			m_Server;
	private GameData			m_GameData;
	private BitmapFont			m_Font;
	private Ship				m_Ship;
	private ArrayList<Asteroid> m_Asteroids;

	public AsteroidEngine()
	{
		m_World = new World(new Vector2(0, 0), true);
		m_SpriteBatch = new SpriteBatch();
		m_Font = new BitmapFont();
		m_Camera = new OrthographicCamera(WIDTH, HEIGHT);
		m_Camera.position.set(m_Camera.viewportWidth / 2, m_Camera.viewportHeight / 2, 0f);
		m_Camera.update();
		m_GameData = new GameData();

		m_DebugRenderer = new Box2DDebugRenderer();
		m_World.setContactListener(this);
		m_Ship = new Ship(new Vector2(AsteroidEngine.WIDTH / 2, AsteroidEngine.HEIGHT / 2), m_World);
		m_Asteroids = new ArrayList<Asteroid>();

		m_Handler = new InputHandler(m_Ship);
		try
		{
			m_Server = new RmiServer(m_GameData, m_Handler);
		}
		catch(Exception e)
		{
			System.out.println("RmiServer Initialization Exception: " + e.getMessage());
		}
	}

	@Override
	public void render(float delta)
	{
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		m_DebugRenderer.render(m_World, m_Camera.combined);
		m_SpriteBatch.begin();
		m_Font.draw(m_SpriteBatch, "Lives: " + m_GameData.getLives(), 10, HEIGHT - 10);
		m_Font.draw(m_SpriteBatch, "Score: " + m_GameData.getScore(), 210, HEIGHT - 10);
		for(Asteroid be: m_Asteroids)
			be.draw(m_SpriteBatch);
		m_Ship.draw(m_SpriteBatch);
		m_SpriteBatch.end();

		for(Asteroid be: m_Asteroids)
			be.update(delta);
		m_Ship.update(delta);
		m_GameData.updateShipData(m_Ship);
		m_GameData.updateAsteroidData(m_Asteroids);
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
			// Ship collided with an Asteroid
			System.out.println("Ship collision!");
			// Take a life
			if (m_GameData.takeLife())
			{
				// Game over
				m_GameData.reset();
			}
		}
		// Check for Asteroid/Bullet collision
		if((contact.getFixtureA().getBody().getUserData() instanceof Bullet &&
			contact.getFixtureB().getBody().getUserData() instanceof Asteroid) ||
			(contact.getFixtureB().getBody().getUserData() instanceof Bullet &&
			contact.getFixtureA().getBody().getUserData() instanceof Asteroid))
		{
			// Bullet collided with an Asteroid
			System.out.println("Bullet collision!");
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
