/**
 * 
 */
package com.fw.utils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Contains common utility methods
 * @author akiran
 */
public class CommonUtils
{
	private static Logger logger = LogManager.getLogger(CommonUtils.class);
	
	private static Map<Class<?>, Class<?>> wrapperToPrimitive = new HashMap<Class<?>, Class<?>>();
	private static Map<Class<?>, Class<?>> primitiveToWrapper = new HashMap<Class<?>, Class<?>>();
	
	static
	{
		addMapping(Boolean.class, boolean.class);
		addMapping(Byte.class, byte.class);
		addMapping(Character.class, char.class);
		addMapping(Short.class, short.class);
		addMapping(Integer.class, int.class);
		addMapping(Long.class, long.class);
		addMapping(Float.class, float.class);
		addMapping(Double.class, double.class);
	}

	private static void addMapping(Class<?> wrapperType, Class<?> primType)
	{
		wrapperToPrimitive.put(wrapperType, primType);
		primitiveToWrapper.put(primType, wrapperType);
	}
	
	private static final Pattern EXPRESSION_PATTERN = Pattern.compile("\\$\\{([\\w\\.\\(\\)]+)\\}");

	/**
	 * Returns true if specified type is primitive wrapper type
	 * @param type
	 * @return
	 */
	public static boolean isWrapperClass(Class<?> type)
	{
		return wrapperToPrimitive.containsKey(type);
	}
	
	/**
	 * Checks if object of type "from" can be assigned to type "to". This method considers auto-boxing also
	 * @param from
	 * @param to
	 * @return
	 */
	public static boolean isAssignable(Class<?> from, Class<?> to)
	{
		//if its directly assignable
		if(to.isAssignableFrom(from))
		{
			return true;
		}
		
		//if from is wrapper
		if(wrapperToPrimitive.containsKey(from))
		{
			return wrapperToPrimitive.get(from).isAssignableFrom(to);
		}
		
		//if from is primitive
		if(primitiveToWrapper.containsKey(from))
		{
			return primitiveToWrapper.get(from).isAssignableFrom(to);
		}

		return false;
	}

	/**
	 * Replaces expressions in "expressionString" with the property values of "bean".
	 * The expressions should be of format ${<property-name>}. Where property name can
	 * be a simple property, nested property or indexed property as defined in apache's
	 * BeanUtils.getProperty
	 * 
	 * @param bean
	 * @param expressionString
	 * @param formatter Optional. Formatter to format property values.
	 * @return
	 */
	public static String replaceExpressions(Object bean, String expressionString, IFormatter formatter)
	{
		Matcher matcher = EXPRESSION_PATTERN.matcher(expressionString);
		StringBuffer result = new StringBuffer();
		Object value = null;
		
		//loop through the expressions
		while(matcher.find())
		{
			try
			{
				value = PropertyUtils.getProperty(bean, matcher.group(1));
			}catch(Exception ex)
			{
				//in case of error log a warning and ignore
				logger.warn("An error occurred while parsing expression: " + matcher.group(1), ex);
				value = "";
			}
			
			//if value is null, make it into empty string to avoid exceptions
			if(value == null)
			{
				value = "";
			}
			//if value is not null and formatter is specified
			else if(formatter != null)
			{
				value = formatter.convert(value);
			}
			
			//replace expression with property value
			matcher.appendReplacement(result, Matcher.quoteReplacement(value.toString()));
		}
		
		matcher.appendTail(result);
		
		return result.toString();
	}

	/**
	 * Finds index of "element" in specified "array". If not found returns -1.
	 * @param array
	 * @param element
	 * @return
	 */
	public static <T> int indexOfElement(T array[], T element)
	{
		if(array == null || array.length == 0)
		{
			return -1;
		}
		
		for(int  i = 0; i < array.length; i++)
		{
			if(element == null)
			{
				if(array[i] == null)
				{
					return i;
				}
			}
			else if(element.equals(array[i]))
			{
				return i;
			}
		}
		
		return -1;
	}
	
	/**
	 * Converts specified array of elements into Set and returns the same. 
	 * Duplicate values will get filtered by set.
	 * 
	 * @param elements
	 * @return
	 */
	public static <E> Set<E> toSet(E... elements)
	{
		if(elements == null)
		{
			return new HashSet<E>();
		}
		
		return new HashSet<E>(Arrays.asList(elements));
	}
	
	/**
	 * Checks if the specified array is empty (null or zero length array)
	 * @param array
	 * @return
	 */
	public static boolean isEmptyArray(Object... array)
	{
		if(array == null)
		{
			return true;
		}
		
		if(array.length == 0)
		{
			return true;
		}
		
		return false;
	}
	
	/**
	 * Creates a map out of the key value pairs provided
	 * @param keyValues Key Value pairs
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <K, V> Map<K, V> toMap(Object... keyValues)
	{
		//if array is empty, return empty map
		if(isEmptyArray(keyValues))
		{
			return Collections.emptyMap();
		}
		
		//if key values are not provided in pairs
		if(keyValues.length % 2 != 0)
		{
			throw new IllegalArgumentException("Key values are not provided in pairs");
		}
		
		Map<K, V> keyToVal = new HashMap<K, V>();
		
		for(int i = 0; i < keyValues.length; i += 2)
		{
			((Map)keyToVal).put(keyValues[i], keyValues[i + 1]);
		}
		
		return keyToVal;
	}
	
	/**
	 * Fetches the specified field value of the bean. This field should be made accessible
	 * before calling this method.
	 * 
	 * @param field
	 * @param bean
	 * @return
	 */
	public static Object getFieldValue(Field field, Object bean)
	{
		try
		{
			return field.get(bean);
		}catch(Exception ex)
		{
			throw new IllegalStateException("An error occurred while fetching field value - " + field.getName(), ex);
		}
	}

	
	
}
