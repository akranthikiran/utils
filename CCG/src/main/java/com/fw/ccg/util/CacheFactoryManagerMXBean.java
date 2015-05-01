package com.fw.ccg.util;

public interface CacheFactoryManagerMXBean 
{
	public int getActiveCount();
	public int getFreeCount();
	public void clean();
}
