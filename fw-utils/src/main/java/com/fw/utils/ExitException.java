/**
 * 
 */
package com.fw.utils;


/**
 * @author akiran
 *
 */
public class ExitException extends RuntimeException
{
	private static final long serialVersionUID = 1L;
	
	private int code;

	/**
	 * @param message
	 */
	public ExitException(int code, String message)
	{
		super(message);
		
		this.code = code;
	}
	
	/**
	 * @return the code
	 */
	public int getCode()
	{
		return code;
	}
}
