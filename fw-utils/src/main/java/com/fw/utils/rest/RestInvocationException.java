/**
 * 
 */
package com.fw.utils.rest;

/**
 * Thrown when there is an error during rest request invocation
 * @author akiran
 */
public class RestInvocationException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param message
	 * @param cause
	 */
	public RestInvocationException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
