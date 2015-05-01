package com.fw.test.persitence;

import com.fw.persistence.IPersistenceContext;

public class TestPersistenceContext implements IPersistenceContext
{
	public static final String USER = "TEST_USER";
	
	@Override
	public String getCurrentUser()
	{
		return USER;
	}

	@Override
	public boolean isAuditEnabled()
	{
		return true;
	}

}
