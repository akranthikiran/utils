package com.fw.persistence;

public class UniqueConstraintViolationException extends PersistenceException
{
	private static final long serialVersionUID = 1L;
	
	private String constraintName;

	public UniqueConstraintViolationException(String constraintName, String message, Throwable cause)
	{
		super(message, cause);
		this.constraintName = constraintName;
	}

	public UniqueConstraintViolationException(String constraintName, String message)
	{
		super(message);
		this.constraintName = constraintName;
	}
	
	public String getConstraintName()
	{
		return constraintName;
	}
}
