package com.fw.persistence.monitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fw.persistence.EntityDetails;
import com.fw.persistence.EntityDetailsFactory;

/**
 * Keeps track of changes in entity details and support change event listeners
 * @author akiran
 */
public class EntityDetailsMonitor
{
	private static Logger logger = LogManager.getLogger(EntityDetailsMonitor.class);
	private Map<Class<?>, List<IEntityCreateTableListener>> createTableListeners = new HashMap<>();

	private Map<Class<?>, EntityDetails> entityWithTables = new HashMap<>();

	/**
	 * Event method expected to be invoked by {@link EntityDetailsFactory} after tables are created.
	 * @param entityDetails
	 */
	public synchronized void tablesCreatedForEntity(EntityDetails entityDetails)
	{
		Class<?> entityType = entityDetails.getEntityType();
		
		logger.debug("Got event of table creation for entity type: {}", entityType.getName());
		entityWithTables.put(entityType, entityDetails);
		
		List<IEntityCreateTableListener> listeners = createTableListeners.get(entityType);
		
		//if no listeners are registered for current entity
		if(listeners == null)
		{
			return;
		}
		
		//invoke the registered listeners
		for(IEntityCreateTableListener listener : listeners)
		{
			listener.tableCreated(entityDetails);
		}
		
		//clean the listeners as the event is notified
		createTableListeners.remove(entityType);
	}
	
	/**
	 * Returns true, if tables for specified entity is created
	 * @param entityType
	 * @return
	 */
	public synchronized boolean isTablesCreated(Class<?>... entityTypes)
	{
		//check if all specified entity tables are created
		for(Class<?> type : entityTypes)
		{
			//even if one is found as not created
			if(!entityWithTables.containsKey(type))
			{
				return false;
			}
		}
		
		//if all are found to be created
		return true;
	}
	
	
	/**
	 * Adds create table listener for specified entity types. When each of this entity table is created, this listener
	 * will get called
	 * @param listener
	 * @param entityTypes
	 */
	public synchronized void addCreateTableListener(IEntityCreateTableListener listener, Class<?>... entityTypes)
	{
		List<IEntityCreateTableListener> listeners = null;
		
		//loop through specified types
		for(Class<?> entityType : entityTypes)
		{
			//check if table already created
			if(entityWithTables.containsKey(entityType))
			{
				//if created, invoke listener directly and dont add to listener list
				listener.tableCreated(entityWithTables.get(entityType));
				continue;
			}
			
			//get listeners list for specified type
			listeners = createTableListeners.get(entityType);
			
			//if no listeners are present for current type
			if(listeners == null)
			{
				listeners = new ArrayList<>();
				createTableListeners.put(entityType, listeners);
			}
			
			//add current listener
			listeners.add(listener);
		}
	}
}
