package com.fw.ccg.core;

/**
 * this interface and use application specific logging (eg., log4j etc.,). Most of the
 * critical components of CCG accepts this interface objects for logging messages.
 * In order to maitain standard, implementation classes should follow below
 * rules.
 * <BR>
 * @author A. Kranthi Kiran
 */
public interface LogManager
{
	/**
	 * A Log manager that log messages to standard output.
	 */
	public LogManager STD_OUT_MANAGER=new SimpleLogManager(System.out);
	/**
	 * A Log manager that log messages to standard error.
	 */
	public LogManager STD_ERR_MANAGER=new SimpleLogManager(System.err);
	/**
	 * A Log manager that log messages to none. As the name suggests its dummy, it
	 * is not null, will log messages anywhere.
	 */
	public LogManager DUMMY_MANAGER=new SimpleLogManager();
	
	/**
	 * This method should not add any line breaks 
	 * before or after the message. The line breaks and tabs (for formatting)
	 * are expected to be part of message. This helps calling methods to format messages
	 * in customizable way.	 
	 * @param mssg Message to be logged.
	 */
	public void log(String mssg);
	
	/**
	 * This method, just like log(String) prints message without adding line 
	 * breaks before or after message and should be followed
	 * by ex.printStackTrace() and which inturn should be followed by empty line.
	 * So that there is line break for sure after stack trace.
	 * <BR>
	 * If exception "ex" is null, then this method behaves just like log(String).	 
	 * @param mssg Message to be loagged.
	 * @param ex Throwable to be logged.
	 */
	public void log(String mssg,Throwable ex);
}
