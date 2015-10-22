package com.fw.persistence.query;

/**
 * Represents a join condition in condition based query
 * 
 * @author akiran
 */
public class QueryJoinCondition
{
	/**
	 * Left side table to be used for join condition
	 */
	private String leftTableCode;

	/**
	 * Left side table column to be used for join condition
	 */
	private String leftColumn;

	/**
	 * Right side table code for join condition
	 */
	private String rightTableCode;

	/**
	 * Right side table column for join condition
	 */
	private String rightColumn;

	public QueryJoinCondition(String leftTableCode, String leftColumn, String rightTableCode, String rightColumn)
	{
		this.leftTableCode = leftTableCode;
		this.leftColumn = leftColumn;
		this.rightTableCode = rightTableCode;
		this.rightColumn = rightColumn;
	}

	/**
	 * @return the {@link #leftTableCode leftTableCode}
	 */
	public String getLeftTableCode()
	{
		return leftTableCode;
	}

	/**
	 * @param leftTableCode
	 *            the {@link #leftTableCode leftTableCode} to set
	 */
	public void setLeftTableCode(String leftTableCode)
	{
		this.leftTableCode = leftTableCode;
	}

	/**
	 * @return the {@link #leftColumn leftColumn}
	 */
	public String getLeftColumn()
	{
		return leftColumn;
	}

	/**
	 * @param leftColumn
	 *            the {@link #leftColumn leftColumn} to set
	 */
	public void setLeftColumn(String leftColumn)
	{
		this.leftColumn = leftColumn;
	}

	/**
	 * @return the {@link #rightTableCode rightTableCode}
	 */
	public String getRightTableCode()
	{
		return rightTableCode;
	}

	/**
	 * @param rightTableCode
	 *            the {@link #rightTableCode rightTableCode} to set
	 */
	public void setRightTableCode(String rightTableCode)
	{
		this.rightTableCode = rightTableCode;
	}

	/**
	 * @return the {@link #rightColumn rightColumn}
	 */
	public String getRightColumn()
	{
		return rightColumn;
	}

	/**
	 * @param rightColumn
	 *            the {@link #rightColumn rightColumn} to set
	 */
	public void setRightColumn(String rightColumn)
	{
		this.rightColumn = rightColumn;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder("[");

		builder.append(leftTableCode).append(".").append(leftColumn);
		builder.append(" = ").append(rightTableCode).append(rightColumn);

		return builder.toString();
	}
}
