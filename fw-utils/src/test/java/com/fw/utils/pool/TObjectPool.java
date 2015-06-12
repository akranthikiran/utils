package com.fw.utils.pool;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TObjectPool
{
	@Test
	public void testObjectPoolLimits()
	{
		ObjectPool<String> pool = new ObjectPool<String>(String.class, 3);
		
		String s = null;
		
		//make sure objects are available till limit
		Assert.assertNotNull(s = pool.getObject());
		Assert.assertNotNull(pool.getObject());
		Assert.assertNotNull(pool.getObject());
		
		//once limit is crossed, object should not be available
		Assert.assertNull(pool.getObject());
		
		//free the object and then try
		pool.freeObject(s);
		Assert.assertNotNull(pool.getObject());
	}
}
