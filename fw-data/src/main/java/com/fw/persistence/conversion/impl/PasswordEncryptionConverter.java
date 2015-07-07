package com.fw.persistence.conversion.impl;

import com.fw.persistence.annotations.DataType;
import com.fw.persistence.conversion.IPersistenceConverter;
import com.fw.persistence.utils.PasswordEncryptor;

public class PasswordEncryptionConverter implements IPersistenceConverter
{
	@Override
	public Object convertToJavaType(Object dbObject, DataType dbType, Class<?> javaType)
	{
		//Retain encrypted value from db. As encryption is one way encryption
		return dbObject;
	}

	@Override
	public Object convertToDBType(Object javaObject, DataType dbType)
	{
		return PasswordEncryptor.encryptPassword((String)javaObject);
	}

}
