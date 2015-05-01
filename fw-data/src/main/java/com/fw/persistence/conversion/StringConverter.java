package com.fw.persistence.conversion;

public class StringConverter extends AbstractConverter
{
	@Override
	public Object convertToTargetType(Object source, Class<?> targetType)
	{
		if(!String.class.isAssignableFrom(targetType))
		{
			return null;
		}

		return source.toString();
	}
	
}
