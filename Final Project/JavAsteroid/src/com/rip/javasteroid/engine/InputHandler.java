package com.rip.javasteroid.engine;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.rip.javasteroid.JavAsteroid;
import com.rip.javasteroid.entity.Ship;

/**
 * Created with IntelliJ IDEA.
 * User: Luke
 * Date: 11/11/13
 * Time: 9:47 PM
 */
public class InputHandler implements InputProcessor
{
	/* Private Attributes */
	private Ship m_Ship;

	/**
	 * Input Handler constructor
	 * @param ship - Ship to use for handing the operations to
	 */
	public InputHandler(Ship ship)
	{
		// Save the ship for setting the accelerations
		m_Ship = ship;
	}

	public void startForward()
	{
		if (!JavAsteroid.getEngine().isGameOver())
			m_Ship.setMoving(true);
	}
	public void stopForward()
	{
		if (!JavAsteroid.getEngine().isGameOver())
			m_Ship.setMoving(false);
	}
	public void startRight()
	{
		if (!JavAsteroid.getEngine().isGameOver())
			m_Ship.setRotatingRight(true);
	}
	public void stopRight()
	{
		if (!JavAsteroid.getEngine().isGameOver())
			m_Ship.setRotatingRight(false);
	}
	public void startLeft()
	{
		if (!JavAsteroid.getEngine().isGameOver())
			m_Ship.setRotatingLeft(true);
	}
	public void stopLeft()
	{
		if (!JavAsteroid.getEngine().isGameOver())
			m_Ship.setRotatingLeft(false);
	}
	public void fire()
	{
		if (!JavAsteroid.getEngine().isGameOver())
			m_Ship.fire();
	}
	public void reset()
	{
		// Reset the game
		JavAsteroid.getEngine().resetGame();
	}
	public void exit()
	{
		// Kill the application
		System.exit(0);
	}

	@Override
	public boolean keyDown(int keycode)
	{
		switch(keycode)
		{
			case Input.Keys.UP:
			case Input.Keys.W:
				startForward();
				break;
			case Input.Keys.SPACE:
				fire();
				break;
			case Input.Keys.RIGHT:
			case Input.Keys.D:
				startRight();
				break;
			case Input.Keys.LEFT:
			case Input.Keys.A:
				startLeft();
				break;
			case Input.Keys.R:
				reset();
				break;
			case Input.Keys.Q:
			case Input.Keys.ESCAPE:
				exit();
				break;
		}
		return true;
	}

	@Override
	public boolean keyUp(int keycode)
	{
		switch(keycode)
		{
			case Input.Keys.UP:
			case Input.Keys.W:
				stopForward();
				break;
			case Input.Keys.RIGHT:
			case Input.Keys.D:
				stopRight();
				break;
			case Input.Keys.LEFT:
			case Input.Keys.A:
				stopLeft();
				break;
		}
		return true;
	}

	@Override
	public boolean keyTyped(char character)
	{
		return false;
	}
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button)
	{
		return false;
	}
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button)
	{
		return false;
	}
	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer)
	{
		return false;
	}
	@Override
	public boolean mouseMoved(int screenX, int screenY)
	{
		return false;
	}
	@Override
	public boolean scrolled(int amount)
	{
		return false;
	}
}
