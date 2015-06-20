package com.fw.persistence.rdbms.converters;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Blob;

import com.fw.persistence.UnsupportedOperationException;
import com.fw.persistence.annotations.DataType;
import com.fw.persistence.conversion.IPersistenceConverter;


/**
 * BLOB conversion handler
 * @author akiran
 */
public class BlobConverter implements IPersistenceConverter
{
	
	/**
	 * Reads object from specified input stream and returns the same
	 * @param is
	 * @return
	 * @throws ClassNotFoundException 
	 */
	private Object readObject(InputStream is) throws IOException, ClassNotFoundException
	{
		ObjectInputStream ois = new ObjectInputStream(is);
		Object res = ois.readObject();
		
		ois.close();
		is.close();
		
		return res;
	}
	
	/* (non-Javadoc)
	 * @see com.fw.persistence.conversion.IPersistenceConverter#convertToJavaType(java.lang.Object, com.fw.persistence.annotations.DataType, java.lang.Class)
	 */
	@Override
	public Object convertToJavaType(Object dbObject, DataType dbType, Class<?> javaType)
	{
		//if db type is not blob, don't try conversion
		if(dbType != DataType.BLOB)
		{
			return null;
		}
		
		//if db object is char[] and target is string
		if(dbObject instanceof byte[])
		{
			try
			{
				return readObject(new ByteArrayInputStream((byte[])dbObject));
			}catch(Exception ex)
			{
				throw new IllegalStateException("An error occurred while reading object from blob", ex);
			}
		}

		//if db object is blob and target is string
		if(dbObject instanceof Blob)
		{
			Blob blob = (Blob)dbObject;
			
			try
			{
				return readObject(blob.getBinaryStream());
			}catch(Exception ex)
			{
				throw new IllegalStateException("An error occurred while reading object from blob", ex);
			}
		}

		throw new UnsupportedOperationException(String.format("Unsupported db object type '%s' encountered during BLOB to java conversion", dbObject.getClass().getName()));
	}

	/* (non-Javadoc)
	 * @see com.fw.persistence.conversion.IPersistenceConverter#convertToDBType(java.lang.Object, com.fw.persistence.annotations.DataType)
	 */
	@Override
	public Object convertToDBType(Object javaObject, DataType dbType)
	{
		//if not blob field, ignore
		if(dbType != DataType.BLOB)
		{
			return null;
		}
		
		//convert java object into byte[]
		try
		{
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			
			oos.writeObject(javaObject);
			oos.flush();
			
			oos.close();
			bos.flush();
			
			return bos.toByteArray();
		}catch(Exception ex)
		{
			throw new IllegalStateException("An error occurred while converting specified java object into BLOB - " + javaObject, ex);
		}
	}

}
