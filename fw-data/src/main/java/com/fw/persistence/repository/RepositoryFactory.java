package com.fw.persistence.repository;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Table;

import com.fw.persistence.EntityDetails;
import com.fw.persistence.EntityDetailsFactory;
import com.fw.persistence.ICrudRepository;
import com.fw.persistence.IDataStore;
import com.fw.persistence.InvalidMappingException;

public class RepositoryFactory
{
	private IDataStore dataStore;
	
	private Map<Class<?>, ICrudRepository<?>> typeToRepo = new HashMap<>();
	private Map<Class<?>, ICrudRepository<?>> entityTypeToRepo = new HashMap<>();

	private boolean createTables;
	
	private ExecutorFactory executorFactory;
	
	public IDataStore getDataStore()
	{
		return dataStore;
	}

	public void setDataStore(IDataStore dataStore)
	{
		this.dataStore = dataStore;
	}
	
	public boolean isCreateTables()
	{
		return createTables;
	}

	public void setCreateTables(boolean createTables)
	{
		this.createTables = createTables;
	}
	
	public ExecutorFactory getExecutorFactory()
	{
		if(executorFactory == null)
		{
			executorFactory = new ExecutorFactory(new PersistenceExecutionContext(this));
		}
		
		return executorFactory;
	}

	public void setExecutorFactory(ExecutorFactory executorFactory)
	{
		this.executorFactory = executorFactory;
	}
	
	private Type fetchRepositoryType(Class<?> repoType)
	{
		if(!ICrudRepository.class.isAssignableFrom(repoType))
		{
			return null;
		}
		
		Type superTypes[] = repoType.getGenericInterfaces();
		Class<?> superInterfaces[] = repoType.getInterfaces();
		
		for(int i = 0; i < superInterfaces.length; i++)
		{
			if(ICrudRepository.class.equals(superInterfaces[i]))
			{
				return superTypes[i];
			}
		}
		
		return null;
	}
	
	@SuppressWarnings({"rawtypes"})
	private EntityDetails fetchEntityDetails(Class<?> repositoryType)
	{
		Type crudRepoType = fetchRepositoryType(repositoryType);
		
		if(crudRepoType == null)
		{
			throw new IllegalStateException("Failed to find super ICrudRepository interface for type: " + repositoryType.getName());
		}
		
		Type crudRepoParams[] = ((ParameterizedType)crudRepoType).getActualTypeArguments();
		Class<?> entityType = (Class<?>)crudRepoParams[0];
		
		Table table = entityType.getAnnotation(Table.class);
		
		if(table == null)
		{
			throw new InvalidMappingException("No @Table annotation found on entity type: " + entityType.getName());
		}
		
		return EntityDetailsFactory.getEntityDetails((Class)entityType, dataStore, createTables);
	}
	
	@SuppressWarnings("unchecked")
	public synchronized <R extends ICrudRepository<?>> R getRepository(Class<R> repositoryType)
	{
		R repo = (R)typeToRepo.get(repositoryType);
		
		if(repo != null)
		{
			return repo;
		}
		
		EntityDetails entityDetails = fetchEntityDetails(repositoryType);
		RepositoryProxy proxyImpl = new RepositoryProxy(dataStore, repositoryType, entityDetails, getExecutorFactory());
		
		repo = (R)Proxy.newProxyInstance(RepositoryFactory.class.getClassLoader(), new Class<?>[] {repositoryType}, proxyImpl);
		typeToRepo.put(repositoryType, repo);
		entityTypeToRepo.put(entityDetails.getEntityType(), repo);
		
		return repo;
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	private synchronized ICrudRepository<?> getGenericRepository(Class<?> entityType)
	{
		ICrudRepository<?> repo = entityTypeToRepo.get(entityType);
		
		if(repo != null)
		{
			return repo;
		}

		EntityDetails entityDetails = EntityDetailsFactory.getEntityDetails((Class)entityType, dataStore, createTables);
		RepositoryProxy proxyImpl = new RepositoryProxy(dataStore, (Class)ICrudRepository.class, entityDetails, getExecutorFactory());
		
		repo = (ICrudRepository)Proxy.newProxyInstance(RepositoryFactory.class.getClassLoader(), new Class<?>[] {ICrudRepository.class}, proxyImpl);
		entityTypeToRepo.put(entityType, repo);
		
		return repo;
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	public <T> ICrudRepository<T> getRepositoryForEntity(Class<T> entityType)
	{
		ICrudRepository<?> repo = entityTypeToRepo.get(entityType);
		
		if(repo != null)
		{
			return (ICrudRepository)repo;
		}
		
		return (ICrudRepository)getGenericRepository(entityType);
	}
}
