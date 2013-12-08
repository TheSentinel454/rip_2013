package com.planner.machinelearning;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: Luke
 * Date: 12/7/13
 * Time: 8:44 PM
 */
public class LeafData implements Serializable
{
	/* Private Attributes */
	private float m_Ratio = 0;
	private int m_Trains = 0;
	private int m_Successes = 0;

	/* Public Accessors */
	public float getProbability()
	{
		return (m_Trains == 0 ? 0.0f : (m_Successes / (m_Trains * 1.0f)));
	}
	public float getRatio()
	{
		return m_Ratio;
	}
	public boolean getPrediction()
	{
		return (getProbability() >= 0.5f);
	}

	/**
	 * Basic constructor for the Leaf Data
	 * @param ratio - Ratio of data hitting this leaf
	 * @param hits -
	 * @param successes - Successful trains using this LeafData
	 */
	public LeafData(float ratio, int hits, int successes)
	{
		m_Ratio = ratio;
		m_Trains = hits;
		m_Successes = successes;
	}

	/**
	 * Train this Leaf Data
	 * @param success True if successful, False otherwise
	 * @param totalTrains Total trains in the decision tree
	 */
	public void train(boolean success, int totalTrains)
	{
		// Increment the train count
		m_Trains++;
		// Increment the success count if successful
		if (success)
			m_Successes++;
		// Recalculate the ratio
		m_Ratio = (m_Trains / (totalTrains * 1.0f));
	}

	@Override
	public String toString()
	{
		return getPrediction() + ": " + Float.toString(m_Ratio * 100f) + "% with Pr[" + Float.toString(getProbability()) + "]";
	}
}
