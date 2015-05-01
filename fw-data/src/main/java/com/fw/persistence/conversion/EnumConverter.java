package com.fw.persistence.conversion;

import com.fw.persistence.FieldDetails;

public class EnumConverter extends AbstractConverter
{
	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public Object convertToTargetType(Object source, Class<?> targetType)
	{
		if(!targetType.isEnum())
		{
			return null;
		}
		
		if(!(source instanceof String))
		{
			return null;
		}

		return Enum.valueOf((Class)targetType, (String)source);
	}

	@Override
	public Object convertToDBType(Object source, FieldDetails fieldDetails)
	{
		if(!(source instanceof Enum))
		{
			return null;
		}
		
		return ((Enum<?>)source).name();
	}
}
