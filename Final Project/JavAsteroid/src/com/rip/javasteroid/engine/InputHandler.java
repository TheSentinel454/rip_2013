package com.rip.javasteroid.engine;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
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
		m_Ship.setMoving(true);
	}
	public void stopForward()
	{
		m_Ship.setMoving(false);
	}
	public void startRight()
	{
		m_Ship.setRotatingRight(true);
	}
	public void stopRight()
	{
		m_Ship.setRotatingRight(false);
	}
	public void startLeft()
	{
		m_Ship.setRotatingLeft(true);
	}
	public void stopLeft()
	{
		m_Ship.setRotatingLeft(false);
	}
	public void fire()
	{
		m_Ship.fire();
	}

	@Override
	public boolean keyDown(int keycode)
	{
		switch(keycode)
		{
			case Input.Keys.UP:
				startForward();
				break;
			case Input.Keys.SPACE:
				fire();
				break;
			case Input.Keys.RIGHT:
				startRight();
				break;
			case Input.Keys.LEFT:
				startLeft();
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
				stopForward();
				break;
			case Input.Keys.RIGHT:
				stopRight();
				break;
			case Input.Keys.LEFT:
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
