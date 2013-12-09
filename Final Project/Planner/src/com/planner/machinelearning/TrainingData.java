package com.planner.machinelearning;

/**
 * Created with IntelliJ IDEA.
 * User: Luke
 * Date: 12/8/13
 * Time: 7:35 PM
 */
public class TrainingData implements Comparable<TrainingData>
{
	/* Constants */
	private static int EVALUATION_DELAY = 2000; // 500 ms before evaluating outcome

	/* Private Attributes */
	private long            m_Timestamp;
	private SearchCriteria  m_SearchCriteria;
	private float           m_SafePercentage;
	private boolean         m_Prediction;
	private int             m_AsteroidID;
	private int             m_Score;
	private int             m_Lives;

	public boolean isTimeUp()
	{
		return (System.currentTimeMillis() >= (m_Timestamp + EVALUATION_DELAY));
	}

	public int getLives()
	{
		return m_Lives;
	}

	public SearchCriteria getSearchCriteria()
	{
		return m_SearchCriteria;
	}

	public float getSafePercentage()
	{
		return m_SafePercentage;
	}

	public boolean getPrediction()
	{
		return m_Prediction;
	}

	public int getAsteroidID()
	{
		return m_AsteroidID;
	}

	public int getScore()
	{
		return m_Score;
	}

	@Override
	public int compareTo(TrainingData o)
	{
		return (int)Math.signum(this.m_Timestamp - o.m_Timestamp);
	}

	public TrainingData(long timestamp, SearchCriteria searchCriteria, float safePercentage, boolean prediction, int asteroidID, int score, int lives)
	{
		this.m_Timestamp = timestamp;
		this.m_SearchCriteria = searchCriteria;
		this.m_SafePercentage = safePercentage;
		this.m_Prediction = prediction;
		this.m_AsteroidID = asteroidID;
		this.m_Score = score;
		this.m_Lives = lives;
	}
}
