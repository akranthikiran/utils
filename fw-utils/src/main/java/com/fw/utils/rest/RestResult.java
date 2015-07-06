/**
 * 
 */
package com.fw.utils.rest;

/**
 * Represents result of REST api invocation
 * 
 * @author akiran
 */
public class RestResult<T>
{
	/**
	 * Result of rest API invocation, if any
	 */
	private T value;

	/**
	 * Http response status code
	 */
	private int statusCode;

	/**
	 * @param value
	 * @param statusCode
	 */
	public RestResult(T value, int statusCode)
	{
		super();
		this.value = value;
		this.statusCode = statusCode;
	}

	/**
	 * Gets value of value
	 * 
	 * @return the value
	 */
	public T getValue()
	{
		return value;
	}

	/**
	 * Gets value of statusCode
	 * 
	 * @return the statusCode
	 */
	public int getStatusCode()
	{
		return statusCode;
	}
}
