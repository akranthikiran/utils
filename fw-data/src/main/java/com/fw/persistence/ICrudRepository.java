package com.fw.persistence;

import java.util.Date;

public interface ICrudRepository<E>
{
	public EntityDetails getEntityDetails();

	public ITransaction newTransaction();
	
	public ITransaction currentTransaction();
	
	//audit related functions
	public void clearAuditEntries(Date tillDate);
	
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
