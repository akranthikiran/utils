package com.fw.persistence.conversion;

import com.fw.persistence.FieldDetails;

public interface IConverter
{
	public Object convertToTargetType(Object source, Class<?> targetType);
	public Object convertToDBType(Object source, FieldDetails fieldDetails);
}
