package com.rip.javasteroid.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

/**
 * Created with IntelliJ IDEA.
 * User: Luke
 * Date: 11/9/13
 * Time: 10:56 PM
 */
public class TextureWrapper
{
	/* Private Attributes */
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
	 * Texture wrapper constructor
	 * @param texture - Texture to be used
	 * @param pos - Initial position of the texture
	 */
	public TextureWrapper(Texture texture, Vector2 pos)
	{
		this.m_Position = pos;
		setTexture(texture);
	}

	/**
	 * Set the Texture to be used when drawing
	 * @param texture - Texture to be used for drawing
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
	 * Set the position of the texture
	 * @param vector2 - Position
	 */
	public void setPosition(Vector2 vector2)
	{
		m_Position.set(vector2);
	}

	/**
	 * Set the rotation of the texture
	 * @param r - Rotation
	 */
	public void setRotation(float r)
	{
		m_Rotation = r;
	}

	/**
	 * Draw the texture
	 * @param sp - SpriteBatch to use for drawing
	 */
	public void draw(SpriteBatch sp)
	{
		sp.draw(m_Texture, m_Position.x - m_Width / 2, m_Position.y - m_Height / 2,
				m_OriginX, m_OriginY, m_Width, m_Height,
				m_ScaleX, m_ScaleY, m_Rotation, 0, 0, m_Width, m_Height, false, false);
	}

	/**
	 * Draw the texture
	 * @param sp - SpriteBatch to use for drawing
	 * @param alpha - Alpha of the texture
	 */
	public void draw(SpriteBatch sp, float alpha)
	{
		Color c = sp.getColor();
		sp.setColor(c.r, c.g, c.b, alpha);
		sp.draw(m_Texture, m_Position.x - m_Width / 2, m_Position.y - m_Height / 2,
				m_OriginX, m_OriginY, m_Width, m_Height,
				m_ScaleX, m_ScaleY, m_Rotation, 0, 0, m_Width, m_Height, false, false);
		sp.setColor(c.r, c.g, c.b, c.a);
	}

	/**
	 * Dispose any resources
	 */
	public void dispose()
	{
		if (m_Texture != null)
		{
			m_Texture.dispose();
			m_Texture = null;
		}
	}
}
