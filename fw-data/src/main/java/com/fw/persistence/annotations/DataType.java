package com.fw.persistence.annotations;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public enum DataType
{
	STRING(String.class), 
	INT(byte.class, Byte.class, short.class, Short.class, int.class, Integer.class), 
	LONG(long.class, Long.class), 
	FLOAT(float.class, Float.class), 
	DOBULE(double.class, Double.class),
	BOOLEAN(boolean.class, Boolean.class),
	DATE, 
	BLOB, 
	CLOB,
	DATE_TIME(Date.class), 
	UNKNOWN;
	
	private static Map<Class<?>, DataType> typeMap;
	
	private Class<?> javaTypes[];

	private DataType(Class<?>... javaTypes)
	{
		this.javaTypes = javaTypes;
	}
	
	public static DataType getDataType(Class<?> type)
	{
		if(typeMap != null)
		{
			return typeMap.get(type);
		}
		
		Map<Class<?>, DataType> map = new HashMap<Class<?>, DataType>();
		
		for(DataType dataType: DataType.values())
		{
			for(Class<?> jtype: dataType.javaTypes)
			{
				map.put(jtype, dataType);
			}
		}
		
		DataType.typeMap = map;
		return map.get(type);
	}
	
	public String getName()
	{
		return super.name();
	}
}
