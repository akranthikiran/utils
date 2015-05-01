package com.fw.ccg.util;

import com.fw.ccg.core.CCGException;

/**
 * © Copyright 2006 IBM Corporation
 * <BR><BR>
 * Thrown if an error occurs if an exception occurs during method searching in the 
 * CCGUtility methods.
 * <BR><BR>
 * @author Kranthi
 */
public class MethodSearchException extends CCGException
{
	private static final long serialVersionUID=1L;

		public MethodSearchException()
		{
			super();
		}
	
		public MethodSearchException(String mssg,Throwable rootCause)
		{
			super(mssg,rootCause);
		}
	
		public MethodSearchException(String mssg)
		{
			super(mssg);
		}
	
		public MethodSearchException(Throwable rootCause)
		{
			super(rootCause);
		}
		
		public MethodSearchException(String methodName,boolean noMatch,Object args[],int level,Class cls)
		{
			super(buildMessage(methodName,noMatch,args,level,cls));
		}
		
		public MethodSearchException(String methodName,boolean noMatch,Class argTypes[],int level,Class cls)
		{
			super(buildMessage(methodName,noMatch,argTypes,level,cls));
		}
		
		private static String buildMessage(String methodName,boolean noMatch,Object args[],int level,Class cls)
		{
			StringBuffer mssg=new StringBuffer();
				if(noMatch)
					mssg.append("No property found with name \"");
				else
					mssg.append("Multiple properties matches found with name \"");
			
			mssg.append(methodName);
			mssg.append("\" which can accept parameters (");
			
				if(args!=null)
				{
					for(int i=0;i<args.length;i++)
					{
							if(args[i]==null)
								mssg.append("null");
							else
								mssg.append(args[i].getClass().getName());
						
							if(i<args.length-1)
								mssg.append(",");
					}
				}
			
			mssg.append(") in class \"");
			mssg.append(cls.getName());
			mssg.append("\"\nNested Level: ");
			mssg.append(level);
			return mssg.toString();	
		}

		private static String buildMessage(String methodName,boolean noMatch,Class argTypes[],int level,Class cls)
		{
			StringBuffer mssg=new StringBuffer();
				if(noMatch)
					mssg.append("No property found with name \"");
				else
					mssg.append("Multiple properties matches found with name \"");
			
			mssg.append(methodName);
			mssg.append("\" which can accept parameters (");
			
				if(argTypes!=null)
				{
					for(int i=0;i<argTypes.length;i++)
					{
							if(argTypes[i]==null)
								mssg.append("?");
							else
								mssg.append(argTypes[i].getName());
						
							if(i<argTypes.length-1)
								mssg.append(",");
					}
				}
			
			mssg.append(") in class \"");
			mssg.append(cls.getName());
			mssg.append("\"\nNested Level: ");
			mssg.append(level);
			return mssg.toString();	
		}
}
