/**
 * 
 */
package com.fw.utils;

/**
 * Formatter to format input object into string
 * @author akiran
 */
public interface IFormatter
{
	/**
	 * Formats the specified object into string and returns it
	 * @param value
	 * @return
	 */
	public String convert(Object value);
}
