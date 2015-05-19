/**
 * 
 */
package com.fw.utils;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A iterator wrapper over array. Array can be of primitive type or complex type 
 * @author akiran
 */
public class ArrayIterator implements Iterator<Object>
{
	private Object array;
	
	private int index = 0;
	private int length = 0;
	
	public ArrayIterator(Object array)
	{
		if(!array.getClass().isArray())
		{
			throw new IllegalArgumentException("Invalid array value specified: " + array);
		}
		
		this.array = array;
		length = Array.getLength(array);
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext()
	{
		return (index < length);
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	public Object next()
	{
		if(index >= length)
		{
			throw new NoSuchElementException();
		}

		Object val = Array.get(array, index);
		index++;
		
		return val;
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	public void remove()
	{
		throw new UnsupportedOperationException("Remove operation is not supported on this iterator");
	}
}
