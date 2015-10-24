package com.fw.persistence.listeners;

import com.fw.persistence.repository.RepositoryFactory;

/**
 * Event object representing listener event
 * @author akiran
 */
public class EntityEvent
{
	/**
	 * Entity under action
	 */
	private Object entity;
	
	/**
	 * Type of event
	 */
	private EntityEventType eventType;
	
	/**
	 * Repository factory
	 */
	private RepositoryFactory repositoryFactory;
	
	public EntityEvent(Object entity, EntityEventType eventType, RepositoryFactory factory)
	{
		this.entity = entity;
		this.eventType = eventType;
		this.repositoryFactory = factory;
	}

	/**
	 * @return the {@link #entity entity}
	 */
	public Object getEntity()
	{
		return entity;
	}

	/**
	 * @return the {@link #eventType eventType}
	 */
	public EntityEventType getEventType()
	{
		return eventType;
	}
	
	public RepositoryFactory getRepositoryFactory()
	{
		return repositoryFactory;
	}
}
