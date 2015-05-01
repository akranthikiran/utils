/*
Title: Utilities 
Page Name: Grid tags, Excel data generator
Author: A. Kranthi Kiran
Date:   11/15/2005
Description: A simple iterator wrapper over array.
*/
package com.fw.ccg.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * © Copyright 2006 IBM Corporation
 * <BR><BR>
 * This class represents a iterator (java.util.Iterator) wrapper over arrays.
 * <BR>
 * @author A. Kranthi Kiran
 */
public class ArrayIterator<T> implements Iterator<T>
{
	private T array[];
	private int index;
		/**
		 * Builds an ArrayIterator with specified array as source.
		 * @param arr Source array.
		 */
		public ArrayIterator(T arr[])
		{
				if(arr==null)
					throw new NullPointerException("Source array cannot be null.");
			array=arr;
		}
		
		/**
		 * Builds an ArrayIterator with specified array as source. And positions this iterator
		 * at "st".
		 * @param arr Source array.
		 * @param st Starting point of iterator.
		 */
		public ArrayIterator(T arr[],int st)
		{
			this(arr);
			
				if(st<0 || st>=arr.length)
					throw new IndexOutOfBoundsException("Specified start index is out of index.");
				
			index=st;
		}
		
		/**
		 * Equivalent to calling ArrayIterator(col.toArray()).
		 * @param col Source array in form of Collection
		 */
		@SuppressWarnings("unchecked")
		public ArrayIterator(Collection<T> col)
		{
				if(col==null)
					throw new NullPointerException("Source collection cannot be null.");
			array=(T[])col.toArray();
		}
		
		/**
		 * Equivalent to calling ArrayIterator(col.toArray(),st).
		 * @param col Source array in form of Collection
		 * @param st Starting point of iterator.
		 */
		@SuppressWarnings("unchecked")
		public ArrayIterator(Collection<T> col,int st)
		{
				if(col==null)
					throw new NullPointerException("Source collection cannot be null");
				
				if(st<0 || st>=col.size())
					throw new IndexOutOfBoundsException("Specified start index is out of index.");
			array=(T[])col.toArray();
			index=st;
		}
		
		/**
		 * Returns undelying source array.
		 * @return undelying source array.
		 */
		public T[] toArray()
		{
			return array;
		}
		
		/* (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext()
		{
			return (index<array.length);
		}
		
		/* (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		public T next() 
		{
				if(index>=array.length)
					throw new NoSuchElementException("No element found.");
				
			return array[index++];
		}
		
		/**
		 * Returns object currently pointed by iterator without changing iterator position.
		 * @return object currently pointed by iterator.
		 */
		public T getCurrent()
		{
				if(index==0)
					throw new NoSuchElementException("No element found.");
				
				if(index-1>=array.length)
					throw new NoSuchElementException("No element found.");
				
			return array[index-1];
		}
		
		/**
		 * Returns next object in forward direction of iterator without changing iterator position.
		 * @return next object in forward direction of iterator.
		 */
		public T getNext()
		{
				if(index>=array.length)
					throw new NoSuchElementException("No element found.");
				
			return array[index];
		}
		
		/**
		 * Always throws UnsupportedOperationException.
		 * @see java.util.Iterator#remove()
		 */
		public void remove()
		{
			throw new UnsupportedOperationException("Remove operation is not supported by ArrayIterator.");
		}
}
