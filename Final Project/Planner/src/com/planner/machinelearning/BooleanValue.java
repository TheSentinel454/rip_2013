package com.planner.machinelearning;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: Luke
 * Date: 12/7/13
 * Time: 10:11 PM
 */
public class BooleanValue implements Serializable
{
	/* Private Attributes */
	private String m_Name;
	private boolean m_Value;

	/* Public Accessors */
	public String getName()
	{
		return m_Name;
	}
	public boolean getValue()
	{
		return m_Value;
	}

	/**
	 * Basic constructor for the Boolean Value
	 * @param name - Name
	 * @param value - Value
	 */
	public BooleanValue(String name, boolean value)
	{
		m_Name = name;
		m_Value = value;
	}

	@Override
	public String toString()
	{
		return m_Name + ": " + Boolean.toString(m_Value);
	}
}
