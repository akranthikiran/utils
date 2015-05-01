package com.fw.persistence;

public interface IPersistenceContext
{
	public String getCurrentUser();
	
	public boolean isAuditEnabled();
}
