package com.rip.javasteroid;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.rip.javasteroid.engine.AsteroidEngine;

public class JavAsteroid extends Game
{
	public static AsteroidEngine m_AsteroidGame;
	public static AsteroidEngine getEngine()
	{
		return m_AsteroidGame;
	}

	@Override
	public void create()
	{
		m_AsteroidGame = new AsteroidEngine();
		this.setScreen(m_AsteroidGame);
	}

	@Override
	public void dispose()
	{
		m_AsteroidGame.dispose();
	}

	@Override
	public void render()
	{
		super.render();
	}

	@Override
	public void resize(int width, int height)
	{
		super.resize(width, height);
	}

	@Override
	public void pause()
	{
		super.pause();
	}

	@Override
	public void resume()
	{
		super.resume();
	}
}
