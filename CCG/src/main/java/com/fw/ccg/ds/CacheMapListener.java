package com.fw.ccg.ds;

public interface CacheMapListener
{
	public void objectCached(Object key,Object value);
	public void loadedObject(Object key,Object value);
	public void removedRemovable(Object key,Object value);
}
