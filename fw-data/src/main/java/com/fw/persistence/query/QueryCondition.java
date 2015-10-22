package com.fw.persistence.query;

import com.fw.persistence.Operator;

/**
 * Represents a condition in condition based query
 * @author akiran
 */
public class QueryCondition implements Cloneable
{
	/**
	 * Table (code) to be used for this condition
	 */
	private String tableCode;
	
	/**
	 * Column to be used for this condition
	 */
	private String column;
	
	/**
	 * Condition operator
	 */
	private Operator operator = Operator.EQ;
	
	/**
	 * Value for the condition
	 */
	private Object value;
	
	public QueryCondition(String tableCode, String column, Operator operator, Object value)
	{
		this.tableCode = tableCode;
		this.column = column;
		this.operator = operator;
		this.value = value;
	}

	/**
	 * @return the {@link #tableCode tableCode}
	 */
	public String getTableCode()
	{
		return tableCode;
	}

	/**
	 * @param tableCode the {@link #tableCode tableCode} to set
	 */
	public void setTableCode(String tableCode)
	{
		this.tableCode = tableCode;
	}

	/**
	 * @return the {@link #column column}
	 */
	public String getColumn()
	{
		return column;
	}

	/**
	 * @param column the {@link #column column} to set
	 */
	public void setColumn(String column)
	{
		this.column = column;
	}

	/**
	 * @return the {@link #operator operator}
	 */
	public Operator getOperator()
	{
		return operator;
	}

	/**
	 * @param operator the {@link #operator operator} to set
	 */
	public void setOperator(Operator operator)
	{
		this.operator = operator;
	}

	/**
	 * @return the {@link #value value}
	 */
	public Object getValue()
	{
		return value;
	}

	/**
	 * @param value the {@link #value value} to set
	 */
	public void setValue(Object value)
	{
		this.value = value;
	}
	
	@Override
	public QueryCondition clone()
	{
		try
		{
			return (QueryCondition)super.clone();
		} catch(CloneNotSupportedException ex)
		{
			throw new IllegalStateException("An error occurred while cloding query condition", ex);
		}
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder("[");

		builder.append(tableCode).append(".").append(column).append(" ");
		builder.append(operator).append(" ").append(value);

		return builder.toString();
	}
}
