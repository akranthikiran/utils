package com.fw.persistence.query;

import com.fw.persistence.Operator;

public class ConditionParam
{
	private String column;
	private Operator operator = Operator.EQ;
	private Object value;
	private int index;
	
	public ConditionParam(String column, Operator operator, Object value, int index)
	{
		this.column = column;
		this.operator = operator;
		this.value = value;
		this.index = index;
	}

	public ConditionParam(String column, Object value, int index)
	{
		this(column, Operator.EQ, value, index);
	}

	public String getColumn()
	{
		return column;
	}

	public Operator getOperator()
	{
		return operator;
	}
	
	public void setValue(Object value)
	{
		this.value = value;
	}

	public Object getValue()
	{
		return value;
	}
	
	public int getIndex()
	{
		return index;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("[");

		builder.append(column).append(" ").append(operator).append(" ").append(value);

		builder.append("]");
		return builder.toString();
	}
}
