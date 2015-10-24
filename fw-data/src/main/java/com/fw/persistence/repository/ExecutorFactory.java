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
import com.fw.persistence.repository.executors.CountQueryExecutor;
import com.fw.persistence.repository.executors.DeleteQueryExecutor;
import com.fw.persistence.repository.executors.FinderQueryExecutor;
import com.fw.persistence.repository.executors.QueryExecutor;
import com.fw.persistence.repository.executors.QueryExecutorPattern;
import com.fw.persistence.repository.executors.SaveQueryExecutor;
import com.fw.persistence.repository.executors.SearchQueryExecutor;
import com.fw.persistence.repository.executors.UpdateQueryExecutor;

/**
 * Factory to get query executor for specified repository method. A query executor for a repository method can be determined using
 * 		1) Annotations on repository method
 * 		2) Method name prefixes
 * 
 * This configuration (whether and what annotation to use or prefixes to be used or excluded) is defined using annotaion {@link QueryExecutorPattern} on the
 * executor class being registered.
 * @author akiran
 */
public class ExecutorFactory
{
	/**
	 * Executor details like which constructor to use for executor creation. And which method prefixes or suffixes to be used to match
	 * repository method.
	 * @author akiran
	 */
	private static class ExecutorDetails
	{
		/**
		 * Constructor for executor creation
		 */
		private Constructor<?> constructor;
		
		/**
		 * Prefixes to be used for repository method matching
		 */
		private String prefixes[];
		
		/**
		 * Prefixes to be excluded during repository method match
		 */
		private String excludePrefixes[];
		
		public ExecutorDetails(Constructor<?> constructor, String prefixes[], String excludePrefixes[])
		{
			this.constructor = constructor;
			this.prefixes = prefixes;
			this.excludePrefixes = excludePrefixes;
		}
		
		/**
		 * Creates new query executor with specified details
		 * @param persistenceExecutionContext
		 * @param repositoryType Repository type
		 * @param method matched repository method
		 * @param entityDetails Target entity details
		 * @return
		 */
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
		
		/**
		 * Checks if the specified method name matches with the current executor prefix conditions
		 * @param methodName
		 * @return
		 */
		public boolean isMatchingMethodName(String methodName)
		{
			//if no prefixes are specified return mismatch
			if(this.prefixes == null)
			{
				return false;
			}
			
			//check if method name has excluded prefixes
			if(this.excludePrefixes != null)
			{
				for(String exPrefix: this.excludePrefixes)
				{
					//if name matches with any of excluded prefix
					if(methodName.startsWith(exPrefix))
					{
						//return mismatch
						return false;
					}
				}
			}
			
			//check if method name matches with any of the specified prefix
			for(String prefix: this.prefixes)
			{
				if(methodName.startsWith(prefix))
				{
					return true;
				}
			}
			
			//if no prefix condition matched, return mismatch
			return false;
		}
	}
	
	/**
	 * Maintains list of supported executor details
	 */
	private List<ExecutorDetails> executorDetailsLst = new ArrayList<>();
	
	/**
	 * List of annotation mappings to be used to match repository methods to 
	 * executor
	 */
	private Map<Class<?>, ExecutorDetails> annotationToDetails = new HashMap<>();
	
	private PersistenceExecutionContext persistenceExecutionContext;
	
	public ExecutorFactory(PersistenceExecutionContext persistenceExecutionContext)
	{
		registerDefaultExecutors();
		
		this.persistenceExecutionContext = persistenceExecutionContext;
	}
	
	/**
	 * Registers default executors
	 */
	protected void registerDefaultExecutors()
	{
		registerExecutor(CountQueryExecutor.class);
		registerExecutor(FinderQueryExecutor.class);
		registerExecutor(SearchQueryExecutor.class);
		registerExecutor(SaveQueryExecutor.class);
		registerExecutor(DeleteQueryExecutor.class);
		registerExecutor(UpdateQueryExecutor.class);
	}

	/**
	 * Registers specified executor type
	 * @param executorType
	 */
	public void registerExecutor(Class<? extends QueryExecutor> executorType)
	{
		QueryExecutorPattern executorPattern = executorType.getAnnotation(QueryExecutorPattern.class);
		
		//if QueryExecutorPattern is not specified on target type
		if(executorPattern == null)
		{
			throw new IllegalArgumentException("Specified executor-type is not annotated with @QueryExecutorPattern - " + executorType.getName());
		}
		
		//fetch prefix match conditions
		String prefixes[] = executorPattern.prefixes();
		prefixes = prefixes.length == 0 ? null : prefixes;

		//fetch prefix conditions that should not match
		String excludePrefixes[] = executorPattern.excludePrefixes();
		excludePrefixes = excludePrefixes.length == 0 ? null : excludePrefixes;

		//check if executor can be matched using annotation
		Class<?> annotationType = executorPattern.annotatedWith();
		
		if(prefixes == null && Annotation.class.equals(annotationType))
		{
			throw new IllegalArgumentException("Neither prefix not annotated-with is specified in @QueryExecutorPattern annotation of - " + executorType.getName());
		}
		
		//ensure required constructor is defined and get it for instance creation
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
		
		//based on annotations, check if match between repository method and exeutor can be done
		for(Annotation annotaion: annotaions)
		{
			details = annotationToDetails.get(annotaion.annotationType());
			
			if(details != null)
			{
				return details.newQueryExecutor(persistenceExecutionContext, repositoryType, method, entityDetails);
			}
		}
		
		//if match can not be done using annotation
		String methodName = method.getName();
		
		//try if match can be done based on method name prefix conditions
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
