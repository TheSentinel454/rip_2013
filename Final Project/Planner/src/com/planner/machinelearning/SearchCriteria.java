package com.planner.machinelearning;

/**
 * Created with IntelliJ IDEA.
 * User: Luke
 * Date: 12/8/13
 * Time: 7:39 PM
 */
public class SearchCriteria
{
	private boolean m_RelativeLocation;
	private float m_Distance;
	private float m_TimeTillImpact;
	private float m_Angle;

	public float getAngle()
	{
		return m_Angle;
	}

	public float getTimeTillImpact()
	{
		return m_TimeTillImpact;
	}

	public float getDistance()
	{
		return m_Distance;
	}

	public boolean isRelativeLocation()
	{
		return m_RelativeLocation;
	}

	public SearchCriteria(boolean relativeLocation, float distance, float timeTillImpact, float angle)
	{
		m_RelativeLocation = relativeLocation;
		m_Distance = distance;
		m_TimeTillImpact = timeTillImpact;
		m_Angle = angle;
	}
}
