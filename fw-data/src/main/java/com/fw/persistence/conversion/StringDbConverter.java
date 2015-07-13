package com.fw.persistence.conversion;

import com.fw.persistence.annotations.DataType;

/**
 * Converts different data types into string, for string db data type
 * @author akiran
 */
public class StringDbConverter implements IPersistenceConverter
{
	@Override
	public Object convertToJavaType(Object dbObject, DataType dbType, Class<?> javaType)
	{
		//return null, so that default behaviour is retained
		return null;
	}

	@Override
	public Object convertToDBType(Object javaObject, DataType dbType)
	{
		//for non string ignore
		if(dbType != DataType.STRING)
		{
			return null;
		}
		
		//simple convert java object to string using to-string
		return javaObject.toString();
	}

}
