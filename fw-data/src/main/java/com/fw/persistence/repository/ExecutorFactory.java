package com.fw.persistence.repository;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fw.persistence.EntityDetails;
import com.fw.persistence.repository.executors.DeleteQueryExecutor;
import com.fw.persistence.repository.executors.ExistenceQueryExecutor;
import com.fw.persistence.repository.executors.FinderQueryExecutor;
import com.fw.persistence.repository.executors.QueryExecutor;
import com.fw.persistence.repository.executors.QueryExecutorPattern;
import com.fw.persistence.repository.executors.SaveOrUpdateQueryExecutor;
import com.fw.persistence.repository.executors.SaveQueryExecutor;
import com.fw.persistence.repository.executors.UpdateQueryExecutor;

public class ExecutorFactory
{
	private static class ExecutorDetails
	{
		private Constructor<?> constructor;
		private String prefixes[];
		private String excludePrefixes[];
		
		public ExecutorDetails(Constructor<?> constructor, String prefixes[], String excludePrefixes[])
		{
			this.constructor = constructor;
			this.prefixes = prefixes;
			this.excludePrefixes = excludePrefixes;
		}
		
		public QueryExecutor newQueryExecutor(PersistenceExecutionContext persistenceExecutionContext, Class<?> repositoryType, Method method, EntityDetails entityDetails)
		{
			try
			{
				QueryExecutor execuctor = (QueryExecutor)constructor.newInstance(repositoryType, method, entityDetails);
				execuctor.setPersistenceExecutionContext(persistenceExecutionContext);
				
				return execuctor;
			}catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex)
			{
				throw new IllegalStateException("An error occurred while creating instance of Query-executor", ex);
			}
		}
		
		public boolean isMatchingMethodName(String methodName)
		{
			if(this.prefixes == null)
			{
				return false;
			}
			
			if(this.excludePrefixes != null)
			{
				for(String exPrefix: this.excludePrefixes)
				{
					if(methodName.startsWith(exPrefix))
					{
						return false;
					}
				}
			}
			
			for(String prefix: this.prefixes)
			{
				if(methodName.startsWith(prefix))
				{
					return true;
				}
			}
			
			return false;
		}
	}
	
	private List<ExecutorDetails> executorDetailsLst = new ArrayList<>();
	private Map<Class<?>, ExecutorDetails> annotationToDetails = new HashMap<>();
	
	private PersistenceExecutionContext persistenceExecutionContext;
	
	public ExecutorFactory(PersistenceExecutionContext persistenceExecutionContext)
	{
		registerDefaultExecutors();
		
		this.persistenceExecutionContext = persistenceExecutionContext;
	}
	
	protected void registerDefaultExecutors()
	{
		registerExecutor(ExistenceQueryExecutor.class);
		registerExecutor(FinderQueryExecutor.class);
		registerExecutor(SaveOrUpdateQueryExecutor.class);
		registerExecutor(SaveQueryExecutor.class);
		registerExecutor(DeleteQueryExecutor.class);
		registerExecutor(UpdateQueryExecutor.class);
	}

	public void registerExecutor(Class<? extends QueryExecutor> executorType)
	{
		QueryExecutorPattern executorPattern = executorType.getAnnotation(QueryExecutorPattern.class);
		
		if(executorPattern == null)
		{
			throw new IllegalArgumentException("Specified executor-type is not annotated with @QueryExecutorPattern - " + executorType.getName());
		}
		
		String prefixes[] = executorPattern.prefixes();
		prefixes = prefixes.length == 0 ? null : prefixes;

		String excludePrefixes[] = executorPattern.excludePrefixes();
		excludePrefixes = excludePrefixes.length == 0 ? null : excludePrefixes;

		Class<?> annotationType = executorPattern.annotatedWith();
		
		if(prefixes == null && Annotation.class.equals(annotationType))
		{
			throw new IllegalArgumentException("Neither prefix not annotated-with is specified in @QueryExecutorPattern annotation of - " + executorType.getName());
		}
		
		Constructor<?> constructor = null;
		
		try
		{
			constructor = executorType.getConstructor(Class.class, Method.class, EntityDetails.class);
		}catch(NoSuchMethodException ex)
		{
			throw new IllegalArgumentException("No constructor of type <init>(Class<?> repositoryType, Method method, EntityDetails entityDetails) "
					+ "is defined in specified executor type: " + executorType.getName());
		}
		
		ExecutorDetails executorDetails = new ExecutorDetails(constructor, prefixes, excludePrefixes); 
		this.executorDetailsLst.add(executorDetails);
		
		annotationToDetails.put(annotationType, executorDetails);
	}
	
	/**
	 * For given "repositoryType" and for given repositoryType's method "method" fetches QueryExecutor 
	 * @param repositoryType
	 * @param method
	 * @param entityDetails
	 * @return
	 */
	public QueryExecutor getQueryExecutor(Class<?> repositoryType, Method method, EntityDetails entityDetails)
	{
		Annotation annotaions[] = method.getAnnotations();
		ExecutorDetails details = null;
		
		for(Annotation annotaion: annotaions)
		{
			details = annotationToDetails.get(annotaion.annotationType());
			
			if(details != null)
			{
				return details.newQueryExecutor(persistenceExecutionContext, repositoryType, method, entityDetails);
			}
		}
		
		String methodName = method.getName();
		
		for(ExecutorDetails executorDetails: this.executorDetailsLst)
		{
			if(executorDetails.isMatchingMethodName(methodName))
			{
				return executorDetails.newQueryExecutor(persistenceExecutionContext, repositoryType, method, entityDetails);
			}
		}
		
		return null;
	}
}
