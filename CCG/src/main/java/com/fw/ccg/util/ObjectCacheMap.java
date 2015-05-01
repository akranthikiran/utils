package com.fw.ccg.util;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

public class ObjectCacheMap<K, V>
{
	private LinkedHashMap<K, V> map = new LinkedHashMap<K, V>()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public  boolean removeEldestEntry(Entry<K, V> eldest)
		{
			return super.size() > cacheSize;
		}
	};
	
	private int cacheSize = 100;
	
	public int getCacheSize()
	{
		return cacheSize;
	}

	public void setCacheSize(int cacheSize)
	{
		if(cacheSize < 2)
		{
			throw new IllegalArgumentException("Illegal cache size specified. Minimum size is 2. Specified size: " + cacheSize);
		}
		
		this.cacheSize = cacheSize;
	}

	public void remove(K key)
	{
		map.remove(key);
	}
	
	public void put(K key, V value)
	{
		if(map.containsKey(key))
		{
			map.remove(key);
		}
		
		map.put(key, value);
	}
	
	public V get(K key)
	{
		return map.get(key);
	}
	
	public V revisitKey(K key)
	{
		V value = map.remove(key);
		
		if(value == null)
		{
			return null;
		}
		
		map.put(key, value);
		
		return value;
	}
	
	public Set<K> keySet()
	{
		return map.keySet();
	}
	
	public Collection<V> values()
	{
		return map.values();
	}
	
	public int size()
	{
		return map.size();
	}
	
	public void clear()
	{
		map.clear();
	}
	
	@Override
	public String toString()
	{
		return map.toString();
	}
}
