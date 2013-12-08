package com.planner.machinelearning;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: Luke
 * Date: 12/7/13
 * Time: 8:44 PM
 */
public class TreeData implements Serializable
{
	/* Private Attributes */
	private float m_Probability = 0;
	private float m_Ratio = 0;
	private boolean m_Result = false;

	/* Public Accessors */
	public float getProbability()
	{
		return m_Probability;
	}
	public float getRatio()
	{
		return m_Ratio;
	}
	public boolean getResult()
	{
		return m_Result;
	}

	/**
	 * Basic constructor for the Leaf Data
	 * @param result - Result for this leaf
	 * @param ratio - Ratio of data hitting this leaf
	 * @param probability - Probability of success
	 */
	public TreeData(boolean result, float ratio, float probability)
	{
		m_Result = result;
		m_Ratio = ratio;
		m_Probability = probability;
	}

	@Override
	public String toString()
	{
		return m_Result + ": " + Float.toString(m_Ratio * 100f) + "% with Pr[" + Float.toString(m_Probability) + "]";
	}
}
