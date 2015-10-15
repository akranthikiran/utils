package com.fw.persistence;

import com.fw.persistence.repository.annotations.CountFunction;

public interface ICrudRepository<E>
{
	public EntityDetails getEntityDetails();

	public ITransaction newTransaction();
	
	public ITransaction currentTransaction();
	
	/**
	 * Saves the entity to underlying store
	 * @param entity
	 */
	public boolean save(E entity);

	public boolean update(E entity);
	
	public boolean saveOrUpdate(E entity);
	
	public boolean deleteById(Object key);
	
	public E findById(Object key);
	
	/**
	 * Fetches the count of number of entities in this repository 
	 * @return
	 */
	@CountFunction
	public long getCount();
}
