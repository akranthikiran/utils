package com.fw.persistence.repository.executors;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fw.ccg.util.StringUtil;
import com.fw.persistence.EntityDetails;
import com.fw.persistence.FieldDetails;
import com.fw.persistence.ICrudRepository;
import com.fw.persistence.IDataStore;
import com.fw.persistence.IPersistenceContext;
import com.fw.persistence.Operator;
import com.fw.persistence.conversion.ConversionService;
import com.fw.persistence.query.ConditionParam;
import com.fw.persistence.query.IConditionalQuery;
import com.fw.persistence.repository.InvalidRepositoryException;
import com.fw.persistence.repository.PersistenceExecutionContext;
import com.fw.persistence.repository.annotations.Condition;

public abstract class QueryExecutor
{
	private static Logger logger = LogManager.getLogger(QueryExecutor.class);

	protected EntityDetails entityDetails;
	protected Class<?> repositoryType;
	
	protected PersistenceExecutionContext persistenceExecutionContext;
	protected IPersistenceContext context;
	
	public void setPersistenceExecutionContext(PersistenceExecutionContext persistenceExecutionContext)
	{
		this.persistenceExecutionContext = persistenceExecutionContext;
		this.context = persistenceExecutionContext.getPersistenceContext();
	}
	
	protected ICrudRepository<?> getCrudRepository(Class<?> entityType)
	{
		return persistenceExecutionContext.getRepositoryFactory().getRepositoryForEntity(entityType);
	}
	
	public abstract Object execute(IDataStore dataStore, ConversionService conversionService, Object... params);
	
	@SuppressWarnings("unchecked")
	protected <A extends Annotation> A getAnnotation(Annotation annotations[], Class<A> type)
	{
		logger.trace("Started method: getAnnotation");
		
		if(annotations == null || annotations.length == 0)
		{
			return null;
		}
		
		for(Annotation a: annotations)
		{
			if(a.annotationType().equals(type))
			{
				return (A)a;
			}
		}
		
		return null;
	}
	
	protected boolean fetchConditonsByAnnotations(Method method, IConditionalQuery query, boolean expectAllConditions)
	{
		logger.trace("Started method: fetchConditonsByAnnotations");
		
		Class<?> paramTypes[] = method.getParameterTypes();
		Annotation paramAnnotations[][] = method.getParameterAnnotations();
		
		if(paramAnnotations == null || paramAnnotations.length == 0)
		{
			return false;
		}

		Condition condition = null;
		boolean found = false;
		FieldDetails fieldDetails = null;
		
		//fetch conditions for each argument
		for(int i = 0; i < paramTypes.length; i++)
		{
			condition = getAnnotation(paramAnnotations[i], Condition.class);
			
			if(condition == null)
			{
				if(!expectAllConditions)
				{
					continue;
				}
				
				if(found)
				{
					throw new InvalidRepositoryException("@Condition are not defined for all parameters of finder method '" 
								+ method.getName() + "' of repository: " + repositoryType.getName());
				}
				
				return false;
			}
			
			fieldDetails = this.entityDetails.getFieldDetailsByField(condition.value());
			
			if(fieldDetails == null)
			{
				throw new InvalidRepositoryException("Invalid @Condition field '" + condition.value() + "' is specifie for finder method '" 
						+ method.getName() + "' of repository: " + repositoryType.getName());
			}
			
			query.addCondition(new ConditionParam(fieldDetails.getColumn(), condition.op(), null, i));
			found = true;
		}

		return found;
	}
	
	protected boolean fetchConditionsByName(Method method, IConditionalQuery query, String type)
	{
		logger.trace("Started method: fetchConditionsByName");
		
		String name = method.getName();
		int idx = name.indexOf("By");
		
		if(idx < 0 || (idx + 2) >= name.length())
		{
			return false;
		}
		
		name = name.substring(idx + 2);
		String fieldNames[] = name.split("And");
		
		if(method.getParameterTypes().length != fieldNames.length)
		{
			throw new InvalidRepositoryException("Unable to find sufficient fields names from " + type + " method name '" 
						+ method.getName() + "' of repository " + repositoryType.getName());
		}
		
		FieldDetails fieldDetails = null;
		int index = 0;
		
		for(String field: fieldNames)
		{
			field = StringUtil.toStartLower(field);
			fieldDetails = entityDetails.getFieldDetailsByField(field);
			
			if(fieldDetails == null)
			{
				throw new InvalidRepositoryException("Invalid field name '" + field + "' extracted from " + type + " method name '" 
							+ method.getName() + "' of repository " + repositoryType.getName());
			}
			
			query.addCondition(new ConditionParam(fieldDetails.getColumn(), Operator.EQ, null, index));
			index++;
		}
		
		return true;
	}
	
}
