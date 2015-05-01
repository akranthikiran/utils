package com.fw.persistence;

import java.util.Date;
import java.util.List;

public interface ICrudRepository<E>
{
	public EntityDetails getEntityDetails();

	public ITransaction newTransaction();
	
	public ITransaction currentTransaction();
	
	//audit related functions
	public void clearAuditEntries(Date tillDate);
	
	public List<Record> fetchAuditRecords(AuditSearchQuery query);
	
	/**
	 * Saves the entity to underlying store
	 * @param entity
	 */
	public boolean save(E entity);

	public boolean update(E entity);
	
	public boolean saveOrUpdate(E entity);
	
	public boolean deleteById(Object key);
	
	public E findById(Object key);
}
