package com.fw.persistence.repository.search;

import com.fw.persistence.Operator;

/**
 * Dynamic condition for search query
 * @author akiran
 */
public class SearchCondition
{
	/**
	 * Entity field expression
	 */
	private String field;
	
	/**
	 * Condition operator
	 */
	private Operator operator;
	
	/**
	 * Value for condition comparison
	 */
	private Object value;
	
	public SearchCondition()
	{}

	public SearchCondition(String field, Operator operator, Object value)
	{
		this.field = field;
		this.operator = operator;
		this.value = value;
	}

	/**
	 * @return the {@link #field field}
	 */
	public String getField()
	{
		return field;
	}

	/**
	 * @param field the {@link #field field} to set
	 */
	public void setField(String field)
	{
		this.field = field;
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
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder(super.toString());
		builder.append("[");

		builder.append(field).append(" ").append(operator).append(" ").append(value);

		builder.append("]");
		return builder.toString();
	}
}
