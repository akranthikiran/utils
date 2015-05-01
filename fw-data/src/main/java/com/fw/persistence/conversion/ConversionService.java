package com.fw.persistence.conversion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fw.persistence.FieldDetails;

public class ConversionService
{
	private List<IConverter> converters = new ArrayList<>();
	
	private Map<Class<?>, IConverter> typeToConverter = new HashMap<>();
	
	public ConversionService()
	{
		converters.add(new NumberConverter());
		converters.add(new ClobConverter());
		converters.add(new EnumConverter());

		//String converter should be used as last option
		converters.add(new StringConverter());
	}
	
	public void addConverter(IConverter converter)
	{
		if(converter == null)
		{
			throw new NullPointerException("Converter can not be null");
		}
		
		this.converters.add(converter);
	}
	
	private IConverter getConverter(FieldDetails fieldDetails)
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
		IConverter converter = typeToConverter.get(converterType);
		
		if(converter != null)
		{
			return converter;
		}
		
		try
		{
			converter = (IConverter)converterType.newInstance();
		}catch(Exception ex)
		{
			throw new IllegalStateException("Failed to create converter of type: " + converterType.getName(), ex);
		}
		
		typeToConverter.put(converterType, converter);
		return converter;
	}
	
	public Object convertFromDataStore(Object from, FieldDetails fieldDetails)
	{
		IConverter converter = getConverter(fieldDetails);
		
		if(converter != null)
		{
			return converter.convertToTargetType(from, fieldDetails.getField().getType());
		}
		
		return convert(from, fieldDetails.getField().getType());
	}
	
	public Object convertToDataStore(Object from, FieldDetails fieldDetails)
	{
		IConverter fldConverter = getConverter(fieldDetails);
		
		if(fldConverter != null)
		{
			return fldConverter.convertToDBType(from, fieldDetails);
		}
		
		Object result = null;
		
		//check if any converter can handle conversion
		for(IConverter converter: converters)
		{
			result = converter.convertToDBType(from, fieldDetails);
			
			if(result != null)
			{
				return result;
			}
		}
		
		//if no converter is able to convert, simply return actual value
		return from;
	}
	
	protected Object convert(Object from, Class<?> targetType)
	{
		if(from == null)
		{
			return null;
		}
		
		if(targetType.isAssignableFrom(from.getClass()))
		{
			return from;
		}
		
		if((from instanceof String) && ((String)from).trim().length() == 0)
		{
			return null;
		}
		
		Object result = null;
		
		for(IConverter converter: converters)
		{
			result = converter.convertToTargetType(from, targetType);
			
			if(result != null)
			{
				return result;
			}
		}
		
		throw new DataConversionException("Failed to convert to '" + targetType.getName() + "' from value - " + from + "[" + from.getClass().getName() + "]");
	}
}
