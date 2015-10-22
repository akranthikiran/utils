package com.fw.persistence.repository.executors;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fw.ccg.util.StringUtil;
import com.fw.persistence.EntityDetails;
import com.fw.persistence.FieldDetails;
import com.fw.persistence.ICrudRepository;
import com.fw.persistence.IDataStore;
import com.fw.persistence.Operator;
import com.fw.persistence.conversion.ConversionService;
import com.fw.persistence.repository.InvalidRepositoryException;
import com.fw.persistence.repository.PersistenceExecutionContext;
import com.fw.persistence.repository.annotations.Condition;
import com.fw.persistence.repository.annotations.ConditionBean;

public abstract class QueryExecutor
{
	private static Logger logger = LogManager.getLogger(QueryExecutor.class);

	protected EntityDetails entityDetails;
	protected Class<?> repositoryType;
	
	protected PersistenceExecutionContext persistenceExecutionContext;
	
	public void setPersistenceExecutionContext(PersistenceExecutionContext persistenceExecutionContext)
	{
		this.persistenceExecutionContext = persistenceExecutionContext;
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
	
	private boolean fetchConditionsFromObject(String methodName, Class<?> queryobjType,  
			int index, ConditionQueryBuilder conditionQueryBuilder, String methodDesc)
	{
		Field fields[] = queryobjType.getDeclaredFields();
		Condition condition = null;
		FieldDetails fieldDetails = null;
		String name = null;
		boolean found = false;
		
		//loop through query object type fields 
		for(Field field : fields)
		{
			condition = field.getAnnotation(Condition.class);
			
			//if field is not marked as condition
			if(condition == null)
			{
				continue;
			}
			
			//fetch entity field name
			name = condition.value();
			
			//if name is not specified in condition
			if(name.trim().length() == 0)
			{
				//use field name
				name = field.getName();
			}
			
			//fetch corresponding field details
			fieldDetails = this.entityDetails.getFieldDetailsByField(name);
			
			if(fieldDetails == null)
			{
				throw new InvalidRepositoryException(String.format(
						"Invalid @Condition field '%s'[%s] is specified for finder method '%s' of repository: %s", 
							name, queryobjType.getName(), methodName, repositoryType.getName()));
			}

			conditionQueryBuilder.addCondition(condition.op(), index, field.getName(), name, methodDesc);
			found = true;
		}
		
		return found;
	}
	
	protected boolean fetchConditonsByAnnotations(Method method, 
			boolean expectAllConditions, ConditionQueryBuilder conditionQueryBuilder, String methodDesc)
	{
		logger.trace("Started method: fetchConditonsByAnnotations");
		
		Class<?> paramTypes[] = method.getParameterTypes();
		Annotation paramAnnotations[][] = method.getParameterAnnotations();
		
		if(paramAnnotations == null || paramAnnotations.length == 0)
		{
			return false;
		}

		ConditionBean conditionBean = null;
		Condition condition = null;
		boolean found = false;
		String fieldName = null;
		
		//fetch conditions for each argument
		for(int i = 0; i < paramTypes.length; i++)
		{
			condition = getAnnotation(paramAnnotations[i], Condition.class);
			
			//if condition is not found on attr
			if(condition == null)
			{
				//check for query object annotation
				conditionBean = getAnnotation(paramAnnotations[i], ConditionBean.class);
				
				//if query object is found find nested conditions
				if(conditionBean != null)
				{
					if( fetchConditionsFromObject(method.getName(), paramTypes[i], i, conditionQueryBuilder, methodDesc) )
					{
						found = true;
					}
					
					continue;
				}
				
				if(!expectAllConditions)
				{
					continue;
				}
				
				if(found)
				{
					throw new InvalidRepositoryException("@Condition/@ConditionBean is not defined for all parameters of method '" 
								+ method.getName() + "' of repository: " + repositoryType.getName());
				}
				
				return false;
			}
			
			fieldName = condition.value();
			
			if(fieldName.trim().length() == 0)
			{
				throw new InvalidRepositoryException("No name is specified in @Condition parameter of method '" 
						+ method.getName() + "' of repository: " + repositoryType.getName());
			}
			
			conditionQueryBuilder.addCondition(condition.op(), i, null, fieldName.trim(), methodDesc);
			found = true;
		}

		return found;
	}
	
	protected boolean fetchConditionsByName(Method method, ConditionQueryBuilder conditionQueryBuilder, String methodDesc)
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
			throw new InvalidRepositoryException("Unable to find sufficient fields names from " + methodDesc);
		}
		
		int index = 0;
		
		for(String field: fieldNames)
		{
			field = StringUtil.toStartLower(field);
			
			conditionQueryBuilder.addCondition(Operator.EQ, index, null, field, methodDesc);
			index++;
		}
		
		return true;
	}
	
}
