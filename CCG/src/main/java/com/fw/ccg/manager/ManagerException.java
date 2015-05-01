package com.fw.ccg.manager;

import com.fw.ccg.core.CCGException;

/**
 * © Copyright 2006 IBM Corporation
 * <BR><BR>
 * Thrown when a problem occurs, while loading manager from the XML stream. 
 * <BR>
 * @author A. Kranthi Kiran
 */
public class ManagerException extends CCGException
{
	private static final long serialVersionUID=1L;

		public ManagerException()
		{
			super();
		}
	
		public ManagerException(String mssg)
		{
			super(mssg);
		}
	
		public ManagerException(String mssg,Throwable rootCause)
		{
			super(mssg,rootCause);
		}
	
		public ManagerException(Throwable rootCause)
		{
			super(rootCause);
		}
}
