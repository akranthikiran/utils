package com.fw.persistence;

import java.util.Date;
import java.util.HashMap;

public class Record
{
	private Object data[];
	private HashMap<String, Object> nameToVal = new HashMap<String, Object>();
	private String columnNames[];

	public Record(int len)
	{
		data = new Object[len];
		columnNames = new String[len];
	}

	public void set(int idx, String name, Object value)
	{
		data[idx] = value;
		columnNames[idx] = name;
		
		nameToVal.put(name, value);
	}
	
	public Object[] getValues()
	{
		return data;
	}
	
	public String[] getColumnNames()
	{
		return columnNames;
	}
	
	public String getColumn(int idx)
	{
		return columnNames[idx];
	}

	public Object getObject(int idx)
	{
		return data[idx];
	}

	public String getString(int idx)
	{
		return (String)data[idx];
	}

	public Date getDate(int idx)
	{
		return (Date)data[idx];
	}

	public byte getByte(int idx)
	{
		if(data[idx] == null)
			return 0;

		if(!(data[idx] instanceof Number))
			throw new IllegalArgumentException("Can not convert to int from: " + data.getClass().getName() + ". Idx: " + idx);

		return ((Number)data[idx]).byteValue();
	}

	public boolean getBoolean(int idx)
	{
		if(data[idx] == null)
			return false;
		return (Boolean)data[idx];
	}

	public char getChar(int idx)
	{
		if(data[idx] == null)
			return 0;

		return (Character)data[idx];
	}

	public short getShort(int idx)
	{
		if(data[idx] == null)
			return 0;

		if(!(data[idx] instanceof Number))
			throw new IllegalArgumentException("Can not convert to int from: " + data.getClass().getName() + ". Idx: " + idx);

		return ((Number)data[idx]).shortValue();
	}

	public int getInt(int idx)
	{
		if(data[idx] == null)
			return 0;

		if(!(data[idx] instanceof Number))
			throw new IllegalArgumentException("Can not convert to int from: " + data.getClass().getName() + ". Idx: " + idx);

		return ((Number)data[idx]).intValue();
	}

	public long getLong(int idx)
	{
		if(data[idx] == null)
			return 0l;

		if(!(data[idx] instanceof Number))
			throw new IllegalArgumentException("Can not convert to int from: " + data.getClass().getName() + ". Idx: " + idx);

		return ((Number)data[idx]).longValue();
	}

	public float getFloat(int idx)
	{
		if(data[idx] == null)
			return 0f;

		if(!(data[idx] instanceof Number))
			throw new IllegalArgumentException("Can not convert to int from: " + data.getClass().getName() + ". Idx: " + idx);

		return ((Number)data[idx]).floatValue();
	}

	public double getDouble(int idx)
	{
		if(data[idx] == null)
			return 0.0;

		if(!(data[idx] instanceof Number))
			throw new IllegalArgumentException("Can not convert to int from: " + data.getClass().getName() + ". Idx: " + idx);

		return ((Number)data[idx]).doubleValue();
	}

	public Object getObject(String name)
	{
		return nameToVal.get(name);
	}

	public String getString(String name)
	{
		Object o = nameToVal.get(name);

		if(o == null)
			return null;

		return o.toString();
	}

	public byte getByte(String name)
	{
		Object data = getObject(name);

		if(data == null)
			return 0;

		if(!(data instanceof Number))
			throw new IllegalArgumentException("Can not convert to byte from: " + data.getClass().getName() + ". Name: " + name);

		return ((Number)data).byteValue();
	}

	public boolean getBoolean(String name)
	{
		Object data = getObject(name);

		if(data == null)
			return false;

		return (Boolean)data;
	}

	public char getChar(String name)
	{
		Object data = getObject(name);

		if(data == null)
			return 0;

		return (Character)data;
	}

	public short getShort(String name)
	{
		Object data = getObject(name);

		if(data == null)
			return 0;

		if(!(data instanceof Number))
			throw new IllegalArgumentException("Can not convert to short from: " + data.getClass().getName() + ". Name: " + name);

		return ((Number)data).shortValue();
	}

	public int getInt(String name)
	{
		Object data = getObject(name);

		if(data == null)
			return 0;

		if(!(data instanceof Number))
			throw new IllegalArgumentException("Can not convert to int from: " + data.getClass().getName() + ". Name: " + name);

		return ((Number)data).intValue();
	}

	public long getLong(String name)
	{
		Object data = getObject(name);

		if(data == null)
			return 0l;

		if(!(data instanceof Number))
			throw new IllegalArgumentException("Can not convert to long from: " + data.getClass().getName() + ". Name: " + name);

		return ((Number)data).longValue();
	}

	public float getFloat(String name)
	{
		Object data = getObject(name);

		if(data == null)
			return 0f;

		if(!(data instanceof Number))
			throw new IllegalArgumentException("Can not convert to float from: " + data.getClass().getName() + ". Name: " + name);

		return ((Number)data).floatValue();
	}

	public double getDouble(String name)
	{
		Object data = getObject(name);

		if(data == null)
			return 0.0;

		if(!(data instanceof Number))
			throw new IllegalArgumentException("Can not convert to double from: " + data.getClass().getName() + ". Name: " + name);

		return ((Number)data).doubleValue();
	}

	public Date getDate(String name)
	{
		Object data = getObject(name);
		return (Date)data;
	}

	public String toString()
	{
		return "Record [" + nameToVal.toString() + "]";
	}
}
