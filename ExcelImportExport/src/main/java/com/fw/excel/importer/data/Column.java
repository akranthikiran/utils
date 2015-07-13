package com.fw.excel.importer.data;

public class Column
{
	private String name;
	private ColumnType type;
	private Class<?> javaType;

	public Column(String name, ColumnType type, Class<?> javaType)
	{
		if(name == null || name.trim().length() == 0)
		{
			throw new NullPointerException("Name can not be null or empty");
		}
		
		if(type == null)
		{
			throw new NullPointerException("Type can not be null");
		}
		
		this.name = name;
		this.type = type;
		this.javaType = javaType;
	}

	public String getName()
	{
		return name;
	}

	public ColumnType getType()
	{
		return type;
	}

	public Class<?> getJavaType()
	{
		return javaType;
	}
}
