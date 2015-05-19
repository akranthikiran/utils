/**
 * 
 */
package com.fw.utils;

import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.converters.ByteConverter;
import org.apache.commons.beanutils.converters.CharacterConverter;
import org.apache.commons.beanutils.converters.DoubleConverter;
import org.apache.commons.beanutils.converters.FloatConverter;
import org.apache.commons.beanutils.converters.IntegerConverter;
import org.apache.commons.beanutils.converters.LongConverter;
import org.apache.commons.beanutils.converters.ShortConverter;

/**
 * By default, apache converter-utils returns default value when invalid values are passed. Instead of throwing exceptions.
 * 
 * This class overrides that behavior by registering converters without default values. Thus these converters throws exceptions
 * when invalid value is passed
 *  
 * @author akiran
 */
public class DataConverter
{
	
	private ConvertUtilsBean convertUtilsBean;
	
	/**
	 * 
	 */
	public DataConverter()
	{
		convertUtilsBean = new ConvertUtilsBean();
		
		register(new CharacterConverter(), Character.class, char.class);
		register(new ByteConverter(), Byte.class, byte.class);
		register(new ShortConverter(), Short.class, short.class);
		register(new IntegerConverter(), Integer.class, int.class);
		register(new LongConverter(), Long.class, long.class);

		register(new FloatConverter(), Float.class, float.class);
		register(new DoubleConverter(), Double.class, double.class);
	}
	
	/**
	 * Registers specified converter for specified classes
	 * @param converter
	 * @param classes
	 */
	private void register(Converter converter, Class<?>... classes)
	{
		for(Class<?> cls: classes)
		{
			convertUtilsBean.register(converter, cls);
		}
	}
	
	/**
	 * Converts given value to given target type.
	 * @param value
	 * @param toType
	 * @return
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public <T> T convert(Object value, Class<T> toType)
	{
		//if string needs to be converted to enum
		if(toType.isEnum() && (value instanceof String))
		{
			return (T)Enum.valueOf((Class)toType, (String)value);
		}
		
		//use conversion util to convert
		Object res = convertUtilsBean.convert(value, toType);
		
		//if result is null
		if(res == null)
		{
			return null;
		}
		
		//sometimes conversion util is returning input value where conversion is not possible
		//	so this extra check is required
		if(CommonUtils.isAssignable(res.getClass(), toType))
		{
			return (T)res;
		}

		//throw exception if not convertible
		throw new ConversionException(String.format("Failed to convert value '%s' to type %s", value, toType.getName()));
	}
}

