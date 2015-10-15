package com.fw.persistence.repository.executors;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fw.persistence.EntityDetails;
import com.fw.persistence.IDataStore;
import com.fw.persistence.conversion.ConversionService;
import com.fw.persistence.query.ConditionParam;
import com.fw.persistence.query.CountQuery;
import com.fw.persistence.repository.InvalidRepositoryException;
import com.fw.persistence.repository.annotations.CountFunction;
import com.fw.utils.ConvertUtils;

/**
 * Executor for count queries. Count query methods can have following return types - Boolean, int or long
 * @author akiran
 */
@QueryExecutorPattern(annotatedWith = CountFunction.class)
public class CountQueryExecutor extends QueryExecutor
{
	private static Logger logger = LogManager.getLogger(CountQueryExecutor.class);
	
	/**
	 * Support return types by this query executor
	 */
	private static Set<Class<?>> SUPPORTED_RETURN_TYPES = new HashSet<>(Arrays.asList(
			Boolean.class, boolean.class,
			Long.class, long.class,
			Integer.class, int.class
	));
	
	private CountQuery query;
	private ReentrantLock queryLock = new ReentrantLock();
	private Class<?> returnType;
	
	public CountQueryExecutor(Class<?> repositoryType, Method method, EntityDetails entityDetails)
	{
		super.repositoryType = repositoryType;
		this.query = new CountQuery(entityDetails);
		super.entityDetails = entityDetails;
		
		fetchConditonsByAnnotations(method, query, true);
		
		this.returnType = method.getReturnType();
		
		if(!SUPPORTED_RETURN_TYPES.contains(returnType))
		{
			throw new InvalidRepositoryException("Invalid return type encountered for count method '" 
					+ method.getName() + "' of repository - " + repositoryType.getName() + " (expected return type - boolean, long or int)");
		}
	}
	
	/* (non-Javadoc)
	 * @see com.fw.persistence.repository.executors.QueryExecutor#execute(com.fw.persistence.IDataStore, com.fw.persistence.conversion.ConversionService, java.lang.Object[])
	 */
	@Override
	public Object execute(IDataStore dataStore, ConversionService conversionService, Object... params)
	{
		logger.trace("Started method: execute");
		
		queryLock.lock();
		
		try
		{
			Object value = null;
			
			//set condition values on query
			for(ConditionParam condition: query.getConditions())
			{
				value = conversionService.convertToDBType(params[condition.getIndex()], null);
				condition.setValue(value);
			}
			
			//execute the query and fetch result count
			long count = dataStore.getCount(query, entityDetails);
			
			//if return type is boolean
			if(boolean.class.equals(this.returnType) || Boolean.class.equals(this.returnType))
			{
				return (count > 0);
			}
			
			//convert the count to required return type
			return ConvertUtils.convert(count, returnType);
			
		}finally
		{
			queryLock.unlock();
		}
	}
	
	
}
