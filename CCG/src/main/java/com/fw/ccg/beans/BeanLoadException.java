package com.fw.ccg.beans;

import com.fw.ccg.core.CCGException;

/**
 * <BR><BR>
 * This exception is thrown when an error occurs during loading or conversion of the bean.
 * <BR>
 * @author A. Kranthi Kiran
 */
public class BeanLoadException extends CCGException
{
	private static final long serialVersionUID=1L;
		public BeanLoadException(String mssg,Throwable rootCause)
		{
			super(mssg,rootCause);
		}

		public BeanLoadException(String mssg)
		{
			super(mssg);
		}

		public BeanLoadException(Throwable rootCause)
		{
			super(rootCause);
		}
}
