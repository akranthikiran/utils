package com.fw.persistence.conversion;

import com.fw.ccg.util.CCGUtility;

public class NumberConverter extends AbstractConverter
{
	@Override
	public Object convertToTargetType(Object source, Class<?> targetType)
	{
		if(targetType.isPrimitive())
		{
			targetType = CCGUtility.getWrapperClass(targetType);
		}
		
		if(!Number.class.isAssignableFrom(targetType))
		{
			return null;
		}
		
		Number sourceValue = null;
		
		if(source instanceof Number)
		{
			sourceValue = (Number)source;
		}
		else if(source instanceof String)
		{
			try
			{
				sourceValue = Double.parseDouble((String)source);
			}catch(Exception ex)
			{
				throw new DataConversionException("Failed to convert string value into '" + targetType.getName() + "'. Value: " + source);
			}
		}
		
		if(byte.class.equals(targetType) || Byte.class.equals(targetType))
		{
			return sourceValue.byteValue();
		}
		
		if(short.class.equals(targetType) || Short.class.equals(targetType))
		{
			return sourceValue.shortValue();
		}

		if(int.class.equals(targetType) || Integer.class.equals(targetType))
		{
			return sourceValue.intValue();
		}
		
		if(long.class.equals(targetType) || Long.class.equals(targetType))
		{
			return sourceValue.longValue();
		}

		if(float.class.equals(targetType) || Float.class.equals(targetType))
		{
			return sourceValue.floatValue();
		}

		if(double.class.equals(targetType) || Double.class.equals(targetType))
		{
			return sourceValue.floatValue();
		}
		
		throw new IllegalStateException("Unknown number type encountered: " + targetType.getName());
	}
	
}
