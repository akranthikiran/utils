package com.fw.persistence.repository.executors;

import java.lang.reflect.Method;

import javax.persistence.GenerationType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fw.persistence.EntityDetails;
import com.fw.persistence.FieldDetails;
import com.fw.persistence.ICrudRepository;
import com.fw.persistence.IDataStore;
import com.fw.persistence.ITransaction;
import com.fw.persistence.conversion.ConversionService;
import com.fw.persistence.query.ColumnParam;
import com.fw.persistence.query.SaveQuery;
import com.fw.persistence.repository.InvalidRepositoryException;

@QueryExecutorPattern(prefixes = {"save"})
public class SaveQueryExecutor extends AbstractPersistQueryExecutor
{
	private static Logger logger = LogManager.getLogger(SaveQueryExecutor.class);
	private Class<?> returnType;
	
	public SaveQueryExecutor(Class<?> repositoryType, Method method, EntityDetails entityDetails)
	{
		super.entityDetails = entityDetails;
		super.repositoryType = repositoryType;

		Class<?> paramTypes[] = method.getParameterTypes();
		boolean isCoreInterface = ICrudRepository.class.equals(method.getDeclaringClass());

		if(paramTypes.length != 1)
		{
			throw new InvalidRepositoryException("Non-single parameter save method '" + method.getName() + "' in repository: " + repositoryType.getName());
		}
		
		if(!entityDetails.getEntityType().equals(paramTypes[0]) && !isCoreInterface)
		{
			throw new InvalidRepositoryException("Save method '" + method.getName() + "' found with non-entity parameter in repository: " + repositoryType.getName());
		}
		
		returnType = method.getReturnType();
		
		if(!boolean.class.equals(returnType) && !void.class.equals(returnType))
		{
			throw new InvalidRepositoryException("Save method '" + method.getName() + "' found with non-boolean and non-void return type in repository: " + repositoryType.getName());
		}
	}
	
	@Override
	public Object execute(IDataStore dataStore, ConversionService conversionService, Object... params)
	{
		logger.trace("Started method: execute");
		
		Object entity = params[0];

		if(entity == null)
		{
			throw new NullPointerException("Entity can not be null");
		}
			
		//check if unique constraints are getting violated
		checkForUniqueConstraints(dataStore, conversionService, entity, false);
		
		//check if all foreign parent keys are available
		checkForForeignConstraints(dataStore, conversionService, entity);

		SaveQuery query = new SaveQuery(entityDetails);
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
					query.addColumn(new ColumnParam(field.getColumn(), null, -1, field.getSequenceName()));
					continue;
				}
				
				query.addColumn(new ColumnParam(field.getColumn(), field.getValue(entity), -1, null));
				continue;
			}
			
			value = conversionService.convertToDBType(field.getValue(entity), field);
			
			query.addColumn(new ColumnParam(field.getColumn(), value, -1));
		}
		
		//save the entity and audit entry, if needed, as single transaction.
		try(ITransaction transaction = dataStore.getTransactionManager().newOrExistingTransaction())
		{
			int res = dataStore.save(query, entityDetails);
			
			//if insert was successful
			if(res > 0)
			{
				//fetch the newly save entry id and populate it to entity
				fetchId(entity, dataStore, conversionService);
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
