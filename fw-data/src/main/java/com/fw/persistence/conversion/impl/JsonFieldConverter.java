package com.fw.persistence.conversion.impl;

import com.fw.persistence.FieldDetails;
import com.fw.persistence.conversion.IConverter;

import conm.fw.common.util.JsonWrapper;

public class JsonFieldConverter implements IConverter
{
	@Override
	public Object convertToDBType(Object object, FieldDetails fieldDetails)
	{
		return JsonWrapper.format(object);
	}

	@Override
	public Object convertToTargetType(Object json, Class<?> target)
	{
		return JsonWrapper.parse((String)json);
	}
}
