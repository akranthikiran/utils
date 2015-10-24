package com.fw.persistence.listeners;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fw.persistence.repository.RepositoryFactory;

/**
 * Manages registered listener methods and their invocations
 * @author akiran
 */
public class EntityListenerManager
{
	private static Logger logger = LogManager.getLogger(EntityListenerManager.class);
	
	/**
	 * Holds handlers grouped by entity-type and event-type
	 */
	private Map<String, List<EntityListener>> typeToListeners = new HashMap<>();
	
	/**
	 * Add listener details with specified key
	 * @param key
	 * @param details
	 */
	private void addListenerDetails(String key, EntityListener details)
	{
		List<EntityListener> detailsLst = typeToListeners.get(key);
		
		//for specified key if list is not already existing
		if(detailsLst == null)
		{
			detailsLst = new ArrayList<>();
			typeToListeners.put(key, detailsLst);
		}
		
		detailsLst.add(details);
	}
	
	/**
	 * Registers specified listener container object
	 * @param listenerContainerObject
	 */
	public void registerListener(Object listenerContainerObject)
	{
		Method methods[] = listenerContainerObject.getClass().getMethods();
		Class<?> argTypes[] = null;
		
		EntityEventHandler entityEventHandler = null;
		Class<?> entityTypes[] = null;
		
		//loop through accessible methods
		for(Method method: methods)
		{
			entityEventHandler = method.getAnnotation(EntityEventHandler.class);
			
			//if method is not marked as event handler ignore
			if(entityEventHandler == null)
			{
				continue;
			}
			
			argTypes = method.getParameterTypes();
			
			//if arg types is more than 1 parameter
			if(argTypes.length > 1)
			{
				logger.debug("Ignoring method '{}' as it is having more than one argument", method.getName());
				continue;
			}
			
			//if the argument is available but is not of even type
			if(argTypes.length == 1 && !EntityEvent.class.equals(argTypes[0]))
			{
				logger.debug("Ignoring method '{}' as it is having non-event type argument", method.getName());
			}
			
			entityTypes = entityEventHandler.entityTypes();
			
			//if no entity types are specified
			if(entityTypes.length == 0)
			{
				logger.debug("Registering '{}.{}()' to handle {} event for all types of entities", 
							listenerContainerObject.getClass().getName(), method.getName(), entityEventHandler.eventType());
				addListenerDetails(entityEventHandler.eventType().name(), new EntityListener(listenerContainerObject, method, (argTypes.length == 1) ));
				continue;
			}
			
			//if entity types are specified over annotation, register for this entity types
			for(Class<?> entityType: entityTypes)
			{
				logger.debug("Registering '{}.{}()' to handle {} event for entity type - {}", 
						listenerContainerObject.getClass().getName(), method.getName(), entityEventHandler.eventType(), entityType.getName());
				addListenerDetails(entityType.getName() + "@" + entityEventHandler.eventType(), 
						new EntityListener(listenerContainerObject, method, (argTypes.length == 1)) );
			}
		}
	}
	
	/**
	 * Invokes handlers for specified entity-type, entity and event type
	 * @param entityType
	 * @param entity
	 * @param eventType
	 */
	public void handleEventType(Class<?> entityType, RepositoryFactory factory, 
			Object key, Object entity, EntityEventType eventType)
	{
		List<EntityListener> listenerLst = typeToListeners.get(entityType.getName() + "@" + eventType);
		List<EntityListener> genericListenerLst = typeToListeners.get(eventType.toString());
		
		//if no handlers are present
		if(listenerLst == null && genericListenerLst == null)
		{
			return;
		}
		
		EntityEvent event = new EntityEvent(entity, eventType, factory);
		
		//if available, invoke entity specific handlers
		if(listenerLst != null)
		{
			for(EntityListener listener : listenerLst)
			{
				try
				{
					listener.invoke(event);
				}catch(Exception ex)
				{
					logger.error("An error occurred while invoking event handler - " + listener, ex);
				}
			}
		}
		
		//if available, invoke generic handlers
		if(genericListenerLst != null)
		{
			for(EntityListener listener : genericListenerLst)
			{
				try
				{
					listener.invoke(event);
				}catch(Exception ex)
				{
					logger.error("An error occurred while invoking generic event handler - " + listener, ex);
				}
			}
		}
	}
	
	/**
	 * Indicates whether listener is present for specified entity type and event type 
	 * @param entityType
	 * @param eventType
	 * @return
	 */
	public boolean isListenerPresent(Class<?> entityType, EntityEventType eventType)
	{
		List<EntityListener> listenerLst = typeToListeners.get(entityType.getName() + "@" + eventType);
		List<EntityListener> genericListenerLst = typeToListeners.get(eventType.toString());
		
		//if no handlers are present
		if(listenerLst == null && genericListenerLst == null)
		{
			return false;
		}
		
		return true;
	}
}
