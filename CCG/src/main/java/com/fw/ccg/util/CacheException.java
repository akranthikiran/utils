package com.fw.ccg.util;

import com.fw.ccg.core.CCGException;


/**
 * © Copyright 2006 IBM Corporation
 * <BR><BR>
 * Thrown by Caching mechanisms when there is an error in maintaining cache.
 * <BR>
 * @author A. Kranthi Kiran
 */
public class CacheException extends CCGException
{
	private static final long serialVersionUID=1L;

		public CacheException()
		{
			super();
		}
	
		public CacheException(String mssg)
		{
			super(mssg);
		}
	
		public CacheException(String mssg,Throwable rootCause)
		{
			super(mssg,rootCause);
		}
	
		public CacheException(Throwable rootCause)
		{
			super(rootCause);
		}
}
