package com.fw.persistence.conversion.impl;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Clob;

import com.fw.persistence.FieldDetails;
import com.fw.persistence.conversion.IConverter;

import conm.fw.common.util.JsonWrapper;

public class JsonFieldConverter implements IConverter
{
	@Override
	public Object convertToDBType(Object object, FieldDetails fieldDetails)
	{
		return JsonWrapper.format(object);
	}

	@Override
	public Object convertToTargetType(Object json, Class<?> target)
	{
		if(!(json instanceof String))
		{
			json = toStr(json);
		}
		
		return JsonWrapper.parse((String)json);
	}


	private static String toStr(Object obj)
	{
		if(obj instanceof String)
			return (String)obj;

		if(obj instanceof byte[])
			return new String((byte[])obj);

		if(obj instanceof char[])
			return new String((char[])obj);

		if(obj instanceof Blob)
		{
			Blob blob = (Blob)obj;
			try
			{
				return readStream(blob.getBinaryStream());
			}catch(Exception ex)
			{
				throw new IllegalStateException("An error occured while reading blob data.", ex);
			}
		}

		if(obj instanceof Clob)
		{
			Clob clob = (Clob)obj;
			try
			{
				return readStream(clob.getAsciiStream());
			}catch(Exception ex)
			{
				throw new IllegalStateException("An error occured while reading clob data.", ex);
			}
		}

		return obj.toString();
	}

	private static String readStream(InputStream is) throws IOException
	{
		byte buff[] = new byte[1024];
		int read = 0;
		StringBuilder res = new StringBuilder();

		while((read = is.read(buff)) > 0)
		{
			res.append(new String(buff, 0, read));
		}

		return res.toString();
	}
}
