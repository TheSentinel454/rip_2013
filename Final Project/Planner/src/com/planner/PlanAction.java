package com.planner;

/**
 * Created with IntelliJ IDEA.
 * User: luketornquist
 * Date: 11/30/13
 * Time: 12:16 PM
 */
public class PlanAction implements Comparable<PlanAction>
{
	@Override
	public int compareTo(PlanAction o)
	{
		return (int)Math.signum(this.m_Time - o.getTime());
	}

	/* Enum */
	public enum Action
	{
		startForward,
		stopForward,
		startRight,
		stopRight,
		startLeft,
		stopLeft,
		fire;
	};

	/* Private Attributes */
	private long	m_Time;
	private Action	m_Action;

	/**
	 * Get the time at which we need to execute this action
	 * @return System.currentTimeMillis() to execute this action
	 */
	public long getTime()
	{
		return m_Time;
	}

	/**
	 * Get the action to be executed
	 * @return Action to be executed
	 */
	public Action getAction()
	{
		return m_Action;
	}

	/**
	 * Construct a Plan Action
	 * @param time - Time to execute the action
	 * @param action - Action to be executed
	 */
	public PlanAction(long time, Action action)
	{
		m_Time = time;
		m_Action = action;
	}

    public void shiftTime(long diff) {
        m_Time += diff;
    }
}
