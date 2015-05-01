package com.fw.ccg.core;



/**
 * <BR><BR>
 * This checked exception is thrown when a validation fails. 
 * <BR>
 * @author A. Kranthi Kiran
 */
public class ValidateException extends CCGCheckedException
{
	private static final long serialVersionUID=1L;

		public ValidateException()
		{
			super();
		}
	
		public ValidateException(String mssg,Throwable rootCause)
		{
			super(mssg,rootCause);
		}
	
		public ValidateException(String mssg)
		{
			super(mssg);
		}
	
		public ValidateException(Throwable rootCause)
		{
			super(rootCause);
		}
}
