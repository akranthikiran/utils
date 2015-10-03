package com.fw.persistence.repository;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fw.persistence.EntityDetails;
import com.fw.persistence.ICrudRepository;
import com.fw.persistence.IDataStore;
import com.fw.persistence.TransactionException;
import com.fw.persistence.repository.executors.QueryExecutor;

class RepositoryProxy implements InvocationHandler
{
	private static Logger logger = LogManager.getLogger(RepositoryProxy.class);
	
	private IDataStore dataStore;
	private Map<String, QueryExecutor> methodToExecutor = new HashMap<>();
	private EntityDetails entityDetails;

	private Map<String, Function<Object[], Object>> defaultedMethods = new HashMap<>();
	
	public RepositoryProxy(IDataStore dataStore, Class<? extends ICrudRepository<?>> repositoryType, EntityDetails entityDetails, ExecutorFactory executorFactory)
	{
		defaultedMethods.put("getEntityDetails", this::getEntityDetails);
		defaultedMethods.put("newTransaction", this::newTransaction);
		defaultedMethods.put("currentTransaction", this::currentTransaction);

		this.dataStore = dataStore;
		this.entityDetails = entityDetails;
		
		Method methods[] = repositoryType.getMethods();
		String methodName = null;
		QueryExecutor queryExecutor = null;
		
		for(Method method: methods)
		{
			methodName = method.getName();
			
			if(defaultedMethods.containsKey(methodName))
			{
				continue;
			}
			
			if(methodToExecutor.containsKey(method.getName()))
			{
				throw new InvalidRepositoryException("Duplicate method '" + method.getName() + "' encouneted in repository: " + repositoryType.getName());
			}

			queryExecutor = executorFactory.getQueryExecutor(repositoryType, method, entityDetails);
			
			if(queryExecutor != null)
			{
				methodToExecutor.put(methodName, queryExecutor);
				continue;
			}
			
			
			throw new InvalidRepositoryException("Invalid CRUD method '" + methodName + "' is specified for entity - " + entityDetails.getEntityType().getName());
		}
		
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
	{
		String methodName = method.getName();
				
		if(defaultedMethods.containsKey(methodName))
		{
			logger.debug("Executing default-method '" + method.getName() + "' with arguments: " + Arrays.toString(args));
			
			return defaultedMethods.get(methodName).apply(args);
		}
		
		logger.debug("Executing method '" + method.getName() + "' with arguments: " + Arrays.toString(args));
		
		try
		{
			QueryExecutor queryExecutor = methodToExecutor.get(method.getName());
			return queryExecutor.execute(dataStore, dataStore.getConversionService(), args);
		}catch(RuntimeException ex)
		{
			logger.error("An error occurred while executing method: " + method.getName(), ex);
			throw ex;
		}
	}

	private Object getEntityDetails(Object args[])
	{
		return entityDetails;
	}
	
	private Object newTransaction(Object args[])
	{
		try
		{
			return dataStore.getTransactionManager().newTransaction();
		}catch(TransactionException e)
		{
			throw new IllegalStateException(e);
		}
	}
	
	private Object currentTransaction(Object args[])
	{
		try
		{
			return dataStore.getTransactionManager().currentTransaction();
		}catch(TransactionException e)
		{
			throw new IllegalStateException(e);
		}
	}
}
