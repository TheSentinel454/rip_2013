package com.planner.machinelearning;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: Luke
 * Date: 12/7/13
 * Time: 10:11 PM
 */
public class RangeValue implements Serializable
{
	/* Private Attributes */
	private String m_Name;
	private float m_MinimumValue;
	private float m_MaximumValue;

	/* Public Accessors */
	public String getName()
	{
		return m_Name;
	}
	public float getMinimumValue()
	{
		return m_MinimumValue;
	}
	public float getMaximumValue()
	{
		return m_MaximumValue;
	}
	public boolean inRange(float value)
	{
		return (value >= m_MinimumValue && value < m_MaximumValue);
	}

	/**
	 * Basic constructor for the Boolean Value
	 * @param name - Name
	 * @param minimumValue - Minimum Value
	 * @param maximumValue - Maximum Value
	 */
	public RangeValue(String name, float minimumValue, float maximumValue)
	{
		m_Name = name;
		m_MaximumValue = maximumValue;
		m_MinimumValue = minimumValue;
	}

	@Override
	public String toString()
	{
		return m_Name + ": " + Float.toString(m_MinimumValue) + "->" + Float.toString(m_MaximumValue);
	}
}
