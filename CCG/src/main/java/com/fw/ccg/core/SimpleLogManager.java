package com.fw.ccg.core;

import java.io.FileNotFoundException;
import java.io.PrintStream;

/**
 * <BR><BR>
 * A simple implementation of LogManager. This implementation logs messgages to the 
 * print stream provided in the constructor. If default constructor is used, instance 
 * of this class behaves like a dummy log manager. That is, log messages to no where
 * and behaves like a dummy.
 * <BR>
 * @author A. Kranthi Kiran
 */
public class SimpleLogManager implements LogManager
{
	private PrintStream log;
		public SimpleLogManager()
		{}
		
		public SimpleLogManager(PrintStream log)
		{
				if(log==null)
					throw new NullPointerException("Log stream cannot be null.");
			this.log=log;
		}
		
		public SimpleLogManager(String fileName) throws FileNotFoundException
		{
				if(fileName==null)
					throw new NullPointerException("File name cannot be null.");
			this.log=new PrintStream(fileName);
		}
		
		public void log(String mssg)
		{
				if(log!=null)
				{
					log.print(mssg);
					log.flush();
				}
		}
	
		public void log(String mssg,Throwable ex)
		{
				if(log==null)
					return;
				
			log.print(mssg);
				if(ex!=null)
				{
					ex.printStackTrace(log);
					log.println();
				}
			log.flush();
		}
}
