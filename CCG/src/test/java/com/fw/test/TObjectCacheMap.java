package com.fw.test;

import java.util.Arrays;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.fw.ccg.util.ObjectCacheMap;

public class TObjectCacheMap
{
	@Test
	public void testSizeLimit()
	{
		ObjectCacheMap<String, Integer> map = new ObjectCacheMap<String, Integer>();
		map.setCacheSize(3);
		
		map.put("1", 1);
		map.put("2", 2);
		map.put("3", 3);
		
		Assert.assertEquals(3, map.size());
		
		map.put("4", 4);
		map.put("5", 5);
		Assert.assertEquals(3, map.size());
	}
	
	@Test
	public void testOrderAfterAdd()
	{
		ObjectCacheMap<String, Integer> map = new ObjectCacheMap<String, Integer>();
		map.setCacheSize(3);
		
		map.put("1", 1);
		map.put("2", 2);
		map.put("3", 3);
		
		Assert.assertTrue(Arrays.equals(new Integer[]{1, 2, 3}, map.values().toArray(new Integer[0])));
		Assert.assertTrue(Arrays.equals(new String[]{"1", "2", "3"}, map.keySet().toArray(new String[0])));
		
		map.put("4", 4);
		Assert.assertTrue(Arrays.equals(new Integer[]{2, 3, 4}, map.values().toArray(new Integer[0])));
		Assert.assertTrue(Arrays.equals(new String[]{"2", "3", "4"}, map.keySet().toArray(new String[0])));
		
		map.put("5", 5);
		Assert.assertTrue(Arrays.equals(new Integer[]{3, 4, 5}, map.values().toArray(new Integer[0])));
		Assert.assertTrue(Arrays.equals(new String[]{"3", "4", "5"}, map.keySet().toArray(new String[0])));
	}
	

	@Test
	public void testRevisit()
	{
		ObjectCacheMap<String, Integer> map = new ObjectCacheMap<String, Integer>();
		map.setCacheSize(3);
		
		map.put("1", 1);
		map.put("2", 2);
		map.put("3", 3);
		
		System.out.println(map);
		
		Assert.assertTrue(Arrays.equals(new Integer[]{1, 2, 3}, map.values().toArray(new Integer[0])));
		Assert.assertTrue(Arrays.equals(new String[]{"1", "2", "3"}, map.keySet().toArray(new String[0])));

		Assert.assertEquals((Integer)2, map.revisitKey("2"));
		Assert.assertTrue(Arrays.equals(new Integer[]{1, 3, 2}, map.values().toArray(new Integer[0])));
		Assert.assertTrue(Arrays.equals(new String[]{"1", "3", "2"}, map.keySet().toArray(new String[0])));
		
		System.out.println(map);
	}
}
