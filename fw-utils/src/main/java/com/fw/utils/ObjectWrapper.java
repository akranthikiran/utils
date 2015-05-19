/**
 * 
 */
package com.fw.utils;

/**
 * Wrapper over a value. 
 * @author akiran
 */
public class ObjectWrapper<T>
{
	private T value;

	public ObjectWrapper()
	{}
	
	public ObjectWrapper(T val)
	{
		this.value = val;
	}
	
	/**
	 * Gets value of value 
	 * @return the value
	 */
	public T getValue()
	{
		return value;
	}

	/**
	 * Sets value for value
	 * @param value the value to set
	 */
	public void setValue(T value)
	{
		this.value = value;
	}

}
