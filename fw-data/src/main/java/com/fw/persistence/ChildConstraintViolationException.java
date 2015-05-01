package com.fw.persistence;

public class ChildConstraintViolationException extends PersistenceException
{
	private static final long serialVersionUID = 1L;
	
	private String constraintName;

	public ChildConstraintViolationException(String constraintName, String message, Throwable cause)
	{
		super(message, cause);
		this.constraintName = constraintName;
	}

	public ChildConstraintViolationException(String constraintName, String message)
	{
		super(message);
		this.constraintName = constraintName;
	}
	
	public String getConstraintName()
	{
		return constraintName;
	}
}
