package com.fw.persistence.conversion;

import com.fw.persistence.FieldDetails;

public abstract class AbstractConverter implements IConverter
{
	@Override
	public Object convertToDBType(Object source, FieldDetails fieldDetails)
	{
		return null;
	}
	
}
