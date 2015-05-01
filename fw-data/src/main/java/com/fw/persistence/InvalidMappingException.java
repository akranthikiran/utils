package com.fw.persistence;

public class InvalidMappingException extends PersistenceException
{
	private static final long serialVersionUID = 1L;

	public InvalidMappingException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public InvalidMappingException(String message)
	{
		super(message);
	}
}
