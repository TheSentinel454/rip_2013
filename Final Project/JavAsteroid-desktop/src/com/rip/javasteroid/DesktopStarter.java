package com.rip.javasteroid;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.rip.javasteroid.engine.AsteroidEngine;

public class DesktopStarter
{
	public static void main(String[] args)
	{
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "JavAsteroid";
		cfg.useGL20 = true;
		cfg.width = AsteroidEngine.WIDTH;
		cfg.height = AsteroidEngine.HEIGHT;
		
		new LwjglApplication(new JavAsteroid(), cfg);
	}
}
