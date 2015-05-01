package com.fw.ccg.util;

import java.util.Iterator;

public class ArrayIterable<T> implements Iterable<T>
{
	private T data[];

		public ArrayIterable(T[] data)
		{
			super();
			this.data=data;
		}
	
		public Iterator<T> iterator()
		{
			return new ArrayIterator<T>(data);
		}
}
