package com.fw.persistence;

public class UnsupportedOperationException extends PersistenceException
{
	private static final long serialVersionUID = 1L;

	public UnsupportedOperationException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public UnsupportedOperationException(String message)
	{
		super(message);
	}
}
