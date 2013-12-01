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

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

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
	public static final int		SMALL_ASTEROID_POINTS	= 100;
	private static final float	BOX_STEP				= 1/60f;
	private static final int	BOX_VELOCITY_ITERATIONS	= 6;
	private static final int	BOX_POSITION_ITERATIONS	= 2;

	/* Private Attributes */
	private OrthographicCamera	m_Camera;
	private World				m_World;
	private SpriteBatch			m_SpriteBatch;
	private Box2DDebugRenderer	m_DebugRenderer;
	private InputHandler		m_Handler;
	private RmiServer			m_Server;
	private GameData			m_GameData;
	private BitmapFont			m_Font;
	private Ship				m_Ship;
	private ArrayList<Asteroid>	m_Asteroids;
	private final Object 		m_CollisionQueueLock = new Object();
	private ConcurrentLinkedQueue<BaseEntity> m_CollisionQueue = new ConcurrentLinkedQueue<BaseEntity>();
	private Timer				m_Timer;
	private boolean				m_NewAsteroid = false;
	private boolean				m_GameOver = false;

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
		m_GameOver = true;

		// Generate a few asteroids to start the game
		generateNewAsteroid();
		generateNewAsteroid();
		generateNewAsteroid();
		generateNewAsteroid();

		// Start schedule to randomly add new ones
		m_Timer = new Timer();
		m_Timer.scheduleAtFixedRate(new TimerTask()
		{
			@Override
			public void run()
			{
				// Add a new asteroid
				m_NewAsteroid = true;
			}
		}, 5 * 1000, 5 * 1000); // 5 second timer for asteroid creation

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

		//m_DebugRenderer.render(m_World, m_Camera.combined);
		m_SpriteBatch.begin();
		m_Font.draw(m_SpriteBatch, "Lives: " + m_GameData.getLives(), 10, HEIGHT - 10);
		m_Font.draw(m_SpriteBatch, "Score: " + m_GameData.getScore(), 210, HEIGHT - 10);
		for(Asteroid be: m_Asteroids)
			be.draw(m_SpriteBatch);
		m_Ship.draw(m_SpriteBatch);
		if (m_GameOver)
			m_Font.drawMultiLine(m_SpriteBatch, "GAME OVER\n Press 'R' to restart!", WIDTH / 2, HEIGHT / 2, 15f, BitmapFont.HAlignment.CENTER);
		m_SpriteBatch.end();

		for(Asteroid be: m_Asteroids)
			be.update(delta);
		m_Ship.update(delta);
		m_GameData.updateShipData(m_Ship);
		m_GameData.updateAsteroidData(m_Asteroids);
		m_World.step(BOX_STEP, BOX_VELOCITY_ITERATIONS, BOX_POSITION_ITERATIONS);

		// Handle any Asteroid collisions
		synchronized (m_CollisionQueueLock)
		{
			BaseEntity entity;
			while ((entity = m_CollisionQueue.poll()) != null)
			{
				if (entity instanceof Asteroid)
					destroyAsteroid((Asteroid)entity);
				else if (entity instanceof Ship)
				{
					// Take a life
					if (m_GameData.takeLife())
						// Game over
						m_GameOver = true;
					entity.destroy();
				}
				else if (entity instanceof Bullet)
					entity.destroy();
			}
		}
		// See if we need to create another asteroid
		if (m_NewAsteroid)
		{
			// Generate a new asteroid
			generateNewAsteroid();
			// Flip the flag again
			m_NewAsteroid = false;
		}
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
		// Don't bother if the game is over
		if (m_GameOver) return;
		// Check for Asteroid/Ship collision
		if((contact.getFixtureA().getBody().getUserData() instanceof Ship &&
			contact.getFixtureB().getBody().getUserData() instanceof Asteroid) ||
			(contact.getFixtureB().getBody().getUserData() instanceof Ship &&
			contact.getFixtureA().getBody().getUserData() instanceof Asteroid))
		{
			// Make sure the ship is active
			if (m_Ship.isActive())
			{
				// Ship collided with an Asteroid
				System.out.println("Ship collision!");
				synchronized (m_CollisionQueueLock)
				{
					m_CollisionQueue.add((BaseEntity)contact.getFixtureA().getBody().getUserData());
					m_CollisionQueue.add((BaseEntity)contact.getFixtureB().getBody().getUserData());
				}
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
			synchronized (m_CollisionQueueLock)
			{
				m_CollisionQueue.add((BaseEntity)contact.getFixtureA().getBody().getUserData());
				m_CollisionQueue.add((BaseEntity)contact.getFixtureB().getBody().getUserData());
			}
		}
	}

	/**
	 * Destroy an asteroid
	 * @param asteroid - Asteroid to destroy
	 */
	private void destroyAsteroid(Asteroid asteroid)
	{
		m_GameData.addScore(asteroid.getValue());
		removeAsteroid(asteroid);
		asteroid.destroy();
	}

	/**
	 * Remove an asteroid from the world
	 * @param asteroid - Asteroid to be removed from the world
	 */
	private void removeAsteroid(Asteroid asteroid)
	{
		// Sift through the Asteroids
		for(int i = 0; i < m_Asteroids.size(); i++)
		{
			// Check the IDs to see if they match
			if (m_Asteroids.get(i).getID() == asteroid.getID())
			{
				// Remove the asteroid
				m_Asteroids.remove(i);
				break;
			}
		}
	}

	/**
	 * Add an asteroid to the list
	 * @param asteroid - Asteroid to add to the list
	 */
	public void addAsteroid(Asteroid asteroid)
	{
		m_Asteroids.add(asteroid);
	}

	/**
	 * Reset the game
	 */
	public void resetGame()
	{
		// Make sure the game is over
		if (m_GameOver)
		{
			// Clear the Asteroids
			for(Asteroid asteroid: m_Asteroids)
				m_World.destroyBody(asteroid.getBody());
			m_Asteroids.clear();

			// Generate the starting asteroids
			generateNewAsteroid();
			generateNewAsteroid();
			generateNewAsteroid();
			generateNewAsteroid();

			// Reset the game data
			m_GameData.reset();

			// Reset the flag
			m_GameOver = false;
		}
	}

	/**
	 * Generate and add a new asteroid
	 */
	private void generateNewAsteroid()
	{
		// Randomly select an edge
		int edge = new Random().nextInt(4);
		float x = 0.0f, y = 0.0f;
		switch(edge)
		{
			// TOP
			case 0:
				// Randomize X
				x = (int)(Math.random() * AsteroidEngine.WIDTH);
				// y = MAX
				y = AsteroidEngine.HEIGHT;
				break;
			// BOTTOM
			case 1:
				// Randomize X
				x = (int)(Math.random() * AsteroidEngine.WIDTH);
				// y = MIN
				y = 0;
				break;
			// LEFT
			case 2:
				// Randomize Y
				y = (int)(Math.random() * AsteroidEngine.HEIGHT);
				// x = MIN
				x = 0;
				break;
			// RIGHT
			case 3:
				// Randomize Y
				y = (int)(Math.random() * AsteroidEngine.HEIGHT);
				// x = MAX
				x = AsteroidEngine.WIDTH;
				break;
		}
		// Create and add the new asteroid
		m_Asteroids.add(new Asteroid(new Vector2(x, y), Asteroid.AsteroidSize.Large, m_World));
	}

	/**
	 * Is the game over?
	 * @return True if the Game is over, False otherwise.
	 */
	public boolean isGameOver()
	{
		return m_GameOver;
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
