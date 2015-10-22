package com.fw.persistence.repository.executors;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fw.persistence.EntityDetails;
import com.fw.persistence.FieldDetails;
import com.fw.persistence.ICrudRepository;
import com.fw.persistence.IDataStore;
import com.fw.persistence.ITransaction;
import com.fw.persistence.Operator;
import com.fw.persistence.conversion.ConversionService;
import com.fw.persistence.query.ColumnParam;
import com.fw.persistence.query.QueryCondition;
import com.fw.persistence.query.UpdateQuery;
import com.fw.persistence.repository.InvalidRepositoryException;
import com.fw.persistence.repository.annotations.Field;

@QueryExecutorPattern(prefixes = {"update"})
public class UpdateQueryExecutor extends AbstractPersistQueryExecutor
{
	private static Logger logger = LogManager.getLogger(UpdateQueryExecutor.class);

	private Class<?> returnType;
	private ReentrantLock queryLock = new ReentrantLock();
	private boolean entityUpdate = false;
	private ConditionQueryBuilder conditionQueryBuilder;
	private String methodDesc;
	
	private UpdateQuery updateQuery;
	
	public UpdateQueryExecutor(Class<?> repositoryType, Method method, EntityDetails entityDetails)
	{
		super.entityDetails = entityDetails;
		super.repositoryType = repositoryType;
		
		conditionQueryBuilder = new ConditionQueryBuilder(entityDetails);
		methodDesc = String.format("update method '%s' of repository - '%s'", method.getName(), repositoryType.getName());
		
		
		Class<?> paramTypes[] = method.getParameterTypes();

		if(paramTypes == null || paramTypes.length == 0)
		{
			throw new InvalidRepositoryException("Zero parameter update method '" + method.getName() + "' in repository: " + repositoryType.getName());
		}
		
		boolean isCoreInterface = ICrudRepository.class.equals(method.getDeclaringClass());
		
		if((paramTypes.length == 1 && entityDetails.getEntityType().equals(paramTypes[0])) || isCoreInterface)
		{
			entityUpdate = true;
		}
		else
		{
			updateQuery = new UpdateQuery(entityDetails);
			
			if(!super.fetchConditonsByAnnotations(method, false, conditionQueryBuilder, methodDesc))
			{
				throw new InvalidRepositoryException("For non-entity update method '" + method.getName() + "' no conditions are specified, in repository: " + repositoryType.getName());
			}
			
			if(!fetchColumnsByAnnotations(method))
			{
				throw new InvalidRepositoryException("For non-entity update method '" + method.getName() + "' no columns are specified, in repository: " + repositoryType.getName());
			}
		}
		
		returnType = method.getReturnType();
		
		if(!boolean.class.equals(returnType) && !void.class.equals(returnType) && !int.class.equals(returnType))
		{
			throw new InvalidRepositoryException("Update method '" + method.getName() + "' found with non-boolean, non-void and non-int return type in repository: " + repositoryType.getName());
		}
	}
	
	private boolean fetchColumnsByAnnotations(Method method)
	{
		logger.trace("Started method: fetchColumnsByAnnotations");
		
		Class<?> paramTypes[] = method.getParameterTypes();
		Annotation paramAnnotations[][] = method.getParameterAnnotations();
		
		if(paramAnnotations == null)
		{
			return false;
		}

		Field field = null;
		boolean found = false;
		FieldDetails fieldDetails = null;
		
		//fetch conditions for each argument
		for(int i = 0; i < paramTypes.length; i++)
		{
			field = getAnnotation(paramAnnotations[i], Field.class);
			
			if(field == null)
			{
				continue;
			}
			
			fieldDetails = this.entityDetails.getFieldDetailsByField(field.value());
			
			if(fieldDetails == null)
			{
				throw new InvalidRepositoryException("Invalid @Field with name '" + field.value() + "' is specified for update method '" 
						+ method.getName() + "' of repository: " + repositoryType.getName());
			}
			
			updateQuery.addColumn(new ColumnParam(fieldDetails.getColumn(), null, i));
			found = true;
		}

		return found;
	}
	
	private Object updateFullEntity(IDataStore dataStore, ConversionService conversionService, Object entity)
	{
		logger.trace("Started method: updateFullEntity");
		
		if(entity == null)
		{
			throw new NullPointerException("Entity can not be null");
		}
			
		//check if unique constraints are getting violated
		checkForUniqueConstraints(dataStore, conversionService, entity, true);//TODO: Read only fields should be skipped
		
		//check if all foreign parent keys are available
		checkForForeignConstraints(dataStore, conversionService, entity);

		UpdateQuery query = new UpdateQuery(entityDetails);
		Object value = null;
		
		for(FieldDetails field: entityDetails.getFieldDetails())
		{
			if(field.isIdField())
			{
				continue;
			}
			
			value = conversionService.convertToDBType(field.getValue(entity), field);
			
			query.addColumn(new ColumnParam(field.getColumn(), value, -1));
		}
		
		query.addCondition(new QueryCondition(null, entityDetails.getIdField().getColumn(), Operator.EQ, entityDetails.getIdField().getValue(entity)));
		
		int res = dataStore.update(query, entityDetails);
		
		if(boolean.class.equals(returnType))
		{
			return (res > 0);
		}
		
		return (int.class.equals(returnType)) ? res : null;
		
	}

	
	@Override
	public Object execute(IDataStore dataStore, ConversionService conversionService, Object... params)
	{
		logger.trace("Started method: execute");
		
		if(entityUpdate)
		{
			return updateFullEntity(dataStore, conversionService, params[0]);
		}
		
		queryLock.lock();
		
		try
		{
			Object value = null;
			
			updateQuery.clearConditions();
			conditionQueryBuilder.loadConditionalQuery(updateQuery, params);
			
			//TODO: When unique fields are getting updated, make sure unique constraints are not violated
				//during unique field update might be we have to mandate id is provided as condition
			
			for(ColumnParam column: updateQuery.getColumns())
			{
				value = conversionService.convertToDBType(params[column.getIndex()], entityDetails.getFieldDetailsByColumn(column.getName()));
				column.setValue(value);
			}
			
			try(ITransaction transaction = dataStore.getTransactionManager().newOrExistingTransaction())
			{
				int res = dataStore.update(updateQuery, entityDetails);
				
				if(int.class.equals(returnType))
				{
					return res;
				}
				
				transaction.commit();
				return (boolean.class.equals(returnType)) ? (res > 0) : null;
			}catch(Exception ex)
			{
				//rethrow the catched exception
				if(ex instanceof RuntimeException)
				{
					throw (RuntimeException)ex;
				}
				
				throw new IllegalStateException(ex);
			}
		}finally
		{
			queryLock.unlock();
		}
		
	}
}
