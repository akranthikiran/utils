package com.fw.persistence.repository.executors.proxy;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import com.fw.persistence.EntityDetails;
import com.fw.persistence.ICrudRepository;
import com.fw.persistence.repository.search.SearchCondition;
import com.fw.persistence.repository.search.SearchQuery;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.InvocationHandler;

/**
 * Represents proxy for entity class used for lazy loading
 * @author akiran
 */
public class ProxyCollectionCreator
{
	/**
	 * The actual entity collection which would be loaded lazily on need basis 
	 */
	private Collection<Object> actualCollection;
	
	/**
	 * Flag indicating if target entities are loaded into collection
	 */
	private boolean collectionLoaded;
	
	/**
	 * CRUD repository for the entity
	 */
	private ICrudRepository<?> repository;
	
	/**
	 * Condition to fetch collection
	 */
	private SearchCondition searchCondition;
	
	/**
	 * Proxy object that will be exposed to outside world
	 */
	private Object proxyCollection;
	
	/**
	 * Creates a proxy for specified entity type
	 * @param entityDetails
	 * @param repository
	 * @param entityType
	 */
	public ProxyCollectionCreator(EntityDetails entityDetails, ICrudRepository<?> repository, SearchCondition condition, Collection<Object> actualCollection)
	{
		if(condition == null)
		{
			throw new NullPointerException("Search-condition can not be null");
		}
		
		this.repository = repository;
		this.searchCondition = condition;
		this.actualCollection = actualCollection;
		
		Class<?> entityType = entityDetails.getEntityType();
		
		//create ccg lib handler which will handle method calls on proxy
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(entityType);
		
		enhancer.setCallback(new InvocationHandler()
		{
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
			{
				return ProxyCollectionCreator.this.invoke(proxy, method, args);
			}
		});
		
		this.proxyCollection = enhancer.create();
	}
	
	/**
	 * @return Proxy entity
	 */
	public Object getProxyEntity()
	{
		return proxyCollection;
	}
	
	/**
	 * Proxy method invocation handler method
	 * @param proxy
	 * @param method
	 * @param args
	 * @return
	 * @throws Throwable
	 */
	private synchronized Object invoke(Object proxy, Method method, Object[] args) throws Throwable
	{
		if(collectionLoaded)
		{
			return method.invoke(actualCollection, args);
		}
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		List<Object> entities = (List)repository.search(new SearchQuery(this.searchCondition));
		
		if(entities != null)
		{
			this.actualCollection.addAll(entities);
		}
		
		collectionLoaded = true;
		return method.invoke(actualCollection, args);
	}
	
}
