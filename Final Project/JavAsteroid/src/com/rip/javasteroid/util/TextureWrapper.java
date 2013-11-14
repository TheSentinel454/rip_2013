package com.rip.javasteroid.util;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

/**
 * Created with IntelliJ IDEA.
 * User: Luke
 * Date: 11/9/13
 * Time: 10:56 PM
 */
public class TextureWrapper
{
	private Texture         m_Texture;
	private int             m_Width;
	private int             m_Height;
	private Vector2         m_Position;
	private float           m_ScaleX;
	private float           m_ScaleY;
	private float           m_OriginX;
	private float           m_OriginY;
	private float           m_Rotation;

	/**
	 *
	 * @param texture
	 * @param pos
	 */
	public TextureWrapper(Texture texture, Vector2 pos)
	{
		this.m_Position = pos;
		setTexture(texture);
	}

	/**
	 *
	 * @param texture
	 */
	public void setTexture(Texture texture)
	{
		this.m_Texture = texture;
		m_Width = texture.getWidth();
		m_Height = texture.getHeight();
		m_OriginX = m_Width / 2;
		m_OriginY = m_Height / 2;
		m_ScaleX = 1;
		m_ScaleY = 1;
	}

	/**
	 *
	 * @return
	 */
	public int getWidth()
	{
		return m_Width;
	}

	/**
	 *
	 * @return
	 */
	public int getHeight()
	{
		return m_Height;
	}

	/**
	 *
	 * @param x
	 * @param y
	 */
	public void setPosition(float x, float y)
	{
		m_Position.set(x, y);
	}

	/**
	 *
	 * @param vector2
	 */
	public void setPosition(Vector2 vector2)
	{
		m_Position.set(vector2);
	}

	/**
	 *
	 * @param r
	 */
	public void setRotation(float r)
	{
		m_Rotation = r;
	}

	/**
	 *
	 * @param sp
	 */
	public void draw(SpriteBatch sp)
	{
		sp.draw(m_Texture, m_Position.x - m_Width / 2, m_Position.y - m_Height / 2,
				m_OriginX, m_OriginY, m_Width, m_Height,
				m_ScaleX, m_ScaleY, m_Rotation, 0, 0, m_Width, m_Height, false, false);
	}
}
