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
	private Ship m_Ship;

	public InputHandler(Ship ship)
	{
		// Save the ship for setting the accelerations
		m_Ship = ship;
	}

	@Override
	public boolean keyDown(int keycode)
	{
		switch(keycode)
		{
			case Input.Keys.UP:
				m_Ship.setMoving(true);
				break;
			case Input.Keys.SPACE:
				m_Ship.fire();
				break;
			case Input.Keys.RIGHT:
				m_Ship.setRotatingRight(true);
				break;
			case Input.Keys.LEFT:
				m_Ship.setRotatingLeft(true);
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
				m_Ship.setMoving(false);
				break;
			case Input.Keys.RIGHT:
				m_Ship.setRotatingRight(false);
				break;
			case Input.Keys.LEFT:
				m_Ship.setRotatingLeft(false);
				break;
		}
		return true;
	}

	@Override
	public boolean keyTyped(char character)
	{
		return false;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button)
	{
		return false;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button)
	{
		return false;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer)
	{
		return false;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY)
	{
		return false;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public boolean scrolled(int amount)
	{
		return false;  //To change body of implemented methods use File | Settings | File Templates.
	}
}
