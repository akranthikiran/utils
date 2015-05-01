package com.fw.ccg.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An iterator implementation which buffers one bean at a time. For iterator buffereing 
 * nextForBuffer() is used. When this method returns null, the iterator is treated to be 
 * completed.
 * 
 * @author kranthikirana
 *
 * @param <T>
 */
public abstract class AbstractBufferedIterator<T> implements Iterator<T>
{
	private T bean;
	
		protected AbstractBufferedIterator()
		{
			restart();
		}
		
		/**
		 * Again a bean is fetched using nextForBuffer() and buffered. If non-null
		 * is returned, the iterator is again restarted till nextForBuffer() reaches
		 * null value. 
		 */
		public void restart()
		{
			this.bean=nextForBuffer();
		}
	
		@Override
	    public final boolean hasNext()
	    {
		    return (bean!=null);
	    }
	
		@Override
	    public void remove()
	    {
			throw new UnsupportedOperationException("Remove is not supported by this iterator.");
	    }

		@Override
		public final T next()
		{
				if(bean==null)
					throw new NoSuchElementException();
				
			T curBean=bean;
			this.bean=nextForBuffer();
			
			return curBean;
		}
		
		public abstract T nextForBuffer();
}
