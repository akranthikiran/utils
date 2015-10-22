package com.fw.persistence.repository.executors.proxy;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

import org.apache.commons.beanutils.PropertyUtils;

import com.fw.persistence.EntityDetails;
import com.fw.persistence.ICrudRepository;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.InvocationHandler;

/**
 * Represents proxy for entity class used for lazy loading
 * @author akiran
 */
public class ProxyEntityCreator
{
	/**
	 * The actual entity which would be loaded lazily on need basis 
	 */
	private Object actualEntity;
	
	/**
	 * CRUD repository for the entity
	 */
	private ICrudRepository<?> repository;
	
	/**
	 * Id of the entity
	 */
	private Object entityId;
	
	/**
	 * Id getter method of entity
	 */
	private Method idGetter;
	
	/**
	 * Proxy object that will be exposed to outside world
	 */
	private Object proxyEntity;

	/**
	 * Creates a proxy for specified entity type
	 * @param entityDetails
	 * @param repository
	 * @param entityType
	 */
	public ProxyEntityCreator(EntityDetails entityDetails, ICrudRepository<?> repository, Object entityId)
	{
		this.repository = repository;
		this.entityId = entityId;
		
		Class<?> entityType = entityDetails.getEntityType();
		
		//create ccg lib handler which will handle method calls on proxy
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(entityType);
		
		enhancer.setCallback(new InvocationHandler()
		{
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
			{
				return ProxyEntityCreator.this.invoke(proxy, method, args);
			}
		});
		
		//fetch the id getter method
		try
		{
			String idFieldName = entityDetails.getIdField().getName();
			PropertyDescriptor propertyDesc = PropertyUtils.getPropertyDescriptor(entityType.newInstance(), idFieldName);
			
			this.idGetter = propertyDesc != null ? propertyDesc.getReadMethod() : null;
		}catch(Exception ex)
		{
			throw new IllegalStateException("An error occurred while fetch id getter for entity type - " + entityType.getName(), ex);
		}
		
		//if unable to find id getter throw error
		if(this.idGetter == null)
		{
			throw new IllegalStateException("Failed to fetch id getter for entity type - " + entityType.getName());
		}
		
		this.proxyEntity = enhancer.create();
	}
	
	/**
	 * @return Proxy entity
	 */
	public Object getProxyEntity()
	{
		return proxyEntity;
	}
	
	/**
	 * Proxy method invocation handler method
	 * @param proxy
	 * @param method
	 * @param args
	 * @return
	 * @throws Throwable
	 */
	private Object invoke(Object proxy, Method method, Object[] args) throws Throwable
	{
		//if the method being invoked is same as id getter simply return the value
		if(idGetter.equals(method))
		{
			return entityId;
		}

		synchronized(this)
		{
			if(actualEntity != null)
			{
				return method.invoke(actualEntity, args);
			}
			
			actualEntity = repository.findById(entityId);
			return method.invoke(actualEntity, args);
		}
	}
	
}
