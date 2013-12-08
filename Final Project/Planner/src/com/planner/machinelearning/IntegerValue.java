package com.planner.machinelearning;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: Luke
 * Date: 12/7/13
 * Time: 10:11 PM
 */
public class IntegerValue implements Serializable
{
	/* Private Attributes */
	private String m_Name;
	private int m_Value;

	/* Public Accessors */
	public String getName()
	{
		return m_Name;
	}
	public int getValue()
	{
		return m_Value;
	}
	public void setValue(int value)
	{
		m_Value = value;
	}

	/**
	 * Basic constructor for the Boolean Value
	 * @param name - Name
	 * @param value - Value
	 */
	public IntegerValue(String name, int value)
	{
		m_Name = name;
		m_Value = value;
	}

	@Override
	public String toString()
	{
		return m_Name + ": " + Integer.toString(m_Value);
	}
}
