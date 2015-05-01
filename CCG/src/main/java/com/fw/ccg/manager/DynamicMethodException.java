package com.fw.ccg.manager;

import java.lang.reflect.Method;

import com.fw.ccg.util.CCGUtility;

/**
 * © Copyright 2006 IBM Corporation
 * <BR><BR>
 * This exception is thrown when an unsupported method is invoked on 
 * CCG dynamic managers.
 * <BR>
 * @author A. Kranthi Kiran
 */
public class DynamicMethodException extends ManagerException
{
	private static final long serialVersionUID=1L;
		public DynamicMethodException(Method met)
		{
			this("Encountered unsupported method call on dynamic bean.",null,met);
		}
	
		public DynamicMethodException(String mssg,Method met)
		{
			this(mssg,null,met);
		}
	
		public DynamicMethodException(String mssg,Throwable ex,Method met)
		{
			super(mssg+"\nMethod:"+CCGUtility.toString(met),ex);
		}
}
