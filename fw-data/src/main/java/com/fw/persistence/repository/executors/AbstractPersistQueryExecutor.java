package com.fw.persistence.repository.executors;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fw.ccg.util.StringUtil;
import com.fw.persistence.AuditType;
import com.fw.persistence.EntityDetails;
import com.fw.persistence.FieldDetails;
import com.fw.persistence.ForeignConstraintDetails;
import com.fw.persistence.ForeignConstraintViolationException;
import com.fw.persistence.IDataStore;
import com.fw.persistence.Operator;
import com.fw.persistence.Record;
import com.fw.persistence.UniqueConstraintDetails;
import com.fw.persistence.UniqueConstraintViolationException;
import com.fw.persistence.conversion.ConversionService;
import com.fw.persistence.query.AuditEntryQuery;
import com.fw.persistence.query.ConditionParam;
import com.fw.persistence.query.ExistenceQuery;
import com.fw.persistence.query.FinderQuery;

public abstract class AbstractPersistQueryExecutor extends QueryExecutor
{
	private static Logger logger = LogManager.getLogger(AbstractPersistQueryExecutor.class);
	
	private String formatMessage(String messageTemplate, Map<String, Object> context)
	{
		if(messageTemplate == null || messageTemplate.trim().length() == 0)
		{
			return null;
		}
		
		return StringUtil.getPatternString(messageTemplate, context);
	}
	
	protected void checkForUniqueConstraints(IDataStore dataStore, ConversionService conversionService, Object entity, boolean excludeId)
	{
		logger.trace("Started method: checkForUniqueConstraints");
		
		ExistenceQuery existenceQuery = new ExistenceQuery(entityDetails);
		FieldDetails fieldDetails = null;
		Object value = null;
		String message = null;
		Map<String, Object> fieldValues = new HashMap<>();
		
		//validate unique constraint violation is not happening
		for(UniqueConstraintDetails uniqueConstraint: entityDetails.getUniqueConstraints())
		{
			if(!uniqueConstraint.isValidate())
			{
				continue;
			}
			
			existenceQuery.reset();
			fieldValues.clear();
			
			for(String field: uniqueConstraint.getFields())
			{
				fieldDetails = entityDetails.getFieldDetailsByField(field);
				
				value = fieldDetails.getValue(entity);
				value = conversionService.convertToDBType(value, fieldDetails);
				
				existenceQuery.addCondition(new ConditionParam(fieldDetails.getColumn(), value, -1));
				fieldValues.put(field, value);
			}
			
			if(excludeId)
			{
				existenceQuery.addCondition(new ConditionParam(entityDetails.getIdField().getColumn(), Operator.NE, entityDetails.getIdField().getValue(entity), -1));
			}
			
			if(dataStore.checkForExistenence(existenceQuery, entityDetails) > 0)
			{
				message = formatMessage(uniqueConstraint.getMessage(), fieldValues);
				message = (message != null) ? message : "Unique constraint violated: " + uniqueConstraint.getName();
				
				throw new UniqueConstraintViolationException(uniqueConstraint.getName(), message);
			}
		}
	}
	
	protected void checkForForeignConstraints(IDataStore dataStore, ConversionService conversionService, Object entity)
	{
		logger.trace("Started method: checkForForeignConstraints");
		
		ExistenceQuery existenceQuery = null;
		FieldDetails fieldDetails = null;
		Object value = null;
		String message = null;
		EntityDetails foreignEntityDetails = null;
		FieldDetails foreignFieldDetails = null;
		Map<String, String> fieldToForeign = null;
		Map<String, Object> conditions = null;
		Object entityValue = null;
		Map<String, Object> fieldValueMap = new HashMap<>();
		boolean nonNullFound = false;
		
		//validate foreign constraint violation is not happening
		CONSTRAINT_LOOP: for(ForeignConstraintDetails foreignConstraint: entityDetails.getForeignConstraints())
		{
			if(!foreignConstraint.isValidate())
			{
				continue;
			}
			
			if(foreignConstraint.hasConditions())
			{
				conditions = foreignConstraint.getChildColumnConditions();
				
				for(String column: conditions.keySet())
				{
					fieldDetails = entityDetails.getFieldDetailsByColumn(column);
					value = conditions.get(column);
					entityValue = fieldDetails.getValue(entity);
					
					if(!value.equals(entityValue))
					{
						logger.trace("For {} skipping foreign constraint validation '{}' as column '{}' condition value '{}' is not matching with actual value '{}'", 
								entityDetails.getEntityType().getName(), foreignConstraint.getName(), column, value, entityValue);
						
						continue CONSTRAINT_LOOP;
					}
				}
			}
			
			//create existence query that needs to be executed against parent table
			existenceQuery = new ExistenceQuery(foreignConstraint.getForeignEntity());
			
			fieldValueMap.clear();
			nonNullFound = false;
			
			foreignEntityDetails = foreignConstraint.getForeignEntity();
			fieldToForeign = foreignConstraint.getFields();
			
			for(String field: fieldToForeign.keySet())
			{
				fieldDetails = entityDetails.getFieldDetailsByField(field);
				
				value = fieldDetails.getValue(entity);
				value = conversionService.convertToDBType(value, fieldDetails);
				
				foreignFieldDetails = foreignEntityDetails.getFieldDetailsByField(fieldToForeign.get(field));
				
				existenceQuery.addCondition(new ConditionParam(foreignFieldDetails.getColumn(), value, -1));
				fieldValueMap.put(field, value);
				
				if(value != null)
				{
					nonNullFound = true;
				}
			}
			
			//if all values are null computing foreign relationship, ignore this relation
			if(!nonNullFound)
			{
				continue;
			}
			
			if(foreignConstraint.hasParentConditions())
			{
				conditions = foreignConstraint.getParentColumnConditions();
				
				for(String column: conditions.keySet())
				{
					value = conditions.get(column);
					value = conversionService.convertToDBType(value, null);
					
					existenceQuery.addCondition(new ConditionParam(column, value, -1));
				}
			}
			
			if(dataStore.checkForExistenence(existenceQuery, foreignEntityDetails) <= 0)
			{
				message = formatMessage(foreignConstraint.getMessage(), fieldValueMap);
				message = (message != null) ? message : "Foreign constraint violated: " + foreignConstraint.getName();
				
				logger.error(message);
				throw new ForeignConstraintViolationException(foreignConstraint.getName(), message);
			}
		}
	}
	
	protected Object fetchId(Object entity, IDataStore dataStore, ConversionService conversionService)
	{
		logger.trace("Started method: fetchId");
		
		FieldDetails idFieldDetails = entityDetails.getIdField();
		
		if(!idFieldDetails.isAutoFetch())
		{
			return null;
		}
		
		Collection<UniqueConstraintDetails> uniqueConstraints = entityDetails.getUniqueConstraints();
		
		if(uniqueConstraints == null || uniqueConstraints.isEmpty())
		{
			logger.warn("No unique contraint found on entity '" + entityDetails.getEntityType().getName() + "' for fetching generated id");
			return null;
		}
		
		UniqueConstraintDetails uniqueConstraint = uniqueConstraints.iterator().next();
		
		FinderQuery findQuery = new FinderQuery(entityDetails);
		findQuery.addColumn(idFieldDetails.getColumn());
		
		FieldDetails fieldDetails = null;
		Object value = null;
		
		for(String field: uniqueConstraint.getFields())
		{
			fieldDetails = entityDetails.getFieldDetailsByField(field);
			
			value = fieldDetails.getValue(entity);
			value = conversionService.convertToDBType(value, fieldDetails);
			
			findQuery.addCondition(new ConditionParam(fieldDetails.getColumn(), value, -1));
		}
		
		List<Record> records = dataStore.executeFinder(findQuery, entityDetails);
		
		if(records == null || records.isEmpty())
		{
			return null;
		}
		
		Object idValue = conversionService.convertToJavaType(records.get(0).getObject(0), idFieldDetails);
		idFieldDetails.setValue(entity, idValue);
		
		return idValue;
	}

	/**
	 * Adds audit entries if needed and enabled.
	 * 
	 * @param context
	 * @param dataStore
	 * @param entityDetails
	 * @param conditions
	 */
	protected void addAuditEntries(IDataStore dataStore, EntityDetails entityDetails, AuditType auditType, ConditionParam... conditions)
	{
		//if entity does not need Audit return
		if(!entityDetails.isAuditRequired())
		{
			return;
		}
		
		//if audit is disabled for current context, return
		if(!context.isAuditEnabled())
		{
			logger.debug("As audit was disabled in persistence-context skipping audit for entity: " + entityDetails.getEntityType().getName());
			return;
		}
		
		//create audit query and add provided conditions
		AuditEntryQuery auditQuery = new AuditEntryQuery(entityDetails, auditType, context.getCurrentUser());
		
		for(ConditionParam conditionParam: conditions)
		{
			auditQuery.addCondition(conditionParam);
		}
		
		dataStore.addAuditEntries(auditQuery);
	}
}
