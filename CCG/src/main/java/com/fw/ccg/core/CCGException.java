package com.fw.ccg.core;


/**
 * <BR><BR>
 * A generic exception for CCG classes. 
 * <BR>
 * @author A. Kranthi Kiran
 */
public class CCGException extends RuntimeException
{
	private static final long serialVersionUID=1L;

		public CCGException()
		{
			super();
		}
	
		public CCGException(String mssg,Throwable rootCause)
		{
			super(mssg,rootCause);
		}
	
		public CCGException(String mssg)
		{
			super(mssg);
		}
	
		public CCGException(Throwable rootCause)
		{
			super(rootCause);
		}
}
