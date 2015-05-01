package com.fw.persistence.query;

public class ColumnParam
{
	private String name;
	private Object value;
	private int index;
	private String sequence;
	
	public ColumnParam(String name, Object value, int index, String sequence)
	{
		this.name = name;
		this.value = value;
		this.index = index;
		this.sequence = sequence;
	}

	public ColumnParam(String name, Object value, int index)
	{
		this(name, value, index, null);
	}
	
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public Object getValue()
	{
		return value;
	}

	public void setValue(Object value)
	{
		this.value = value;
	}
	
	public int getIndex()
	{
		return index;
	}
	
	public boolean isSequenceGenerated()
	{
		return (sequence != null);
	}
	
	public String getSequence()
	{
		return sequence;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder(getClass().getName());
		builder.append("[");
		
		builder.append(name).append(" = ").append(value);

		builder.append("]");
		return builder.toString();
	}
}
