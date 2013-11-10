package com.rip.javasteroid.util;

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
	TextureRegion region;
	int width;
	int height;
	Vector2 position;
	float scaleX;
	float scaleY;
	float originX;
	float originY;
	float rotation;

	/**
	 *
	 * @param region
	 * @param pos
	 */
	public TextureWrapper(TextureRegion region, Vector2 pos)
	{
		this.position = pos;
		SetTextureRegion(region);
	}

	/**
	 *
	 * @param region
	 */
	public void SetTextureRegion(TextureRegion region)
	{
		this.region = region;
		width = region.getRegionWidth();
		height = region.getRegionHeight();
		originX = width / 2;
		originY = height / 2;
		scaleX = 1;
		scaleY = 1;
	}

	/**
	 *
	 * @return
	 */
	public int GetWidth()
	{
		return width;
	}

	/**
	 *
	 * @return
	 */
	public int GetHeight()
	{
		return height;
	}

	/**
	 *
	 * @param x
	 * @param y
	 */
	public void SetPosition(float x, float y)
	{
		position.set(x, y);
	}

	/**
	 *
	 * @param vector2
	 */
	public void SetPosition(Vector2 vector2)
	{
		position.set(vector2);
	}

	/**
	 *
	 * @param r
	 */
	public void SetRotation(float r)
	{
		rotation = r;
	}

	/**
	 *
	 * @param sp
	 */
	public void Draw(SpriteBatch sp)
	{
		sp.draw(region, position.x - width / 2, position.y - height / 2,
				originX, originY, width, height,
				scaleX, scaleY, rotation);
	}
}
