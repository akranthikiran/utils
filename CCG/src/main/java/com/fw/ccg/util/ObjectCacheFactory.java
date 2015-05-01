package com.fw.ccg.util;

import java.lang.management.ManagementFactory;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ObjectCacheFactory<T> 
{
	private static Logger logger = LogManager.getLogger(ObjectCacheFactory.class);
	
	public static class CacheFactoryMBean implements CacheFactoryManagerMXBean
	{
		private ObjectCacheFactory<?> factory;
		
		public CacheFactoryMBean(ObjectCacheFactory<?> factory)
		{
			this.factory = factory;
		}
		
		@Override
		public int getActiveCount() 
		{
			return factory.activeObjects.size();
		}

		@Override
		public int getFreeCount() 
		{
			return factory.freeObjects.size();
		}

		@Override
		public void clean() 
		{
			factory.clean();
		}

	}

	LinkedList<T> freeObjects = new LinkedList<T>();
	IdentityHashMap<T, T> activeObjects = new IdentityHashMap<T, T>();
	private Class<T> type;
	
	private ReentrantLock lock = new ReentrantLock();
	
	public ObjectCacheFactory(String name, Class<T> type)
	{
		if(type == null)
		{
			throw new IllegalStateException("Type cannot be null");
		}
		
		this.type = type;
		registerMBean(name);
	}
	
	private void registerMBean(String name)
	{
		try
		{
			CacheFactoryMBean mbean = new CacheFactoryMBean(this);
			MBeanServer server = ManagementFactory.getPlatformMBeanServer();
			ObjectName objectName = new ObjectName("com.fw.objectCache:name=" + name);
			
			try
			{
				server.unregisterMBean(objectName);
				logger.warn("Object Cache MBean already exists with name: " + name);
			}catch(InstanceNotFoundException ex)
			{}
			
			server.registerMBean(mbean, objectName);
		}catch(Exception ex)
		{
			throw new IllegalStateException("An error occurred while registering bean", ex);
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public T getFreeInstance()
	{
		T instance = null;
		
		lock.lock();
		
		try
		{
			if(!freeObjects.isEmpty())
			{
				instance = freeObjects.removeFirst();
				
				if(instance instanceof ICacheableBean)
				{
					((ICacheableBean)instance).reinitalize(this);
				}
			}
			else
			{
				try
				{
					instance = type.newInstance();
				}catch(Exception ex)
				{
					throw new IllegalStateException("An error occurred while creating bean of type: " + type.getName(), ex);
				}
			}
	
			activeObjects.put(instance, instance);
			return instance;
		}finally
		{
			lock.unlock();
		}
	}
	
	public void free(T instance)
	{
		lock.lock();
		
		try
		{
			if(activeObjects.remove(instance) == null)
			{
				return;
			}
			
			freeObjects.add(instance);
		}finally
		{
			lock.unlock();
		}
	}
	
	void clean() 
	{
		lock.lock();
		freeObjects.clear();
		lock.unlock();
	}

}
