package com.fw.persistence.repository;

import com.fw.persistence.IPersistenceContext;

/**
 * Persistence context (used by default) which disables audit and sets the current user as null
 * @author akiran
 *
 */
public class NoAuditPersistenceContext implements IPersistenceContext
{
	@Override
	public String getCurrentUser()
	{
		return null;
	}

	@Override
	public boolean isAuditEnabled()
	{
		return false;
	}

}
