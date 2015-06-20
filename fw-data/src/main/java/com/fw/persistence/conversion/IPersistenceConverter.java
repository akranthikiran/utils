package com.fw.persistence.conversion;

import com.fw.persistence.annotations.DataType;

/**
 * Interface to convert that can be used on entity fields to control the wat the value
 * is stored and read to/from db
 * @author akiran
 */
public interface IPersistenceConverter
{
	/**
	 * Converts "dbObject" read from DB (whose data type is specified "dBtype") to specified 
	 * java-type
	 * @param dbObject DB Object to be converted
	 * @param dbType Target field db type
	 * @param javaType Target field java type
	 * @return Java type converted object
	 */
	public Object convertToJavaType(Object dbObject, DataType dbType, Class<?> javaType);
	
	/**
	 * Converts specified java object into target dbType object
	 * @param javaObject Source java object that needs to be converted
	 * @param dbType Target DB type
	 * @return DB type converted object
	 */
	public Object convertToDBType(Object javaObject, DataType dbType);
}
