package com.fw.ccg.util;

import java.util.HashMap;

public class AbstractObjectCacheFactory 
{
	private static HashMap<String, ObjectCacheFactory<?>> map = new HashMap<String, ObjectCacheFactory<?>>();
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T> ObjectCacheFactory<T> getObjectCacheFactory(Class<T> type)
	{
		String name = type.getSimpleName();
		ObjectCacheFactory<T> factory = (ObjectCacheFactory)map.get(name);
		
		if(factory == null)
		{
			factory = new ObjectCacheFactory<T>(name, type);
			map.put(name, factory);
		}
		
		return factory;
	}
}
