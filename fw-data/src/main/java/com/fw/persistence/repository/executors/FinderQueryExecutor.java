package com.fw.persistence.repository.executors;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fw.ccg.util.CCGUtility;
import com.fw.persistence.EntityDetails;
import com.fw.persistence.FieldDetails;
import com.fw.persistence.ICrudRepository;
import com.fw.persistence.IDataStore;
import com.fw.persistence.PersistenceException;
import com.fw.persistence.Record;
import com.fw.persistence.RecordCountMistmatchException;
import com.fw.persistence.conversion.ConversionService;
import com.fw.persistence.query.ConditionParam;
import com.fw.persistence.query.FinderQuery;
import com.fw.persistence.repository.InvalidRepositoryException;
import com.fw.persistence.repository.annotations.Field;

@QueryExecutorPattern(prefixes = {"find", "fetch"})
public class FinderQueryExecutor extends QueryExecutor
{
	private static Logger logger = LogManager.getLogger(FinderQueryExecutor.class);
	
	private FinderQuery query;
	
	private Class<?> returnType;
	private Map<String, FieldDetails> returnColumnToField = new HashMap<>();
	private Class<?> collectionReturnType = null;
	private boolean isReturnTypeEntity = false;
	
	private ReentrantLock queryLock = new ReentrantLock();
	
	public FinderQueryExecutor(Class<?> repositoryType, Method method, EntityDetails entityDetails)
	{
		super.repositoryType = repositoryType;
		this.query = new FinderQuery(entityDetails);
		super.entityDetails = entityDetails;
		
		fetchReturnDetails(method);
		
		Class<?> paramTypes[] = method.getParameterTypes();

		if(paramTypes.length == 0)
		{
			throw new InvalidRepositoryException("No-parameter finder method '" + method.getName() + "' in repository: " + repositoryType.getName());
		}
		
		if(!fetchConditonsByAnnotations(method, query, true) && !fetchConditionsByName(method, query, "finder"))
		{
			throw new InvalidRepositoryException("Failed to determine parameter conditions for finder method '" 
							+ method.getName() + "' of repository - " + repositoryType.getName());
		}
		
	}

	/*
	FinderQueryExecutor(Class<?> repositoryType, EntityDetails entityDetails, boolean multiple, Collection<ConditionParam> conditions)
	{
		super.repositoryType = repositoryType;
		this.query = new FinderQuery(entityDetails);
		super.entityDetails = entityDetails;
		
		this.collectionReturnType = multiple ? ArrayList.class : null;
		this.returnType = entityDetails.getEntityType();
		
		setFullEntityDetails();
		
		if(conditions != null)
		{
			for(ConditionParam condition : conditions)
			{
				query.addCondition(condition);
			}
		}
	}
	*/
	
	private void setFullEntityDetails()
	{
		logger.trace("Started method: setFullEntityDetails");
		
		for(FieldDetails field: entityDetails.getFieldDetails())
		{
			query.addColumn(field.getColumn());
			returnColumnToField.put(field.getColumn(), field);
		}
		
		isReturnTypeEntity = true;
		this.returnType = entityDetails.getEntityType();
	}

	private void fetchReturnDetails(Method method)
	{
		logger.trace("Started method: fetchReturnDetails");
		
		this.returnType = method.getReturnType();

		if(void.class.equals(this.returnType))
		{
			throw new InvalidRepositoryException("Found void finder method '" + method.getName() + "' in repository: " + repositoryType.getName());
		}

		//TODO: Support map types
		if(Collection.class.isAssignableFrom(returnType))
		{
			if(returnType.isAssignableFrom(ArrayList.class))
			{
				this.collectionReturnType = ArrayList.class;
			}
			else if(returnType.isAssignableFrom(HashSet.class))
			{
				this.collectionReturnType = HashSet.class;
			}
			else
			{
				try
				{
					returnType.newInstance();
					this.collectionReturnType = returnType;
				}catch(Exception ex)
				{
					throw new InvalidRepositoryException("Unsupported collection return type found on finder '" 
								+ method.getName() + "' of repository: " + repositoryType.getName());
				}
			}
			
			ParameterizedType type = (ParameterizedType)method.getGenericReturnType();
			Type typeArgs[] = type.getActualTypeArguments();
			
			if(typeArgs.length != 1)
			{
				throw new InvalidRepositoryException("Unsupported collection return type (with mutliple type params) found on finder '" 
							+ method.getName() + "' of repository: " + repositoryType.getName());
			}
			
			this.returnType = (Class<?>)typeArgs[0];
		}
		
		if(entityDetails.getEntityType().equals(this.returnType) || ICrudRepository.class.equals(method.getDeclaringClass()))
		{
			setFullEntityDetails();
		}
		else if(method.getAnnotation(Field.class) != null)
		{
			Field field = method.getAnnotation(Field.class);
			FieldDetails fieldDetails = entityDetails.getFieldDetailsByField(field.value());
			
			if(!returnType.equals(fieldDetails.getField().getType()))
			{
				throw new InvalidRepositoryException("Return type '" + method.getReturnType().getName() 
							+ "' and field type '" + fieldDetails.getField().getName() 
							+ "' are mismatching for repository method: " + method.getName());
			}
			
			query.addColumn(fieldDetails.getColumn());
			returnColumnToField.put(fieldDetails.getColumn(), fieldDetails);
		}
		//TODO: Support bean return types which has column annotations on its fields
		else
		{
			throw new UnsupportedOperationException("Failed to determine return details of finder method: " + method.getName());
		}
	}
	
	private Object parseRecord(Record record, ConversionService conversionService)
	{
		logger.trace("Started method: parseRecord");
		
		if(!isReturnTypeEntity)
		{
			return conversionService.convertFromDataStore(record.getObject(0), returnColumnToField.get(record.getColumn(0)));
		}
		
		try
		{
			Object result = returnType.newInstance();
			FieldDetails fieldDetails = null;
			Object value = null;
			
			for(String column: returnColumnToField.keySet())
			{
				fieldDetails = returnColumnToField.get(column);
				
				value = record.getObject(column);
				value = conversionService.convertFromDataStore(value, fieldDetails);
			
				if(value == null)
				{
					continue;
				}
				
				fieldDetails.getField().set(result, value);
			}
			
			return result;
		}catch(Exception ex)
		{
			throw new PersistenceException("An error occurred while building result record of type: " + returnType.getName(), ex);
		}
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
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
			
			List<Record> records = dataStore.executeFinder(query, entityDetails);
			
			if(records == null || records.isEmpty())
			{
				//if primitive return type is expected simply return default value
				if(collectionReturnType == null)
				{
					return returnType.isPrimitive() ? CCGUtility.getDefaultPrimitiveValue(returnType) : null;
				}
				
				return Collections.emptyList();
			}
			
			if(collectionReturnType == null)
			{
				if(records.size() > 1)
				{
					throw new RecordCountMistmatchException("Multiple records found when single record is expected.");
				}
				
				return parseRecord(records.get(0), conversionService);
			}
			
			Collection<Object> lst = null;
			
			try
			{
				lst = (Collection)collectionReturnType.newInstance();
			}catch(Exception ex)
			{
				throw new IllegalStateException("An error occurred while creating return collection: " + collectionReturnType.getName(), ex);
			}
			
			for(Record record: records)
			{
				lst.add(parseRecord(record, conversionService));
			}
			
			return lst;
		}finally
		{
			queryLock.unlock();
		}
	}
	
	
}
