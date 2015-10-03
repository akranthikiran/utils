package com.fw.persistence.repository.executors;

import java.lang.reflect.Method;

import javax.persistence.GenerationType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fw.persistence.EntityDetails;
import com.fw.persistence.FieldDetails;
import com.fw.persistence.IDataStore;
import com.fw.persistence.ITransaction;
import com.fw.persistence.conversion.ConversionService;
import com.fw.persistence.query.ColumnParam;
import com.fw.persistence.query.SaveOrUpdateQuery;
import com.fw.persistence.repository.InvalidRepositoryException;

@QueryExecutorPattern(prefixes = {"saveOrUpdate"})
public class SaveOrUpdateQueryExecutor extends AbstractPersistQueryExecutor
{
	private static Logger logger = LogManager.getLogger(SaveOrUpdateQueryExecutor.class);

	private Class<?> returnType;
	
	public SaveOrUpdateQueryExecutor(Class<?> repositoryType, Method method, EntityDetails entityDetails)
	{
		super.entityDetails = entityDetails;
		super.repositoryType = repositoryType;
		
		Class<?> paramTypes[] = method.getParameterTypes();

		if(paramTypes == null || paramTypes.length != 1)
		{
			throw new InvalidRepositoryException("Non-entity save-update method '" + method.getName() + "' specified in repository: " + repositoryType.getName());
		}
		
		returnType = method.getReturnType();
		
		if(!boolean.class.equals(returnType) && !void.class.equals(returnType))
		{
			throw new InvalidRepositoryException("Save-Update method '" + method.getName() + "' found with non-boolean and non-void return type in repository: " + repositoryType.getName());
		}
	}

	@Override
	public Object execute(IDataStore dataStore, ConversionService conversionService, Object... params)
	{
		logger.trace("Started method: execute");
		
		if(params[0] == null)
		{
			throw new NullPointerException("Entity can not be null");
		}
		
		Object entity = params[0];
			
		//check if all foreign parent keys are available
		checkForForeignConstraints(dataStore, conversionService, entity);

		SaveOrUpdateQuery query = new SaveOrUpdateQuery(entityDetails);
		Object value = null;
		
		for(FieldDetails field: entityDetails.getFieldDetails())
		{
			if(field.isIdField())
			{
				if(field.getGenerationType() == GenerationType.IDENTITY)
				{
					continue;
				}
				
				if(field.getGenerationType() == GenerationType.SEQUENCE)
				{
					query.addInsertColumn(new ColumnParam(field.getColumn(), null, -1, field.getSequenceName()));
					continue;
				}
				
				query.addInsertColumn(new ColumnParam(field.getColumn(), field.getValue(entity), -1, null));
				continue;
			}
			
			value = field.getValue(entity);
			value = conversionService.convertToDBType(value, field);
			
			query.addInsertColumn(new ColumnParam(field.getColumn(), value, -1));
			
			query.addUpdateColumn(new ColumnParam(field.getColumn(), value, -1));
		}
		
		//save the entity and audit entry, if needed, as single transaction.
		try(ITransaction transaction = dataStore.getTransactionManager().newOrExistingTransaction())
		{
			int res = dataStore.saveOrUpdate(query, entityDetails);

			//if update/insert is successful
			if(res > 0)
			{
				//fetch the newly save entry id and populate it to entity
				super.fetchId(entity, dataStore, conversionService);
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
	}
}
