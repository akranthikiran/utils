package com.fw.persistence.repository.executors;

import java.lang.reflect.Method;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fw.persistence.EntityDetails;
import com.fw.persistence.IDataStore;
import com.fw.persistence.conversion.ConversionService;
import com.fw.persistence.query.ConditionParam;
import com.fw.persistence.query.ExistenceQuery;
import com.fw.persistence.repository.InvalidRepositoryException;
import com.fw.persistence.repository.annotations.CountFunction;

@QueryExecutorPattern(annotatedWith = CountFunction.class)
public class ExistenceQueryExecutor extends QueryExecutor
{
	private static Logger logger = LogManager.getLogger(ExistenceQueryExecutor.class);
	
	private ExistenceQuery query;
	private ReentrantLock queryLock = new ReentrantLock();
	private Class<?> returnType;
	
	public ExistenceQueryExecutor(Class<?> repositoryType, Method method, EntityDetails entityDetails)
	{
		super.repositoryType = repositoryType;
		this.query = new ExistenceQuery(entityDetails);
		super.entityDetails = entityDetails;
		
		fetchConditonsByAnnotations(method, query, true);
		
		this.returnType = method.getReturnType();
		
		if(!boolean.class.equals(returnType) && !int.class.equals(returnType))
		{
			throw new InvalidRepositoryException("Invalid return type encountered for existence method '" 
					+ method.getName() + "' of repository - " + repositoryType.getName() + " (expected return type - boolean or int)");
		}
	}
	
	@Override
	public Object execute(IDataStore dataStore, ConversionService conversionService, Object... params)
	{
		logger.trace("Started method: execute");
		
		queryLock.lock();
		
		try
		{
			Object value = null;
			
			for(ConditionParam condition: query.getConditions())
			{
				value = conversionService.convertToDataStore(params[condition.getIndex()], null);
				condition.setValue(value);
			}
			
			int count = dataStore.checkForExistenence(query, entityDetails);
			
			return (boolean.class.equals(this.returnType)) ? (count > 0) : count;
			
		}finally
		{
			queryLock.unlock();
		}
	}
	
	
}
