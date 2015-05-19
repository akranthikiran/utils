/**
 * 
 */
package com.fw.utils;

import junit.framework.Assert;

import org.testng.annotations.Test;

import com.fw.utils.ObjectWrapper;
import com.fw.utils.test.ITestGroups;

/**
 * Unit tests for object wrapper
 * @author akiran
 */
public class TObjectWrapper
{
	private void testObjectWrapper(ObjectWrapper<String> wrapper, String initialValue)
	{
		Assert.assertEquals(initialValue, wrapper.getValue());
		
		wrapper.setValue("Some Value");
		Assert.assertEquals("Some Value", wrapper.getValue());
	}
	
	@Test(groups = ITestGroups.UNIT_TESTS)
	public void test()
	{
		testObjectWrapper(new ObjectWrapper<String>(), null);
		testObjectWrapper(new ObjectWrapper<String>(null), null);
		testObjectWrapper(new ObjectWrapper<String>("123"), "123");
	}
}
