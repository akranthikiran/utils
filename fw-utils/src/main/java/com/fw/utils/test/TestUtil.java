/**
 * 
 */
package com.fw.utils.test;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fw.utils.CommonUtils;

/**
 * 
 * @author akiran
 */
public class TestUtil
{
	private static Logger logger = LogManager.getLogger(TestUtil.class);
	
	private static Random random = new Random();

	/**
	 * Creates a temporary folder and returns the path
	 * @return
	 * @throws IOException
	 */
	public static String createTempFolder() throws IOException
	{
		File file = File.createTempFile("Test-res", "");
		File userTempFolder = file.getParentFile();
		
		file.delete();
		File tmpFolder = new File(userTempFolder, "tmp-" + System.currentTimeMillis() + random.nextInt());
		tmpFolder.mkdirs();
		
		return tmpFolder.getPath();
	}
	
	public static String makeResourceCopy(String resource) throws IOException
	{
		return makeResourceCopy(resource, null);
	}
	
	/**
	 * Copies the specified resource as temp file and returns the copied file path
	 * @param resource
	 * @param destFile File as which resource should be copied
	 * @return
	 * @throws IOException
	 */
	public static String makeResourceCopy(String resource, String destFile) throws IOException
	{
		InputStream is = TestUtil.class.getResourceAsStream(resource);
		File file = (destFile != null) ? new File(destFile) : File.createTempFile("Test-res", "test");
		
		FileOutputStream fos = new FileOutputStream(file);
		IOUtils.copy(is, fos);
		
		fos.flush();
		fos.close();
		is.close();
	
		logger.debug("Copied resource '{}' as file '{}'", resource, file.getPath());
		
		return file.getAbsolutePath();
	}
	

	/**
	 * Calls values() and valueOf() on specified enum type. And also calls read method of all properties. This will add code coverage for target enum
	 * @param enumType
	 * @throws NoSuchMethodException 
	 * @throws SecurityException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	public static void coverEnumTest(Class<? extends Enum<?>>... enumTypes) throws SecurityException, NoSuchMethodException, IllegalAccessException, InvocationTargetException
	{
		Method valueOfMet = null;
		Enum<?> vals[] = null;
		PropertyDescriptor props[] = null;
		
		for(Class<? extends Enum<?>> enumType : enumTypes)
		{
			valueOfMet = enumType.getDeclaredMethod("valueOf", String.class);
			vals = (Enum[])enumType.getEnumConstants();
			props = PropertyUtils.getPropertyDescriptors(enumType);
			
			for(Enum<?> e : vals)
			{
				valueOfMet.invoke(null, e.name());
				
				//invoke read properties if any
				for(PropertyDescriptor prop : props)
				{
					if(prop.getReadMethod() != null)
					{
						prop.getReadMethod().invoke(e);
					}
				}
			}
		}
	}
	
	/**
	 * Populate random values for each writeable property of specified bean
	 * 
	 * @param bean
	 * @return
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws InstantiationException
	 */
	public static <T> T populateRandomValues(T bean) throws IllegalAccessException, InvocationTargetException, InstantiationException
	{
		Object value = null;
		PropertyDescriptor properties[] = PropertyUtils.getPropertyDescriptors(bean.getClass());
		
		//loop through properties
		for(PropertyDescriptor prop : properties)
		{
			//if it writeable property create random value
			if(prop.getWriteMethod() != null)
			{
				value = getValueOfType(prop.getPropertyType());
				prop.getWriteMethod().invoke(bean, value);
			}
		}
		
		return bean;
	}

	static boolean isPrimitiveType(Class<?> type)
	{
		if(type.isPrimitive())
		{
			return true;
		}
		
		return CommonUtils.isWrapperClass(type);
	}
	
	static Object getValueOfType(Class<?> type) throws InstantiationException, IllegalAccessException
	{
		if(isPrimitiveType(type))
		{
			if(boolean.class.equals(type) || Boolean.class.equals(type))
			{
				return true;
			}
			if(byte.class.equals(type) || Byte.class.equals(type))
			{
				return (byte)random.nextInt();
			}
			if(char.class.equals(type) || Character.class.equals(type))
			{
				return (char)('A' + random.nextInt(26));
			}
			if(short.class.equals(type) || Short.class.equals(type))
			{
				return (short)random.nextInt();
			}
			if(int.class.equals(type) || Integer.class.equals(type))
			{
				return random.nextInt();
			}
			if(long.class.equals(type) || Long.class.equals(type))
			{
				return random.nextLong();
			}
			if(float.class.equals(type) || Float.class.equals(type))
			{
				return random.nextFloat();
			}
			
			return random.nextDouble();
		}
		
		if(String.class.equals(type))
		{
			return "" + random.nextLong();
		}
		
		if(type.isArray())
		{
			return Array.newInstance(type.getComponentType(), 1);
		}
		
		if(Collection.class.isAssignableFrom(type))
		{
			if(type.isAssignableFrom(HashSet.class))
			{
				return new HashSet<Object>();
			}

			if(type.isAssignableFrom(ArrayList.class))
			{
				return new ArrayList<Object>();
			}
		}
		
		if(Map.class.isAssignableFrom(type))
		{
			if(type.isAssignableFrom(HashMap.class))
			{
				return new HashMap<Object, Object>();
			}
		}
		
		//if type is enum, return one of the enum constant
		if(type.isEnum())
		{
			Object values[] = type.getEnumConstants();
			return values[random.nextInt(values.length)];
		}
		
		return type.newInstance();
	}
	
	/**
	 * Invoke all getters and compare the value obtain against the value expected
	 * @param bean
	 * @param propMap
	 * @param expectedValueMap
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	private static void validateProperties(Object bean, Map<String, PropertyDescriptor> propMap, Map<String, Object> expectedValueMap) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		Object propValue = null, expectedValue = null;
		
		for(PropertyDescriptor prop : propMap.values())
		{
			propValue = null;
			expectedValue = expectedValueMap.get(prop.getName());
			
			if(prop.getReadMethod() != null)
			{
				propValue = prop.getReadMethod().invoke(bean);

				if(expectedValue != null)
				{
					if(!ObjectUtils.equals(expectedValue, propValue))
					{
						String mssg = String.format("Property test failed for property '%s' of bean '%s'. Expected - <%s>, Found - <%s>", 
								prop.getName(), bean.getClass().getName(), expectedValue, propValue);
						
						throw new IllegalStateException(mssg);
					}
				}
			}
		}
	}
	
	/**
	 * Invokes all setters/getters of the bean. The value set by setter is cross-validated
	 * by invoking getter.
	 * @param bean
	 * @param propMap
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws InstantiationException
	 * @throws NoSuchMethodException 
	 * @throws SecurityException 
	 */
	private static void testBean(Object bean, Map<String, PropertyDescriptor> propMap) throws IllegalAccessException, InvocationTargetException, InstantiationException, SecurityException, NoSuchMethodException
	{
		Object value = null;
		Map<String, Object> valueMap = new HashMap<String, Object>();
		
		logger.debug("Testing bean: " + bean.toString());
		
		//loop through properties
		for(PropertyDescriptor prop : propMap.values())
		{
			//if bean is exception, skip stackTrace property
			if((bean instanceof Exception) && "stackTrace".equals(prop.getName()))
			{
				continue;
			}
			
			logger.debug("Testing property - " + prop.getName());
			
			//if it writeable property create random value
			if(prop.getWriteMethod() != null)
			{
				value = getValueOfType(prop.getPropertyType());
				
				try
				{
					prop.getWriteMethod().invoke(bean, value);
				}catch(Exception ex)
				{
					throw new IllegalStateException("An error occured while writing property - " + prop.getName(), ex);
				}
				
				//keep track values set on the map
				valueMap.put(prop.getName(), value);
			}
		}
		
		//validate propery values set with getters
		validateProperties(bean, propMap, valueMap);
		
		
		if(bean instanceof Cloneable)
		{
			try
			{
				Method cloneMet = bean.getClass().getDeclaredMethod("clone");
				cloneMet.setAccessible(true);
				cloneMet.invoke(bean);
			}catch(Exception ex)
			{
				throw new IllegalStateException("An error occured while invoking clone() of cloneable type - " + bean.getClass().getName(), ex);
			}
		}
		
		//invoke equals with same object
		if(!bean.equals(bean))
		{
			throw new IllegalStateException("equals() failed. Bean is not equal to itself");
		}
		
		if(bean.equals("some val"))
		{
			throw new IllegalStateException("equals() failed. Bean is equal to string");
		}
		
		bean.hashCode();
	}
	
	/**
	 * Tested the specified type by invoking all the constructors marked by
	 * {@link BeanConstructor} and default-constructor. For non-default constructors
	 * the properties passed to constructor are validated by invoking getter.
	 * 
	 * And also testBean() method is called which invoked all available setters and getters.
	 * @param type
	 * @param propMap
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException 
	 * @throws SecurityException 
	 */
	private static void testBeanType(Class<?> type, Map<String, PropertyDescriptor> propMap) throws InstantiationException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException
	{
		Constructor<?> constructors[] = type.getConstructors();
		
		if(constructors.length == 0)
		{
			logger.debug("No constructors are found for testing type - {}", type.getName());
			return;
		}
		
		Class<?> argTypes[] = null;
		BeanConstructor beanConstructor = null;
		Object argValues[] = null;
		Object bean = null;
		String beanConstrArgs[] = null;
		Map<String, Object> argValueMap = new HashMap<String, Object>();
		
		for(int i = 0; i < constructors.length; i++)
		{
			argTypes = constructors[i].getParameterTypes();
			beanConstructor = constructors[i].getAnnotation(BeanConstructor.class);
	
			//if constructor is default constructor
			if(argTypes.length == 0)
			{
				bean = constructors[i].newInstance();
				
				testBean(bean, propMap);
				continue;
			}
			
			//for non-default constructor @BeanConstructor is not defined
			if(beanConstructor == null)
			{
				continue;
			}
			
			beanConstrArgs = beanConstructor.arguments();
			
			//validate @BeanConstructor 
			if(argTypes.length != beanConstrArgs.length)
			{
				throw new IllegalStateException("Encountered mismatch between constructor argument count and @BeanConstructor argument count in type - " + type.getName());
			}

			argValues = new Object[argTypes.length];
			argValueMap.clear();
			
			//create argument values
			for(int j = 0; j < argTypes.length; j++)
			{
				if( !argTypes[j].equals(propMap.get(beanConstrArgs[j]).getPropertyType()) )
				{
					throw new IllegalStateException(String.format("Encountered mismatch type between constructor '%s' and @BeanConstructor in type '%s' at index %s",
							constructors[i], type.getName(), j));
				}
				
				argValues[j] = getValueOfType(argTypes[j]);
				argValueMap.put(beanConstrArgs[j], argValues[j]);
			}
			
			bean = constructors[i].newInstance(argValues);
			
			//test properties passed to constructor
			validateProperties(bean, propMap, argValueMap);
			
			//test the bean properties directly
			testBean(bean, propMap);
		}
	}
	
	private static void testInterfaceType(Class<?> type) throws IllegalAccessException
	{
		Field fields[] = type.getFields();
		
		for(Field field : fields)
		{
			field.get(null);
		}
	}
	
	/**
	 * Executes setters and getters of beans specified and validates the properties
	 * are set and fetched properly
	 * @param beans
	 * @throws IllegalAccessException 
	 */
	public static void testBeanTypes(Class<?>... beanTypes) throws IllegalAccessException
	{
		PropertyDescriptor properties[] = null;
		Map<String, PropertyDescriptor> propMap = new HashMap<String, PropertyDescriptor>();

		for(Class<?> type : beanTypes)
		{
			logger.debug("Testing type - {}", type.getName());
			
			if(type.isInterface())
			{
				testInterfaceType(type);
				continue;
			}
			
			properties = PropertyUtils.getPropertyDescriptors(type);
			propMap.clear();
		
			for(PropertyDescriptor prop : properties)
			{
				propMap.put(prop.getName(), prop);
			}
			
			try
			{
				testBeanType(type, propMap);
			}catch(Exception ex)
			{
				throw new IllegalStateException("An error occurred while testing bean-type: " + type.getName(), ex);
			}
			
			logger.debug("Testing completed for type {}", type.getName());
		}
	}
	
	/**
	 * Replaces expression in specified file with values from valueMap
	 * @param file
	 * @param valueMap
	 * @throws IOException
	 */
	public static void replaceExpressions(String file, Map<String, ?> valueMap) throws IOException
	{
		String content = new String(FileUtils.readFileToByteArray(new File(file)));
		
		content = CommonUtils.replaceExpressions(valueMap, content, null);
		
		FileUtils.writeStringToFile(new File(file), content);
	}
}
