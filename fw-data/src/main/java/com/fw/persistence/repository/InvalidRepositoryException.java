package com.fw.persistence.repository;

import com.fw.persistence.PersistenceException;

public class InvalidRepositoryException extends PersistenceException
{
	private static final long serialVersionUID = 1L;

	public InvalidRepositoryException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public InvalidRepositoryException(String message)
	{
		super(message);
	}
}
