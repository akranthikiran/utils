package com.fw.ccg.util;

import com.fw.ccg.core.CCGException;

/**
 * © Copyright 2006 IBM Corporation
 * <BR><BR>
 * Thrown if an invalid value is passed as an argument.
 * <BR><BR>
 * @author Kranthi
 */
public class InvalidValueException extends CCGException
{
	private static final long serialVersionUID=1L;

		public InvalidValueException()
		{
			super();
		}
	
		public InvalidValueException(String mssg,Throwable rootCause)
		{
			super(mssg,rootCause);
		}
	
		public InvalidValueException(String mssg)
		{
			super(mssg);
		}
	
		public InvalidValueException(Throwable rootCause)
		{
			super(rootCause);
		}
}
