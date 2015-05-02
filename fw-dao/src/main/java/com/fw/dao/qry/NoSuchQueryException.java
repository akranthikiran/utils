package com.fw.dao.qry;

public class NoSuchQueryException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public NoSuchQueryException()
	{
	}

	public NoSuchQueryException(String mssg)
	{
		super(mssg);
	}

	public NoSuchQueryException(Throwable ex)
	{
		super(ex);
	}

	public NoSuchQueryException(String mssg, Throwable ex)
	{
		super(mssg, ex);
	}
}
