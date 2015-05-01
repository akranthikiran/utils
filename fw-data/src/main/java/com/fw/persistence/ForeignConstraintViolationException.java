package com.fw.persistence;

public class ForeignConstraintViolationException extends PersistenceException
{
	private static final long serialVersionUID = 1L;
	
	private String constraintName;

	public ForeignConstraintViolationException(String constraintName, String message, Throwable cause)
	{
		super(message, cause);
		this.constraintName = constraintName;
	}

	public ForeignConstraintViolationException(String constraintName, String message)
	{
		super(message);
		this.constraintName = constraintName;
	}
	
	public String getConstraintName()
	{
		return constraintName;
	}
}
