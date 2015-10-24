package com.fw.persistence.repository.executors.proxy;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;

import com.fw.persistence.EntityDetails;
import com.fw.persistence.ICrudRepository;
import com.fw.persistence.NoSuchEntityException;
import com.fw.persistence.repository.search.SearchCondition;
import com.fw.persistence.repository.search.SearchQuery;

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
	 * Flag indicating if actual entity is loaded
	 */
	private boolean actualEntityLoaded = false;
	
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
	 * If id is not specified, this condition is used to fetch entity
	 */
	private SearchCondition searchCondition;
	
	/**
	 * Proxy object that will be exposed to outside world
	 */
	private Object proxyEntity;
	
	public ProxyEntityCreator(EntityDetails entityDetails, ICrudRepository<?> repository, Object entityId)
	{
		this(entityDetails, repository, entityId, null);
	}

	public ProxyEntityCreator(EntityDetails entityDetails, ICrudRepository<?> repository, SearchCondition condition)
	{
		this(entityDetails, repository, null, condition);
	}

	/**
	 * Creates a proxy for specified entity type
	 * @param entityDetails
	 * @param repository
	 * @param entityType
	 */
	private ProxyEntityCreator(EntityDetails entityDetails, ICrudRepository<?> repository, Object entityId, SearchCondition condition)
	{
		if(entityId ==  null && condition == null)
		{
			throw new NullPointerException("Both entity id and search-condition can not be null");
		}
		
		this.repository = repository;
		this.entityId = entityId;
		this.searchCondition = condition;
		
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
		
		if(entityId != null)
		{
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
		if(idGetter != null && idGetter.equals(method))
		{
			return entityId;
		}

		synchronized(this)
		{
			if(actualEntityLoaded)
			{
				if(actualEntity == null)
				{
					throw new NoSuchEntityException();
				}
				
				return method.invoke(actualEntity, args);
			}
			
			//if enity id is present use it for fetching entity
			if(entityId != null)
			{
				actualEntity = repository.findById(entityId);
			}
			//if not use search condition to fetch entity
			else
			{
				@SuppressWarnings({ "unchecked", "rawtypes" })
				List<Object> entities = (List)repository.search(new SearchQuery(this.searchCondition));
				this.actualEntity = (entities.size() > 0) ? entities.get(0) : null;
			}
			
			actualEntityLoaded = true;
			
			//if entity is not found with specified criteria
			if(actualEntity == null)
			{
				throw new NoSuchEntityException();
			}
			
			return method.invoke(actualEntity, args);
		}
	}
	
}
