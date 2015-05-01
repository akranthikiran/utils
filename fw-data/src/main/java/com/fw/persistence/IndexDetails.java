package com.fw.persistence;

import java.util.Arrays;

public class IndexDetails
{
	private String name;
	private String fields[];

	public IndexDetails(String name, String[] fields)
	{
		this.name = name;
		this.fields = fields;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String[] getFields()
	{
		return fields;
	}

	public void setFields(String[] fields)
	{
		this.fields = fields;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder(super.toString());
		builder.append("[");

		builder.append("Name: ").append(name);
		builder.append(",").append("Fields: ").append(Arrays.toString(fields));

		builder.append("]");
		return builder.toString();
	}
}
