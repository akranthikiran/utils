package com.fw.persistence.rdbms.converters;

import java.io.Reader;
import java.sql.Clob;

import org.apache.commons.io.IOUtils;

import com.fw.persistence.UnsupportedOperationException;
import com.fw.persistence.annotations.DataType;
import com.fw.persistence.conversion.IPersistenceConverter;


/**
 * CLOB conversion handler
 * @author akiran
 */
public class ClobConverter implements IPersistenceConverter
{
	/* (non-Javadoc)
	 * @see com.fw.persistence.conversion.IPersistenceConverter#convertToJavaType(java.lang.Object, com.fw.persistence.annotations.DataType, java.lang.Class)
	 */
	@Override
	public Object convertToJavaType(Object dbObject, DataType dbType, Class<?> javaType)
	{
		//if db type is not clob, don't try conversion
		if(dbType != DataType.CLOB)
		{
			return null;
		}
		
		//if db object is char[] and target is string
		if(dbObject instanceof char[])
		{
			return new String((char[])dbObject);
		}

		//if db object is clob and target is string
		if(dbObject instanceof Clob)
		{
			Clob clob = (Clob)dbObject;
			
			try
			{
				Reader reader = clob.getCharacterStream();
				String res = IOUtils.toString(reader);
				
				reader.close();
				return res;
			}catch(Exception ex)
			{
				throw new IllegalStateException("An error occured while reading clob data.", ex);
			}
		}

		throw new UnsupportedOperationException(String.format("Unsupported db object type '%s' encountered during CLOB to java conversion", dbObject.getClass().getName()));
	}

	/* (non-Javadoc)
	 * @see com.fw.persistence.conversion.IPersistenceConverter#convertToDBType(java.lang.Object, com.fw.persistence.annotations.DataType)
	 */
	@Override
	public Object convertToDBType(Object javaObject, DataType dbType)
	{
		//if not clob field, ignore
		if(dbType != DataType.CLOB)
		{
			return null;
		}
		
		//ensure java type is string
		if(!(javaObject instanceof String))
		{
			throw new UnsupportedOperationException(String.format("Non-string object is encountered for CLOB storage - {}", javaObject));
		}

		return javaObject;
	}

}
