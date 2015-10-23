package com.fw.persistence.repository.executors;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fw.persistence.ChildConstraintViolationException;
import com.fw.persistence.EntityDetails;
import com.fw.persistence.FieldDetails;
import com.fw.persistence.ForeignConstraintDetails;
import com.fw.persistence.ICrudRepository;
import com.fw.persistence.IDataStore;
import com.fw.persistence.ITransaction;
import com.fw.persistence.PersistenceException;
import com.fw.persistence.conversion.ConversionService;
import com.fw.persistence.query.ChildrenExistenceQuery;
import com.fw.persistence.query.QueryCondition;
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
	private ReentrantLock queryLock = new ReentrantLock();
	private ConditionQueryBuilder conditionQueryBuilder;
	private String methodDesc;
	
	
	public DeleteQueryExecutor(Class<?> repositoryType, Method method, EntityDetails entityDetails)
	{
		super.entityDetails = entityDetails;
		super.repositoryType = repositoryType;
		
		conditionQueryBuilder = new ConditionQueryBuilder(entityDetails);
		methodDesc = String.format("delete method '%s' of repository - '%s'", method.getName(), repositoryType.getName());
		
		//try to find conditions based on annotations
		if(!super.fetchConditonsByAnnotations(method, true, conditionQueryBuilder, methodDesc, false))
		{
			//if conditions are not found based on annotations, try to find based on method name
			super.fetchConditionsByName(method, conditionQueryBuilder, methodDesc);
		}
		
		returnType = method.getReturnType();
		
		if(!boolean.class.equals(returnType) && !void.class.equals(returnType) && !int.class.equals(returnType))
		{
			throw new InvalidRepositoryException("Delete method '" + method.getName() + "' found with non-boolean, non-void and non-int return type in repository: " + repositoryType.getName());
		}
	}
	
	private void populateChildQuery(ForeignConstraintDetails childConstraint, DeleteQuery deleteQuery, IChildQuery childQuery, 
			IDataStore dataStore, ConversionService conversionService, Object... params)
	{
		logger.trace("Started method: populateChildQuery");
		
		//add conditions from main delete query as parent conditions
		if(deleteQuery.getConditions() != null)
		{
			for(QueryCondition condition: deleteQuery.getConditions())
			{
				childQuery.addParentCondition(condition.clone());
			}
		}
		
		//add parent to child mappings
		Field ownerField = childConstraint.getOwnerField();
		FieldDetails ownerFieldDetails = childConstraint.getOwnerEntityDetails().getFieldDetailsByField(ownerField.getName());
		EntityDetails childTargetEntity = childConstraint.getTargetEntityDetails();
		
		childQuery.addMapping( ownerFieldDetails.getColumn(), childTargetEntity.getIdField().getColumn());
	}
	
	/**
	 * Based on the "deleteCascade" enabled on child tables, child entities will be deleted recursively. 
	 * If deleteCascade is false, then this method ensures no child entities are refering the entity being deleted. If not an error will be thrown.
	 * Note - This functionality is mainly required for NO SQL DBs.
	 * @param dataStore
	 * @param conversionService
	 * @param params
	 */
	private void processChildConstraints(IDataStore dataStore, DeleteQuery deleteQuery, ConversionService conversionService, Object... params)
	{
		logger.trace("Started method: processChildConstraints");
		
		List<ForeignConstraintDetails> childConstraints = entityDetails.getChildConstraints();
		
		//if no child constraints are defined
		if(childConstraints == null || childConstraints.isEmpty())
		{
			return;
		}
		
		//DeleteChildrenQuery deleteChildQuery = null;
		ChildrenExistenceQuery childrenExistenceQuery = null;
		
		//loop through child constraints
		for(ForeignConstraintDetails childConstraint: childConstraints)
		{
			//if delete cascade is enabled
			if(childConstraint.isDeleteCascaded())
			{
				//fetch child entity ids referring to current entity
				//  This is needed to perform delete recursively
				FetchChildrenIdsQuery fetchChildrenIdsQuery = new FetchChildrenIdsQuery(childConstraint.getOwnerEntityDetails(), entityDetails);
				populateChildQuery(childConstraint, deleteQuery, fetchChildrenIdsQuery, dataStore, conversionService, params);
				
				List<Object> childrenIds = dataStore.fetchChildrenIds(fetchChildrenIdsQuery);
				
				//if child entities are present
				if(childrenIds != null)
				{
					//execute delete on child entities recursively 
					ICrudRepository<?> childRepository = super.getCrudRepository(childConstraint.getOwnerEntityDetails().getEntityType());
					
					for(Object childId: childrenIds)
					{
						childRepository.deleteById(childId);
					}
				}
			}
			//if delete cascade is not enabled
			else
			{
				//check if any child entities are referring to current entity
				childrenExistenceQuery = new ChildrenExistenceQuery(childConstraint.getOwnerEntityDetails(), entityDetails);
				
				populateChildQuery(childConstraint, deleteQuery, childrenExistenceQuery, dataStore, conversionService, params);
				
				if(dataStore.checkChildrenExistence(childrenExistenceQuery) > 0)
				{
					throw new ChildConstraintViolationException(childConstraint.getConstraintName(), "Found child items of type '" 
									+ childConstraint.getOwnerEntityDetails().getEntityType().getName() + "'");
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
			DeleteQuery deleteQuery = new DeleteQuery(entityDetails);
			conditionQueryBuilder.loadConditionalQuery(deleteQuery, params);
			
			//if datastore requires explicit child delete handling (like NOSQL DBs)
			if(dataStore.isExplicitForeignCheckRequired())
			{
				processChildConstraints(dataStore, deleteQuery, conversionService, params);
			}

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
