/**
 * 
 */
package com.fw.utils;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.fw.utils.test.ITestGroups;

/**
 * Unit tests for Common utils
 * @author akiran
 */
public class TCommonUtils
{
	private static SimpleDateFormat LOCAL_TIME_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");
	/**
	 * Test bean to check expressions
	 * @author akiran
	 */
	public static class TestBean
	{
		private String property1;
		private int property2;
		private TestBean nestedProp;

		private Date date = new Date();
		
		/**
		 * @param property1
		 * @param property2
		 */
		public TestBean(String property1, int property2, TestBean nestedProp)
		{
			this.property1 = property1;
			this.property2 = property2;
			this.nestedProp = nestedProp;
		}

		/**
		 * Gets value of property1 
		 * @return the property1
		 */
		public String getProperty1()
		{
			return property1;
		}

		/**
		 * Gets value of property2 
		 * @return the property2
		 */
		public int getProperty2()
		{
			if(property2 < 0)
			{
				throw new RuntimeException("Test exception for non-negative value");
			}
			
			return property2;
		}

		/**
		 * Gets value of nestedProp 
		 * @return the nestedProp
		 */
		public TestBean getNestedProp()
		{
			return nestedProp;
		}
		
		/**
		 * Gets value of date 
		 * @return the date
		 */
		public Date getDate()
		{
			return date;
		}
	}
	
	@BeforeClass(groups = ITestGroups.UNIT_TESTS)
	public void setup()
	{
		//call common-utils constructor for code coverage
		new CommonUtils();
	}
	
	/**
	 * Tests replace expressions with a bean
	 */
	@Test(groups = ITestGroups.UNIT_TESTS)
	public void testReplaceExpressions()
	{
		//other is some non-existing property
		String exprStr = "Property1=${property1} Property2=${property2} nested=${nestedProp.property1} other=${other}";
		
		Assert.assertEquals("Property1=val1 Property2=10 nested= other=", CommonUtils.replaceExpressions(new TestBean("val1", 10, null), exprStr, null));
		Assert.assertEquals("Property1= Property2=15 nested= other=", CommonUtils.replaceExpressions(new TestBean(null, 15, null), exprStr, null));
		
		//with nested prop
		Assert.assertEquals("Property1=Val1 Property2=15 nested= other=", CommonUtils.replaceExpressions(new TestBean("Val1", 15, new TestBean(null, 1, null)), exprStr, null));
		Assert.assertEquals("Property1=Val1 Property2=15 nested=Val2 other=", CommonUtils.replaceExpressions(new TestBean("Val1", 15, new TestBean("Val2", 1, null)), exprStr, null));
		
		//property with exception
		Assert.assertEquals("Property1=val1 Property2= nested= other=", CommonUtils.replaceExpressions(new TestBean("val1", -10, null), exprStr, null));
	}
	
	/**
	 * Tests replace expressions with a bean
	 */
	@Test(groups = ITestGroups.UNIT_TESTS)
	public void testReplaceExpressions_withMap()
	{
		//other is some non-existing property
		String exprStr = "Property1=${property1} Property2=${property2} nested=${nestedProp.property1} other=${other}";
		
		Map<String, Object> propMap = new HashMap<String, Object>();
		propMap.put("property1", "val1");
		propMap.put("property2", 10);
		propMap.put("nestedProp", new TestBean("nestVal", 10, null));
		
		Assert.assertEquals("Property1=val1 Property2=10 nested=nestVal other=", CommonUtils.replaceExpressions(propMap, exprStr, null));
	}
	
	/**
	 * Tests replace expressions using formatter
	 */
	@Test(groups = ITestGroups.UNIT_TESTS)
	public void testReplaceExpressions_WithFormatter()
	{
		//other is some non-existing property
		String exprStr = "Property1=${property1} Property2=${property2} date=${date}";
		
		IFormatter dateFormatter = new IFormatter()
		{
			public String convert(Object value)
			{
				if(value instanceof Date)
				{
					return LOCAL_TIME_FORMAT.format(value);
				}
				
				return "" + value;
			}
		};
		
		TestBean testBean = new TestBean("val1", 10, null);
		
		Assert.assertEquals("Property1=val1 Property2=10 date=" + LOCAL_TIME_FORMAT.format(testBean.date), 
				CommonUtils.replaceExpressions(testBean, exprStr, dateFormatter));
	}

	/**
	 * Tests indexOfElement using null and empty array
	 */
	@Test(groups = ITestGroups.UNIT_TESTS)
	public void testIndexOfElement_nullArr()
	{
		Assert.assertEquals(-1, CommonUtils.indexOfElement(null, null));
		Assert.assertEquals(-1, CommonUtils.indexOfElement(null, "val1"));
		
		Assert.assertEquals(-1, CommonUtils.indexOfElement(new String[]{}, null));
		Assert.assertEquals(-1, CommonUtils.indexOfElement(new String[]{}, "val1"));
	}
	
	/**
	 * Tests indexOfElement using proper array
	 */
	@Test(groups = ITestGroups.UNIT_TESTS)
	public void testIndexOfElement_indexes()
	{
		String arr[] = {"val1", "val2", "val1", null, "val3", null};
		
		Assert.assertEquals(0, CommonUtils.indexOfElement(arr, "val1"));
		Assert.assertEquals(1, CommonUtils.indexOfElement(arr, "val2"));
		
		Assert.assertEquals(3, CommonUtils.indexOfElement(arr, null));
		Assert.assertEquals(4, CommonUtils.indexOfElement(arr, "val3"));
		
		Assert.assertEquals(-1, CommonUtils.indexOfElement(arr, "val10"));
	}

	/**
	 * Tests isAssignable for different combinations
	 */
	@Test(groups = ITestGroups.UNIT_TESTS)
	public void testIsAssignable()
	{
		//test with same type
		Assert.assertTrue(CommonUtils.isAssignable(String.class, String.class));
		
		//test with wrong combinations
		Assert.assertFalse(CommonUtils.isAssignable(String.class, Integer.class));
		Assert.assertFalse(CommonUtils.isAssignable(Integer.class, String.class));
		Assert.assertFalse(CommonUtils.isAssignable(int.class, String.class));
		Assert.assertFalse(CommonUtils.isAssignable(String.class, int.class));
		
		//test with wrong primitive combinations
		Assert.assertFalse(CommonUtils.isAssignable(float.class, int.class));
		Assert.assertFalse(CommonUtils.isAssignable(float.class, Double.class));
		
		//check with wrapper/primitive types
		Assert.assertTrue(CommonUtils.isAssignable(Float.class, float.class));
		Assert.assertTrue(CommonUtils.isAssignable(double.class, Double.class));
	}
	
	@Test(groups = ITestGroups.UNIT_TESTS)
	public void testIsWrapper()
	{
		Assert.assertEquals(true, CommonUtils.isWrapperClass(Integer.class));
		Assert.assertEquals(true, CommonUtils.isWrapperClass(Double.class));
		
		Assert.assertEquals(false, CommonUtils.isWrapperClass(int.class));
		Assert.assertEquals(false, CommonUtils.isWrapperClass(String.class));
	}

	@Test(groups = ITestGroups.UNIT_TESTS)
	public void testIsEmptyArray()
	{
		Assert.assertEquals(true, CommonUtils.isEmptyArray((Object[])null));
		Assert.assertEquals(true, CommonUtils.isEmptyArray());
		Assert.assertEquals(true, CommonUtils.isEmptyArray(new Object[]{}));
		
		Assert.assertEquals(false, CommonUtils.isEmptyArray(1, 2, 4));
		Assert.assertEquals(false, CommonUtils.isEmptyArray("d", "Sds", "sd"));
	}

	@Test(groups = ITestGroups.UNIT_TESTS)
	public void testGetFieldValue() throws Exception
	{
		Field field = SomeBean.class.getDeclaredField("value");
		
		try
		{
			CommonUtils.getFieldValue(field, new SomeBean(10));
			Assert.fail("No exception is thrown even when field is not accessible");
		}catch(IllegalStateException ex)
		{
		}

		field.setAccessible(true);
		Assert.assertEquals(10, CommonUtils.getFieldValue(field, new SomeBean(10)));
	}
	
	/**
	 * Tests to map functionality
	 */
	@Test(groups = ITestGroups.UNIT_TESTS)
	public void testToMap()
	{
		Assert.assertTrue(CommonUtils.toMap((Object[])null).isEmpty());
		Assert.assertTrue(CommonUtils.toMap(new Object[]{}).isEmpty());
		
		Map<String, Object> nameToVal = CommonUtils.toMap("k1", 100, "k2", "200", "k3", 2.4);
		Assert.assertEquals(3, nameToVal.size());
		Assert.assertEquals(100, nameToVal.get("k1"));
		Assert.assertEquals("200", nameToVal.get("k2"));
		Assert.assertEquals(2.4, nameToVal.get("k3"));
	}

	/**
	 * Tests to map functionality error
	 */
	@Test(groups = ITestGroups.UNIT_TESTS, expectedExceptions = IllegalArgumentException.class)
	public void testToMap_err()
	{
		CommonUtils.toMap("k1", 1, "k2");
	}
	
	/**
	 * Tests toSet functionality
	 */
	@Test(groups = ITestGroups.UNIT_TESTS)
	public void testToSet()
	{
		Assert.assertTrue(CommonUtils.toSet((Object[])null).isEmpty());
		Assert.assertTrue(CommonUtils.toSet(new Object[]{}).isEmpty());
		
		Assert.assertEquals(CommonUtils.toSet("v1", "v2", "v1").size(), 2);
	}
}
