package com.fw.persistence.repository.executors;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fw.persistence.AuditType;
import com.fw.persistence.ChildConstraintViolationException;
import com.fw.persistence.EntityDetails;
import com.fw.persistence.FieldDetails;
import com.fw.persistence.ForeignConstraintDetails;
import com.fw.persistence.ICrudRepository;
import com.fw.persistence.IDataStore;
import com.fw.persistence.ITransaction;
import com.fw.persistence.PersistenceException;
import com.fw.persistence.annotations.DeleteCascade;
import com.fw.persistence.conversion.ConversionService;
import com.fw.persistence.query.ChildrenExistenceQuery;
import com.fw.persistence.query.ConditionParam;
import com.fw.persistence.query.DeleteQuery;
import com.fw.persistence.query.FetchChildrenIdsQuery;
import com.fw.persistence.query.IChildQuery;
import com.fw.persistence.repository.InvalidRepositoryException;

/**
 * Conditions are not mandatory for delete query
 * @author akkink1
 *
 */
@QueryExecutorPattern(prefixes = {"delete"})
public class DeleteQueryExecutor extends AbstractPersistQueryExecutor
{
	private static Logger logger = LogManager.getLogger(DeleteQueryExecutor.class);
	
	private Class<?> returnType;
	private DeleteQuery deleteQuery;
	private ReentrantLock queryLock = new ReentrantLock();
	
	public DeleteQueryExecutor(Class<?> repositoryType, Method method, EntityDetails entityDetails)
	{
		super.entityDetails = entityDetails;
		super.repositoryType = repositoryType;
		
		deleteQuery = new DeleteQuery(entityDetails);
		
		if(!super.fetchConditonsByAnnotations(method, deleteQuery, false))
		{
			super.fetchConditionsByName(method, deleteQuery, "delete");
		}
		
		returnType = method.getReturnType();
		
		if(!boolean.class.equals(returnType) && !void.class.equals(returnType) && !int.class.equals(returnType))
		{
			throw new InvalidRepositoryException("Update method '" + method.getName() + "' found with non-boolean, non-void and non-int return type in repository: " + repositoryType.getName());
		}
	}
	
	private void populateChildQuery(ForeignConstraintDetails childConstraint, IChildQuery childQuery, 
			IDataStore dataStore, ConversionService conversionService, Object... params)
	{
		logger.trace("Started method: populateChildQuery");
		
		//add conditions from main delete query as parent conditions
		if(deleteQuery.getConditions() != null)
		{
			for(ConditionParam condition: deleteQuery.getConditions())
			{
				childQuery.addParentCondition(new ConditionParam(condition.getColumn(), params[condition.getIndex()], -1));
			}
		}
		
		//add parent conditions from child constraint
		Map<String, Object> parentConditions = childConstraint.getParentColumnConditions();
		Object value = null;
		
		
		if(parentConditions != null)
		{
			for(String column : parentConditions.keySet())
			{
				value = parentConditions.get(column);
				value = conversionService.convertToDataStore(value, null);
				
				childQuery.addParentCondition(new ConditionParam(column, value, -1));
			}
		}
	
		//add child constraint from child constraint
		Map<String, Object> childConditions = childConstraint.getChildColumnConditions();
		
		if(childConditions != null)
		{
			for(String column : childConditions.keySet())
			{
				value = childConditions.get(column);
				value = conversionService.convertToDataStore(value, null);
				
				childQuery.addChildCondition(new ConditionParam(column, value, -1));
			}
		}
		
		//add parent to child mappings
		Map<String, String> fieldMapping = childConstraint.getFields();
		FieldDetails fieldDetails = null;
		FieldDetails foreignFieldDetails = null;
		
		for(String childField: fieldMapping.keySet())
		{
			fieldDetails = childConstraint.getEntityDetails().getFieldDetailsByField(childField);
			foreignFieldDetails = childConstraint.getForeignEntity().getFieldDetailsByField(fieldMapping.get(childField));
			
			childQuery.addMapping(fieldDetails.getColumn(), foreignFieldDetails.getColumn());
		}
	}
	
	private void processChildConstraints(IDataStore dataStore, ConversionService conversionService, Object... params)
	{
		logger.trace("Started method: processChildConstraints");
		
		List<ForeignConstraintDetails> childConstraints = entityDetails.getChildConstraints();
		
		if(childConstraints == null || childConstraints.isEmpty())
		{
			return;
		}
		
		//DeleteChildrenQuery deleteChildQuery = null;
		ChildrenExistenceQuery childrenExistenceQuery = null;
		
		for(ForeignConstraintDetails childConstraint: childConstraints)
		{
			if(childConstraint.getDeleteCascade() == DeleteCascade.DELETE_WITH_PARENT)
			{
				/*
				deleteChildQuery = new DeleteChildrenQuery(childConstraint.getEntityDetails(), entityDetails);
				
				populateChildQuery(childConstraint, deleteChildQuery, dataStore, conversionService, params);

				//TODO: Child deletion should use its corresponding repository, so that grand-childs are deleted first recursively
				//dataStore.deleteChildren(deleteChildQuery);
				*/
				
				FetchChildrenIdsQuery fetchChildrenIdsQuery = new FetchChildrenIdsQuery(childConstraint.getEntityDetails(), entityDetails);
				populateChildQuery(childConstraint, fetchChildrenIdsQuery, dataStore, conversionService, params);
				
				List<Object> childrenIds = dataStore.fetchChildrenIds(fetchChildrenIdsQuery);
				
				if(childrenIds != null)
				{
					ICrudRepository<?> childRepository = super.getCrudRepository(childConstraint.getEntityDetails().getEntityType());
					
					for(Object childId: childrenIds)
					{
						childRepository.deleteById(childId);
					}
				}
			}
			else
			{
				childrenExistenceQuery = new ChildrenExistenceQuery(childConstraint.getEntityDetails(), entityDetails);
				
				populateChildQuery(childConstraint, childrenExistenceQuery, dataStore, conversionService, params);
				
				if(dataStore.checkChildrenExistence(childrenExistenceQuery) > 0)
				{
					throw new ChildConstraintViolationException(childConstraint.getName(), "Found child items of type '" 
									+ childConstraint.getEntityDetails().getEntityType().getName() + "'");
				}
			}
		}
	}
	
	@Override
	public Object execute(IDataStore dataStore, ConversionService conversionService, Object... params)
	{
		logger.trace("Started method: execute");
		
		queryLock.lock();
		
		try(ITransaction transaction = dataStore.getTransactionManager().newOrExistingTransaction())
		{
			if(deleteQuery.getConditions() != null)
			{
				Object value = null;
				
				for(ConditionParam condition: deleteQuery.getConditions())
				{
					value = params[condition.getIndex()];
					value = conversionService.convertToDataStore(value, null);
					
					condition.setValue(value);
				}
			}
			
			processChildConstraints(dataStore, conversionService, params);

			//add audit entries based on same conditions as delete
			super.addAuditEntries(dataStore, entityDetails, AuditType.DELETE, deleteQuery.getConditions().toArray(new ConditionParam[0]));
			
			int res = dataStore.delete(deleteQuery, entityDetails);
			
			transaction.commit();
			
			if(int.class.equals(returnType))
			{
				return res;
			}
			
			return (boolean.class.equals(returnType)) ? (res > 0) : null;
		}catch(PersistenceException ex){
			throw ex;
		}catch(Exception ex)
		{
			throw new PersistenceException("An error occured while deleting entity", ex);
		}finally
		{
			queryLock.unlock();
		}
		
	}
}
