package com.fw.ccg.ds;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.fw.ccg.util.Cache;
import com.fw.ccg.util.CacheException;
import com.fw.ccg.util.FileCache;


public class CacheMap implements Map
{
	private HashMap keyToPtr=new HashMap();
	private Cache cache;
	
		public CacheMap()
		{
			cache=new FileCache();
		}
		
		public CacheMap(Cache cache)
		{
				if(cache==null)
					throw new IllegalArgumentException("Cache can not be null.");
			this.cache=cache;
		}
		
		public CacheMap(String path,String name)
		{
			cache=new FileCache(path,name);
		}

		public int size()
		{
			return keyToPtr.size();
		}

		public boolean isEmpty()
		{
			return keyToPtr.isEmpty();
		}

		public boolean containsKey(Object key)
		{
			return keyToPtr.containsKey(key);
		}

		public boolean containsValue(Object value)
		{
			throw new UnsupportedOperationException("This operation is not supported by this map.");
		}

		public Object get(Object key)
		{
			Integer ptr=(Integer)keyToPtr.get(key);
				if(ptr==null)
					return null;
			return cache.readObject(ptr.intValue());
		}

		public Object put(Object key,Object value)
		{
				if(value==null)
					throw new NullPointerException("Value can not be null.");
				
				if(!(value instanceof Serializable))
					throw new IllegalArgumentException("Only serializable objects are supported by this map.");
			
			Object prevValue=remove(key);
			int ptr=0;
				try
				{
					ptr=cache.writeObject(value);
				}catch(NotSerializableException ex)
				{
					throw new CacheException("Specified value type is not serializable: "+value.getClass().getName(),ex);
				}catch(IOException ex)
				{
					throw new CacheException("IO Error in writing object to cache.",ex);
				}
			keyToPtr.put(key,new Integer(ptr));
			return prevValue;
		}

		public Object remove(Object key)
		{
			Integer ptr=(Integer)keyToPtr.get(key);
			Object value=null;
			
				if(ptr!=null)
				{
					value=get(key);
					cache.removeObject(ptr.intValue());
				}
			
			return value;
		}

		public void putAll(Map map)
		{
			Iterator it=map.keySet().iterator();
			Object key=null;
			Object value=null;
				while(it.hasNext())
				{
					key=it.next();
					value=map.get(key);
					
					put(key,value);
				}
		}

		public void clear()
		{
			keyToPtr.clear();
			cache.clear();
		}

		public Set keySet()
		{
			return keyToPtr.keySet();
		}

		public Collection values()
		{
			throw new UnsupportedOperationException("This operation is not supported by this map.");
		}

		public Set entrySet()
		{
			throw new UnsupportedOperationException("This operation is not supported by this map.");
		}

		public boolean equals(Object other)
		{
			return super.equals(other);
		}

		public int hashCode()
		{
			return super.hashCode();
		}

		public String toString()
		{
			return super.toString();
		}
		
		public void optimize()
		{
			cache.optimize();
		}
		
		protected void finalize()
		{
			cache.close();
		}
}
