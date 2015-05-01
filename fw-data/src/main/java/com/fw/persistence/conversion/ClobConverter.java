package com.fw.persistence.conversion;


public class ClobConverter extends AbstractConverter
{
	@Override
	public Object convertToTargetType(Object source, Class<?> targetType)
	{
		if(!String.class.equals(targetType))
		{
			return null;
		}
		
		if(!(source instanceof char[]))
		{
			return null;
		}

		return new String((char[])source);
	}

}
