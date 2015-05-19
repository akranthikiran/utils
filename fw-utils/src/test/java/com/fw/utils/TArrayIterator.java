/**
 * 
 */
package com.fw.utils;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.fw.utils.ArrayIterator;
import com.fw.utils.test.ITestGroups;

/**
 * Test cases for array iterator
 * @author akiran
 */
public class TArrayIterator
{
	/**
	 * Test the array iterator works with primitive array
	 */
	@Test(groups = ITestGroups.UNIT_TESTS)
	public void testForPrimitveArray()
	{
		int arr[] = {1, 3, 4, 6};
		
		Iterator<Object> it = new ArrayIterator(arr);
		int index = 0;
		
		while(it.hasNext())
		{
			Assert.assertEquals(it.next(), arr[index]);
			index++;
		}
		
		//ensure all the array elements are iterated
		Assert.assertEquals(index, arr.length);
	}

	/**
	 * Test array iterator works properly with object array
	 */
	@Test(groups = ITestGroups.UNIT_TESTS)
	public void testForObjectArray()
	{
		String arr[] = {"val1", "val2", "val3"};
		
		Iterator<Object> it = new ArrayIterator(arr);
		int index = 0;
		
		while(it.hasNext())
		{
			Assert.assertEquals(it.next(), arr[index]);
			index++;
		}

		//ensure all the array elements are iterated
		Assert.assertEquals(index, arr.length);
	}

	/**
	 * Test the bounds test is in place
	 */
	@Test(groups = ITestGroups.UNIT_TESTS, expectedExceptions = NoSuchElementException.class)
	public void testBounds()
	{
		int arr[] = {1, 3, 4, 6};
		
		Iterator<Object> it = new ArrayIterator(arr);
		
		//as there is no check for more-elements this should throw exception
		while(true)
		{
			it.next();
		}
		
	}
	
	/**
	 * Ensure remove on iterator throws exception
	 */
	@Test(groups = ITestGroups.UNIT_TESTS, expectedExceptions = UnsupportedOperationException.class)
	public void testRemove()
	{
		int arr[] = {1, 3, 4, 6};
		
		Iterator<Object> it = new ArrayIterator(arr);
		it.next();
		
		//remove should not be supported by array iterator
		it.remove();
	}
	
	/**
	 * Ensure passing non-arrays throws exceptions 
	 */
	@Test(groups = ITestGroups.UNIT_TESTS, expectedExceptions = IllegalArgumentException.class)
	public void testForNonArray()
	{
		new ArrayIterator("Some object");
	}
}
