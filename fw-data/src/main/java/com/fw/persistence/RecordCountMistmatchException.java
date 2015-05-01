package com.fw.persistence;

public class RecordCountMistmatchException extends PersistenceException
{
	private static final long serialVersionUID = 1L;

	public RecordCountMistmatchException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public RecordCountMistmatchException(String message)
	{
		super(message);
	}
}
