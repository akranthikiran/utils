package com.fw.persistence.conversion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fw.persistence.FieldDetails;
import com.fw.persistence.annotations.DataType;
import com.fw.utils.ConvertUtils;

/**
 * Service to convert object of one type into other.
 * @author akiran
 */
public class ConversionService
{
	private static Logger logger = LogManager.getLogger(ConversionService.class);
	
	private List<IPersistenceConverter> converters = new ArrayList<>();
	
	private Map<Class<?>, IPersistenceConverter> typeToConverter = new HashMap<>();
	
	/**
	 * Adds a converter that can modify data of one particular type to other
	 * @param converter
	 */
	public void addConverter(IPersistenceConverter converter)
	{
		if(converter == null)
		{
			throw new NullPointerException("Converter can not be null");
		}
		
		this.converters.add(converter);
	}
	
	/**
	 * Fetches the converter for specified field, if it is explcitly defined on 
	 * target java field
	 * @param fieldDetails
	 * @return
	 */
	private IPersistenceConverter getConverter(FieldDetails fieldDetails)
	{
		//TODO: Check why field details needs to be null. Is there any substitute.
		if(fieldDetails == null || fieldDetails.getField() == null)
		{
			return null;
		}
		
		FieldConverter fieldConverter = fieldDetails.getField().getAnnotation(FieldConverter.class);
		
		if(fieldConverter == null)
		{
			return null;
		}
		
		Class<?> converterType = fieldConverter.converterType();
		IPersistenceConverter converter = typeToConverter.get(converterType);
		
		if(converter != null)
		{
			return converter;
		}
		
		try
		{
			converter = (IPersistenceConverter)converterType.newInstance();
		}catch(Exception ex)
		{
			throw new IllegalStateException("Failed to create converter of type: " + converterType.getName(), ex);
		}
		
		typeToConverter.put(converterType, converter);
		return converter;
	}
	
	/**
	 * Converts specified db object to matching java type
	 * @param dbObject
	 * @param fieldDetails
	 * @return
	 */
	public Object convertToJavaType(Object dbObject, FieldDetails fieldDetails)
	{
		//fetch field specific converter
		IPersistenceConverter converter = getConverter(fieldDetails);
		
		//if field specific converter is present
		if(converter != null)
		{
			return converter.convertToJavaType(dbObject, fieldDetails.getDbDataType(), fieldDetails.getField().getType());
		}
		
		//try to convert using default converters and in generic way
		return convert(dbObject, fieldDetails.getDbDataType(), fieldDetails.getField().getType());
	}
	
	/**
	 * Converts specified java object into target db type
	 * @param javaObj
	 * @param fieldDetails
	 * @return
	 */
	public Object convertToDBType(Object javaObj, FieldDetails fieldDetails)
	{
		//fieldDetails will be null, when conversion is needed for values in conditions
		if(fieldDetails == null)
		{
			return javaObj;
		}
		
		//fetch field specific converter
		IPersistenceConverter fldConverter = getConverter(fieldDetails);
		
		//if field specific converter is present
		if(fldConverter != null)
		{
			return fldConverter.convertToDBType(javaObj, fieldDetails.getDbDataType());
		}
		
		Object result = null;
		
		//check if any converter can handle conversion
		for(IPersistenceConverter converter: converters)
		{
			result = converter.convertToDBType(javaObj, fieldDetails.getDbDataType());
			
			//if conversion was successful
			if(result != null)
			{
				return result;
			}
		}
		
		//if no converter is able to convert, simply return actual value
		return javaObj;
	}
	
	/**
	 * Converts specified db object into specified java type using default converters 
	 * @param dbObject
	 * @param dbDataType
	 * @param targetType
	 * @return
	 */
	private Object convert(Object dbObject, DataType dbDataType, Class<?> targetType)
	{
		//if from value is null
		if(dbObject == null)
		{
			return null;
		}
		
		//if db object is same as target java type
		if(targetType.isAssignableFrom(dbObject.getClass()))
		{
			return dbObject;
		}
		
		//if db value is string and its empty
		if((dbObject instanceof String) && ((String)dbObject).trim().length() == 0)
		{
			return null;
		}
		
		Object result = null;
		
		//check if any of the default converters can convert current db object
		for(IPersistenceConverter converter: converters)
		{
			result = converter.convertToJavaType(dbObject, dbDataType, targetType);
			
			//if conversion was success
			if(result != null)
			{
				return result;
			}
		}

		//if in built coverters are not able to convert, use generic utils to convert
		try
		{
			return ConvertUtils.convert(dbObject, targetType);
		}catch(Exception ex)
		{
			logger.warn("An error occurred while converting '{}' to type '{}' in generic way. Error - {}", dbObject, targetType.getName(), ex);
		}
		
		throw new DataConversionException("Failed to convert to '" + targetType.getName() + "' from value - " + dbObject + "[" + dbObject.getClass().getName() + "]");
	}
}
