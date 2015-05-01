package com.fw.ccg.xml;

public class BeanPropertyException extends RuntimeException
{
	private static final long serialVersionUID=1L;

		public BeanPropertyException()
		{
		}
	
		public BeanPropertyException(String message)
		{
			super(message);
		}
	
		public BeanPropertyException(Throwable cause)
		{
			super(cause);
		}
	
		public BeanPropertyException(String message,Throwable cause)
		{
			super(message,cause);
		}
}
