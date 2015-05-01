package com.fw.persistence;

public class InvalidConfigurationException extends PersistenceException
{
	private static final long serialVersionUID = 1L;

	public InvalidConfigurationException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public InvalidConfigurationException(String message)
	{
		super(message);
	}
}
