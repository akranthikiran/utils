package com.fw.persistence.conversion;

import com.fw.persistence.PersistenceException;

public class DataConversionException extends PersistenceException
{
	private static final long serialVersionUID = 1L;

	public DataConversionException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public DataConversionException(String message)
	{
		super(message);
	}
}
