package com.fw.ccg.util;

import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

public class CompositeCollection<T> extends AbstractCollection<T>
{
	private List<Collection<T>> collections = new LinkedList<Collection<T>>();
	private int size = 0;
	
	public CompositeCollection()
	{}
	
	public CompositeCollection(Collection<? extends Collection<T>> collections)
	{
		addCollections(collections);
	}
	
	public CompositeCollection(Collection<T>... collections)
	{
		addCollections(collections);
	}
	
	public void addCollections(Collection<? extends Collection<T>> collections)
	{
		for(Collection<T> col: collections)
		{
			addCollection(col);
		}
	}
	
	public void addCollections(Collection<T>... collections)
	{
		addCollections(Arrays.asList(collections));
	}
	
	public void addCollection(Collection<T> collection)
	{
		if(collection.isEmpty())
		{
			return;
		}
		
		this.collections.add(collection);
		size += collection.size();
	}
	
	@Override
	public Iterator<T> iterator()
	{
		Iterator<T> it = new Iterator<T>()
		{
			private Iterator<Collection<T>> collectionIt = collections.iterator();
			private Iterator<T> activeIt = null;
			
			@Override
			public boolean hasNext()
			{
				if(activeIt == null || !activeIt.hasNext())
				{
					return collectionIt.hasNext();
				}
				
				return activeIt.hasNext();
			}

			@Override
			public T next()
			{
				if(activeIt == null || !activeIt.hasNext())
				{
					if(!collectionIt.hasNext())
					{
						throw new NoSuchElementException();
					}
					
					activeIt = collectionIt.next().iterator(); 
				}
				
				return activeIt.next();
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException("Remove is not supported by this iterator");
			}
		};
		
		return it;
	}

	@Override
	public int size()
	{
		return size;
	}

}
