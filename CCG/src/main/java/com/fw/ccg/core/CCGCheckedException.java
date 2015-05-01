package com.fw.ccg.core;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * <BR><BR>
 * A generic exception for CCG classes. 
 * <BR>
 * @author A. Kranthi Kiran
 */
public class CCGCheckedException extends RuntimeException
{
	private static final long serialVersionUID=1L;
	private Throwable rootCause;

		public CCGCheckedException()
		{
			super();
		}
	
		public CCGCheckedException(String mssg,Throwable rootCause)
		{
			super(mssg);
			this.rootCause=rootCause;
		}
	
		public CCGCheckedException(String mssg)
		{
			super(mssg);
		}
	
		public CCGCheckedException(Throwable rootCause)
		{
			super();
			this.rootCause=rootCause;
		}

		public void printStackTrace()
		{
			super.printStackTrace();
				if(rootCause!=null)
				{
					System.err.print("Root Cause: ");
					rootCause.printStackTrace();
				}
		}

		public void printStackTrace(PrintStream out)
		{
			super.printStackTrace(out);
				if(rootCause!=null)
				{
					out.print("Root Cause: ");
					rootCause.printStackTrace(out);
				}
		}

		public void printStackTrace(PrintWriter out)
		{
			super.printStackTrace(out);
				if(rootCause!=null)
				{
					out.print("Root Cause: ");
					rootCause.printStackTrace(out);
				}
		}

		public Throwable getCause()
		{
			return rootCause;
		}

		public String toString()
		{
			StringBuffer buff=new StringBuffer(this.getClass().getName());
			buff.append(": ");
			buff.append(getMessage());
			return buff.toString();
		}
}
