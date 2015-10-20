package com.fw.persistence;

import com.fw.persistence.repository.RepositoryFactory;

/**
 * Generic implementation of CRUD repository for all entities for default functionality
 * @author akiran
 */
public class GenericRepository
{
	private RepositoryFactory repositoryFactory;

	public GenericRepository(RepositoryFactory repositoryFactory)
	{
		this.repositoryFactory = repositoryFactory;
	}

	/**
	 * Saves specified entity
	 * @param entity
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public boolean save(Object entity)
	{
		ICrudRepository<Object> repo = (ICrudRepository)repositoryFactory.getRepositoryForEntity((Class)entity.getClass());
		return repo.save(entity);
	}

	/**
	 * Updates the specified entity
	 * @param entity
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public boolean update(Object entity)
	{
		ICrudRepository<Object> repo = (ICrudRepository)repositoryFactory.getRepository((Class)entity.getClass());
		return repo.update(entity);
	}

	/**
	 * Deletes the entity with specified id
	 * @param entityType
	 * @param key
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public boolean deleteById(Class<?> entityType, Object key)
	{
		ICrudRepository<Object> repo = (ICrudRepository)repositoryFactory.getRepository((Class)entityType);
		return repo.deleteById(key);
	}

	/**
	 * Finds the entity of specified type using specifie key
	 * @param entityType
	 * @param key
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object findById(Class<?> entityType, Object key)
	{
		ICrudRepository<Object> repo = (ICrudRepository)repositoryFactory.getRepository((Class)entityType);
		return repo.findById(key);
	}

	/**
	 * Gets the number of entities from datastore of specified type
	 * @param entityType
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public long getCount(Class<?> entityType)
	{
		ICrudRepository<Object> repo = (ICrudRepository)repositoryFactory.getRepository((Class)entityType);
		return repo.getCount();
	}
	
	
}
