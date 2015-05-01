package com.fw.ccg.ds;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import com.fw.ccg.util.Cache;

public class TimeBasedCacheMap implements Map
{
	public static final int MAX_TIME_GAP=1440;//in minutes, 24 hours, 1 day
	public static final int DEFAULT_TIME_GAP=10;//in minutes
	
	private static Timer bgThread=new Timer("TimerBasedCacheMap_BG_THREAD",true);
	
		private class Wrapper
		{
			private Object value;
			private char stTime;//start time
				public Wrapper(Object value)
				{
					this.value=value;
					stTime=curTime;
				}
				
				public void resetTime()
				{
					stTime=curTime;
				}
				
				public Object getValue()
				{
					stTime=curTime;
					return value;
				}
				
				public boolean isTimeElapsed()
				{
					int gap=0;
						if(stTime>curTime)//in the case of overflow
						{
							gap=MAX_TIME_GAP-stTime;
							gap+=curTime;
						}
						else
							gap=curTime-stTime;
					return (gap>=timeGap);
				}
			
		}
	
		public interface RemoveableBean
		{}
		
		public interface PermanentBean
		{}
		
		public static class MutableBeanContext implements Serializable
		{
			private static final long serialVersionUID=1L;
			
			private transient Object key;
			private transient Object value;
			private transient TimeBasedCacheMap timeMap;
			
				private MutableBeanContext(Object key,Object value,TimeBasedCacheMap timeMap)
				{
					this.key=key;
					this.value=value;
					this.timeMap=timeMap;
				}
				
				public void registerChange()
				{
					timeMap.recacheValue(key,value);
				}
		}
		
		public interface MutableBean extends Serializable
		{
			public void setMutableBeanContext(MutableBeanContext context);
		}
		
	private int entryCount=0;
	private HashMap memoryMap=new HashMap();
	private CacheMap cacheMap=null;
	private char timeGap=10;
	private char curTime=0;
	private CacheMapListener listener;
	private boolean cacheEnabled=true;
	
		public TimeBasedCacheMap()
		{
			this(null,null,DEFAULT_TIME_GAP);
		}
		
		public TimeBasedCacheMap(int timeGap)
		{
			this(null,null,timeGap);
		}
		
		public TimeBasedCacheMap(String path,String name)
		{
			this(path,name,DEFAULT_TIME_GAP);
		}
		
		public TimeBasedCacheMap(String path,String name,int timeGap)
		{
				if(path==null || name==null)
					cacheMap=new CacheMap();
				else
					cacheMap=new CacheMap(path,name);
				
				if(timeGap<=0)
					throw new IllegalArgumentException("Time gap should be non-zero positive value.");
				
				if(timeGap>MAX_TIME_GAP)
					throw new IllegalArgumentException("Specified time gap exceeds MAX_TIME_GAP");
			
			setTimeGap((char)timeGap);
		}
		
		public TimeBasedCacheMap(Cache cache)
		{
			this(cache,DEFAULT_TIME_GAP);
		}
		
		public TimeBasedCacheMap(Cache cache,int timeGap)
		{
				if(cache!=null)
					cacheMap=new CacheMap(cache);
				else
					cacheMap=new CacheMap();
			
				if(timeGap<=0)
					throw new IllegalArgumentException("Time gap should be non-zero positive value.");
				
				if(timeGap>MAX_TIME_GAP)
					throw new IllegalArgumentException("Specified time gap exceeds MAX_TIME_GAP");
				
			setTimeGap((char)timeGap);
		}
		
		private void setTimeGap(final char timeGap)
		{
			this.timeGap=timeGap;
			TimerTask task=new TimerTask()
				{
					public void run()
					{
						curTime++;
							if(curTime>MAX_TIME_GAP)
								curTime=0;
							
							if(curTime%timeGap==0)
								checkForCache();
					}
				};
			
			long gap=60*1000;//conversion of min to millSec 
			bgThread.schedule(task,gap,gap);	
		}
		
		public int getTimeGap()
		{
			return timeGap;
		}
		
		private synchronized void recacheValue(Object key,Object value)
		{
			//even though cache is disabled, this operation should not stop
			//to make sure, cache is up-to-date when cahce is re-enabled.
				if(cacheMap.containsKey(key))
				{
					//change the value in cache
					cacheMap.remove(key);
					cacheMap.put(key,value);
					
					//register new value in the memory map
					memoryMap.put(key,new Wrapper(value));
				}
				
			//reset time in memory for this entry
			Wrapper wrap=(Wrapper)memoryMap.get(key);
			wrap.resetTime();
		}
		
		private void checkForCache()
		{
				if(!cacheEnabled)
					return;
			//increment current time
			int curTime=this.curTime+timeGap;
				//take take care of overflow
				if(curTime>Character.MAX_VALUE)
				{
					curTime-=Character.MAX_VALUE;
					this.curTime=(char)curTime;
				}
			
			//check for each of memory map current entries for caching
			//Note: newly added entries are not expected to get cached immediately
			
			HashSet keys=new HashSet(memoryMap.keySet());
			Iterator it=keys.iterator();
			Object key=null;
			Object value=null;
			Wrapper wrap=null;
				while(it.hasNext())
				{
					key=it.next();
					
						synchronized(this)
						{
					
							value=memoryMap.get(key);
							
								if(!(value instanceof Wrapper))//Note: nulls are also rejected.
									continue;
							
							wrap=(Wrapper)value;
							
								if(wrap.isTimeElapsed())
								{
									value=wrap.value;
									
										if(value instanceof RemoveableBean)
										{
		             						memoryMap.remove(key);
											entryCount--;
											
												if(listener!=null)
													listener.removedRemovable(key,value);
										}
										else
										{
												if(!cacheMap.containsKey(key))
													cacheMap.put(key,value);
												
											memoryMap.remove(key);
												if(listener!=null)
													listener.objectCached(key,value);
										}
								}
						}
				}
		}

		public synchronized int size()
		{
			return entryCount;
		}
	
		public synchronized boolean isEmpty()
		{
			return (entryCount==0);
		}
	
		public synchronized boolean containsKey(Object key)
		{
			return (memoryMap.containsKey(key) || cacheMap.containsKey(key));
		}
	
		public boolean containsValue(Object value)
		{
			throw new UnsupportedOperationException("This operation is not supported by this map.");
		}
	
		public synchronized Object get(Object key)
		{
			Object value=memoryMap.get(key);
			
				if(value!=null)
				{
						if(!(value instanceof Wrapper))
							return value;
						
					return ((Wrapper)value).getValue();
				}
			
			value=cacheMap.get(key);
			
				if(value==null)
					return null;
			
				if(value instanceof MutableBean)
				{
					MutableBeanContext context=new MutableBeanContext(key,value,this);
					((MutableBean)value).setMutableBeanContext(context);
				}
				
			memoryMap.put(key,new Wrapper(value));
			
				if(listener!=null)
					listener.loadedObject(key,value);
				
			return value;
		}
	
		public synchronized Object put(Object key,Object value)
		{
			Object prevVal=remove(key);
			
				if((value instanceof PermanentBean) || 
						(!(value instanceof Serializable) && !(value instanceof RemoveableBean)))
					memoryMap.put(key,value);
				else
				{
						if(value instanceof MutableBean)
						{
							MutableBeanContext context=new MutableBeanContext(key,value,this);
							((MutableBean)value).setMutableBeanContext(context);
						}
					memoryMap.put(key,new Wrapper(value));
				}
				
			entryCount++;
			return prevVal;
		}
	
		public synchronized Object remove(Object key)
		{
			Object prevVal=memoryMap.remove(key);
			
				if(prevVal instanceof Wrapper)
					prevVal=((Wrapper)prevVal).getValue();
			
			Object cacheValue=cacheMap.remove(key);
			
				if(prevVal==null)
					prevVal=cacheValue;
				
				if(prevVal!=null)
					entryCount--;

				if(prevVal instanceof MutableBean)
					((MutableBean)prevVal).setMutableBeanContext(null);
			return prevVal;
		}
	
		public synchronized void putAll(Map map)
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
	
		public synchronized void clear()
		{
			memoryMap.clear();
			cacheMap.clear();
			entryCount=0;
		}
		
		/**
		 * The set returned by this method is not backed by this map. That is changes
		 * in this map will not be reflected on the set and vice versa.
		 * @return
		 */
		public synchronized Set keys()
		{
			HashSet res=new HashSet(memoryMap.keySet());
			
			res.addAll(cacheMap.keySet());
			return res;
		}
	
		public Set keySet()
		{
			throw new UnsupportedOperationException("This operation is not supported by this map.");
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

		public CacheMapListener getCacheMapListener()
		{
			return listener;
		}
		

		public void setCacheMapListener(CacheMapListener listener)
		{
			this.listener=listener;
		}

		public boolean isCacheEnabled()
		{
			return cacheEnabled;
		}
		
		public synchronized void setCacheEnabled(boolean cacheEnabled)
		{
			this.cacheEnabled=cacheEnabled;
		}
		
		public void optimize()
		{
			cacheMap.optimize();
		}
		
		public synchronized void forceCache()
		{
			Iterator it=new HashSet(memoryMap.keySet()).iterator();
			Object key=null;
			Object value=null;
			
				while(it.hasNext())
				{
					key=it.next();
					value=memoryMap.get(key);
					
						if(!(value instanceof Wrapper))
							continue;
						
					value=((Wrapper)value).value;
					
						if(value instanceof RemoveableBean)
							continue;
						
						if(value instanceof PermanentBean)
							continue;
						
					memoryMap.remove(key);	
					cacheMap.put(key,value);
				}
		}
}
