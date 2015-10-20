package com.fw.persistence;

/**
 * Dynamic entity representing join table entries
 * @author akiran
 */
public class JoinTableEntity
{
	public static final String FIELD_JOIN_COLUMN = "joinColumn";
	public static final String FIELD_INV_JOIN_COLUMN = "inverseJoinColumn";
	
	private Object joinColumn;
	private Object inverseJoinColumn;

	public JoinTableEntity(Object joinColumn, Object inverseJoinColumn)
	{
		this.joinColumn = joinColumn;
		this.inverseJoinColumn = inverseJoinColumn;
	}

	/**
	 * @return the {@link #joinColumn joinColumn}
	 */
	public Object getJoinColumn()
	{
		return joinColumn;
	}

	/**
	 * @param joinColumn
	 *            the {@link #joinColumn joinColumn} to set
	 */
	public void setJoinColumn(Object joinColumn)
	{
		this.joinColumn = joinColumn;
	}

	/**
	 * @return the {@link #inverseJoinColumn inverseJoinColumn}
	 */
	public Object getInverseJoinColumn()
	{
		return inverseJoinColumn;
	}

	/**
	 * @param inverseJoinColumn
	 *            the {@link #inverseJoinColumn inverseJoinColumn} to set
	 */
	public void setInverseJoinColumn(Object inverseJoinColumn)
	{
		this.inverseJoinColumn = inverseJoinColumn;
	}

}
