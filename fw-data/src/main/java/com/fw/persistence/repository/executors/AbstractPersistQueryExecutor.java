package com.fw.persistence.repository.executors;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fw.ccg.util.StringUtil;
import com.fw.persistence.EntityDetails;
import com.fw.persistence.FieldDetails;
import com.fw.persistence.ForeignConstraintDetails;
import com.fw.persistence.ForeignConstraintViolationException;
import com.fw.persistence.IDataStore;
import com.fw.persistence.Operator;
import com.fw.persistence.UniqueConstraintDetails;
import com.fw.persistence.UniqueConstraintViolationException;
import com.fw.persistence.conversion.ConversionService;
import com.fw.persistence.query.QueryCondition;
import com.fw.persistence.query.CountQuery;

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
		
		CountQuery existenceQuery = new CountQuery(entityDetails);
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
			
			existenceQuery.clearConditions();
			fieldValues.clear();
			
			for(String field: uniqueConstraint.getFields())
			{
				fieldDetails = entityDetails.getFieldDetailsByField(field);
				
				value = fieldDetails.getValue(entity);
				value = conversionService.convertToDBType(value, fieldDetails);
				
				existenceQuery.addCondition(new QueryCondition(null, fieldDetails.getColumn(), Operator.EQ, value));
				fieldValues.put(field, value);
			}
			
			if(excludeId)
			{
				existenceQuery.addCondition(new QueryCondition(null, entityDetails.getIdField().getColumn(), Operator.NE, entityDetails.getIdField().getValue(entity)));
			}
			
			if(dataStore.getCount(existenceQuery, entityDetails) > 0)
			{
				message = formatMessage(uniqueConstraint.getMessage(), fieldValues);
				message = (message != null) ? message : "Unique constraint violated: " + uniqueConstraint.getName();
				
				throw new UniqueConstraintViolationException(uniqueConstraint.getName(), message);
			}
		}
	}
	
	protected void checkForForeignConstraints(IDataStore dataStore, ConversionService conversionService, Object entity)
	{
		//if explicit foreign key validation is not required
		if(!dataStore.isExplicitForeignCheckRequired())
		{
			return;
		}
		
		logger.trace("Started method: checkForForeignConstraints");
		
		CountQuery existenceQuery = null;
		Object value = null;
		String message = null;
		EntityDetails foreignEntityDetails = null;
		
		FieldDetails ownerFieldDetails = null;
		
		//validate foreign constraint violation is not happening
		for(ForeignConstraintDetails foreignConstraint: entityDetails.getForeignConstraints())
		{
			//if current entity does not own this relation
			if(foreignConstraint.isMappedRelation())
			{
				continue;
			}
			
			//create existence query that needs to be executed against parent table
			existenceQuery = new CountQuery(foreignConstraint.getTargetEntityDetails());

			foreignEntityDetails = foreignConstraint.getTargetEntityDetails();
			ownerFieldDetails = foreignEntityDetails.getFieldDetailsByField(foreignConstraint.getOwnerField().getName());

			value = ownerFieldDetails.getValue(entity);
			value = conversionService.convertToDBType(value, ownerFieldDetails);

			//if no value is defined for relationship
			if(value == null)
			{
				continue;
			}
			
			existenceQuery.addCondition(new QueryCondition(null, foreignEntityDetails.getIdField().getColumn(), Operator.EQ, value));
			
			if(dataStore.getCount(existenceQuery, foreignEntityDetails) <= 0)
			{
				message = "Foreign constraint violated: " + foreignConstraint.getConstraintName();
				
				logger.error(message);
				throw new ForeignConstraintViolationException(foreignConstraint.getConstraintName(), message);
			}
		}
	}
}
