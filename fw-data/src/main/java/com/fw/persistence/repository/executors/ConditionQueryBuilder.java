package com.fw.persistence.repository.executors;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;

import com.fw.persistence.EntityDetails;
import com.fw.persistence.FieldDetails;
import com.fw.persistence.ForeignConstraintDetails;
import com.fw.persistence.InvalidMappingException;
import com.fw.persistence.JoinTableDetails;
import com.fw.persistence.Operator;
import com.fw.persistence.Record;
import com.fw.persistence.conversion.ConversionService;
import com.fw.persistence.query.IConditionalQuery;
import com.fw.persistence.query.QueryCondition;
import com.fw.persistence.query.QueryJoinCondition;
import com.fw.persistence.query.QueryResultField;
import com.fw.persistence.query.QueryTable;
import com.fw.persistence.repository.PersistenceExecutionContext;
import com.fw.persistence.repository.RepositoryFactory;
import com.fw.persistence.repository.executors.proxy.ProxyEntityCreator;
import com.fw.utils.ConvertUtils;
import com.fw.utils.ObjectWrapper;

/**
 * Builder that can be used to keep track of conditions/columns involved and their dependencies.
 * And finally build the required query.
 * 
 * @author akiran
 */
public class ConditionQueryBuilder
{
	private static final String DEF_TABLE_CODE = "T0";
	
	/**
	 * Table information required by the query
	 * @author akiran
	 */
	private static class TableInfo
	{
		/**
		 * Short code for table
		 */
		private String tableCode;
		
		/**
		 * Table name
		 */
		private String tableName;
		
		/**
		 * Entity details representing this table
		 */
		private EntityDetails entityDetails;
		
		/**
		 * Table code to which this table has to join (in order to get joined with main table)
		 */
		private String joinTableCode;
		
		/**
		 * Column to be used in current table for join
		 */
		private String joinTableColumn;
		
		/**
		 * Column to be used in target table for join
		 */
		private String column;

		public TableInfo(String tableCode, String tableName, EntityDetails entityDetails, String joinTableCode, String sourceColumn, String targetColumn)
		{
			this.tableCode = tableCode;
			this.tableName = tableName;
			this.entityDetails = entityDetails;
			this.joinTableCode = joinTableCode;
			this.joinTableColumn = sourceColumn;
			this.column = targetColumn;
		}
	}
	
	/**
	 * Condition details to be used in the query
	 * @author akiran
	 */
	private static class Condition
	{
		/**
		 * Condition operator
		 */
		private Operator operator;
		
		/**
		 * Index of the parameter defining this condition
		 */
		private int index;
		
		/**
		 * Embedded property name to be used when condition object in use
		 */
		private String embeddedProperty;
		
		/**
		 * Entity field expression
		 */
		@SuppressWarnings("unused")
		private String fieldExpression;
		
		/**
		 * Table on which this condition needs to be applied
		 */
		private TableInfo table;
		
		/**
		 * Field details (with column name) that can be used for condition
		 */
		private FieldDetails fieldDetails;

		public Condition(Operator operator, int index, String embeddedProperty, String fieldExpression)
		{
			this.operator = operator;
			this.index = index;
			this.embeddedProperty = embeddedProperty;
			this.fieldExpression = fieldExpression;
		}
		
		
		/**
		 * Converts the condition into bean expression that can be used to fetch condition value from method parameters
		 * @return
		 */
		public String getConditionExpression()
		{
			StringBuilder builder = new StringBuilder("parameters[").append(index).append("]");
			
			if(embeddedProperty != null)
			{
				builder.append(".").append(embeddedProperty);
			}
			
			return builder.toString();
		}
	}
	
	/**
	 * Result field details that is part current query
	 * @author akiran
	 */
	private static class ResultField
	{
		/**
		 * Result property to which resultant value should be populated
		 */
		private String property;
		
		/**
		 * Short code for result field
		 */
		private String code;
		
		/**
		 * Table from which value can be fetched
		 */
		private TableInfo table;
		
		/**
		 * Field details (with column) from which value can be fetched
		 */
		private FieldDetails fieldDetails;
		
		/**
		 * Type of result field
		 */
		private Class<?> fieldType;

		public ResultField(String property, String code, Class<?> fieldType)
		{
			this.property = property;
			this.code = code;
			this.fieldType = fieldType;
		}
	}
	
	/**
	 * Bean context that can be used to parse/process expressions
	 * @author akiran
	 */
	public static class ParameterContext
	{
		private Object parameters[];

		public ParameterContext(Object[] parameters)
		{
			this.parameters = parameters;
		}
		
		public Object[] getParameters()
		{
			return parameters;
		}
	}

	/**
	 * Entity details on which this query is going to be executed
	 */
	private EntityDetails entityDetails;
	
	/**
	 * List of conditions of this query
	 */
	private List<Condition> conditions = new ArrayList<>();
	
	/**
	 * List of result fields of this query
	 */
	private List<ResultField> resultFields = new ArrayList<>();

	/**
	 * Mapping from property name to table
	 */
	private Map<String, TableInfo> propToTable = new HashMap<>();
	
	
	/**
	 * Mapping from table-short-code to the table. Which will be needed while giving join info
	 */
	private Map<String, TableInfo> codeToTable = new HashMap<>();
	
	/**
	 * Counter for generating unique table codes
	 */
	private int nextTableId = 1;
	
	/**
	 * Counter for generating unique field codes
	 */
	private int nextFieldId = 1;
	
	/**
	 * Indicates whether the expected result is single field and is direct return type (not a bean and sub property)
	 */
	private boolean isSingleFieldReturn;
	
	public ConditionQueryBuilder(EntityDetails entityDetails)
	{
		this.entityDetails = entityDetails;
		codeToTable.put(DEF_TABLE_CODE, new TableInfo(DEF_TABLE_CODE, entityDetails.getTableName(), entityDetails, null, null, null));
	}
	
	/**
	 * Generates new unique table code
	 * @return
	 */
	private String nextTableCode()
	{
		return "T" + (nextTableId++);
	}
	
	/**
	 * Generates new unique field code
	 * @return
	 */
	private String nextFieldCode()
	{
		return "R" + (nextFieldId++);
	}
	
	/**
	 * Creates new table info with specified details and adds it to {@link #codeToTable} and {@link #propToTable} maps.
	 * @param tableCode
	 * @param entityDetails
	 * @param joinTableCode
	 * @param joinTableColumn
	 * @param targetColumn
	 * @param property
	 * @return
	 */
	private TableInfo newTableInfo(EntityDetails entityDetails, String tableName, String joinTableCode, String joinTableColumn, String targetColumn, String property)
	{
		TableInfo newTableInfo = new TableInfo(nextTableCode(), tableName, entityDetails, joinTableCode, joinTableColumn, targetColumn);
		
		//add new table info to maps
		
		//property will be null for join tables
		if(property != null)
		{
			propToTable.put(property, newTableInfo);
		}
		
		codeToTable.put(newTableInfo.tableCode, newTableInfo);
		
		return newTableInfo;
	}

	private TableInfo getTableInfo(EntityDetails entityDetails, String entityFieldPath[], String paramType, 
			String conditionExpr, String methodDesc, ObjectWrapper<FieldDetails> fieldDetailsWrapper)
	{
		EntityDetails currentEntityDetails = this.entityDetails, targetEntityDetails = null;
		String currentProp = null;
		TableInfo currentTableInfo = codeToTable.get(DEF_TABLE_CODE), newTableInfo = null, joinTableInfo = null;
		FieldDetails fieldDetails = null, targetFieldDetails = null;
		int maxIndex = entityFieldPath.length - 1;
		ForeignConstraintDetails foreignConstraint = null, targetConstraint = null;
		JoinTableDetails joinTableDetails = null;
		
		//loop through field parts and find the required table joins
		for(int i = 0; i < entityFieldPath.length; i++)
		{
			currentProp = (currentProp != null) ? currentProp + "." + entityFieldPath[i] : entityFieldPath[i];
			fieldDetails = currentEntityDetails.getFieldDetailsByField(entityFieldPath[i]);
			
			//if invalid field details encountered
			if(fieldDetails == null)
			{
				throw new InvalidMappingException( String.format("Invalid field mapping '%1s' found in %2s parameter '%3s' of %4s", 
						currentProp, paramType, conditionExpr, methodDesc) );
			}
			
			//if end of field expression is reached
			if(i == maxIndex)
			{
				//if end field is found to be entity instead of simple property
				if(fieldDetails.isRelationField())
				{
					throw new InvalidMappingException( String.format("Non-simple field mapping '%1s' found as %2s parameter '%3s' of %4s", 
							currentProp, paramType, conditionExpr, methodDesc) );
				}
				
				fieldDetailsWrapper.setValue(fieldDetails);

				return currentTableInfo;
			}

			newTableInfo = propToTable.get(currentProp);
			
			//if table is already found for current property
			if(newTableInfo != null)
			{
				currentTableInfo = newTableInfo;
				currentEntityDetails = newTableInfo.entityDetails;
				continue;
			}
			
			if(!fieldDetails.isRelationField())
			{
				throw new InvalidMappingException( String.format("Non-relational field mapping '%1s' found in %2 parameter '%3s' of %4s", 
						currentProp, paramType, conditionExpr, methodDesc) );
			}
			
			foreignConstraint = fieldDetails.getForeignConstraintDetails();
			targetEntityDetails = foreignConstraint.getTargetEntityDetails();
			
			//if this is mapped relation
			if(foreignConstraint.isMappedRelation())
			{
				targetFieldDetails = targetEntityDetails.getFieldDetailsByField(foreignConstraint.getMappedBy());
				targetConstraint = targetFieldDetails.getForeignConstraintDetails();
				joinTableDetails = targetConstraint.getJoinTableDetails();
				
				//if there is no join table in between
				if(joinTableDetails == null)
				{
					//add target table info
					newTableInfo = newTableInfo(targetEntityDetails, targetEntityDetails.getTableName(), currentTableInfo.tableCode, 
								currentEntityDetails.getIdField().getColumn(), targetFieldDetails.getColumn(), currentProp);
				}
				//if table was joined via join talbe
				else
				{
					//add join table info
					joinTableInfo = newTableInfo(null, joinTableDetails.getTableName(), currentTableInfo.tableCode, 
							currentEntityDetails.getIdField().getColumn(), joinTableDetails.getInverseJoinColumn(), null);
					
					//add target table info
					newTableInfo = newTableInfo(targetEntityDetails, targetEntityDetails.getTableName(), joinTableInfo.tableCode, 
							joinTableDetails.getJoinColumn(), targetEntityDetails.getIdField().getColumn(), currentProp);
				}
			}
			//if the relation is via join table
			else if(foreignConstraint.getJoinTableDetails() != null)
			{
				joinTableDetails = foreignConstraint.getJoinTableDetails();

				//add join table info
				joinTableInfo = newTableInfo(null, joinTableDetails.getTableName(), currentTableInfo.tableCode, 
						currentEntityDetails.getIdField().getColumn(), joinTableDetails.getJoinColumn(), null);
				
				//add target table info
				newTableInfo = newTableInfo(targetEntityDetails, targetEntityDetails.getTableName(), joinTableInfo.tableCode, 
						joinTableDetails.getInverseJoinColumn(), targetEntityDetails.getIdField().getColumn(), currentProp);
			}
			//if the relation is simple relation
			else
			{
				newTableInfo = newTableInfo(targetEntityDetails, targetEntityDetails.getTableName(), currentTableInfo.tableCode, 
						fieldDetails.getColumn(), targetEntityDetails.getIdField().getColumn(), currentProp);
			}
			
			currentTableInfo = newTableInfo;
			currentEntityDetails = targetEntityDetails;
		}
		
		//only to satisfy compiler
		return null;
	}
	
	/**
	 * Adds the condition with specified details to this builder. This method evaluates the required table joins if this condition
	 * needs to be included in the final query
	 * @param operator
	 * @param index
	 * @param embeddedProperty
	 * @param entityFieldExpression
	 * @param methodDesc
	 */
	public void addCondition(Operator operator, int index, String embeddedProperty, String entityFieldExpression, String methodDesc)
	{
		//split the entity field expression
		String entityFieldParts[] = entityFieldExpression.trim().split("\\s*\\.\\s*");

		Condition condition = new Condition(operator, index, embeddedProperty, entityFieldExpression);
		
		//if this mapping is for direct property mapping
		if(entityFieldParts.length == 1)
		{
			condition.fieldDetails = entityDetails.getFieldDetailsByField(entityFieldParts[0]);
			
			//if the field mapping is wrong
			if(condition.fieldDetails == null)
			{
				throw new InvalidMappingException( String.format("Invalid field mapping '%1' found in condition parameter '%2' of '%3'", 
							entityFieldExpression, condition.getConditionExpression(), methodDesc) );
			}
			
			condition.table = codeToTable.get(DEF_TABLE_CODE);
			conditions.add(condition);
			return;
		}
		
		//if the mapping is for nested entity field (with foreign key relationships)
		ObjectWrapper<FieldDetails> fieldDetailsHolder = new ObjectWrapper<>();
		TableInfo tableInfo = getTableInfo(entityDetails, entityFieldParts, "condition", condition.getConditionExpression(), methodDesc, fieldDetailsHolder);
		condition.table = tableInfo;
		condition.fieldDetails = fieldDetailsHolder.getValue();

		conditions.add(condition);
	}

	/**
	 * Adds a result field of the query to this builder
	 * @param resultProperty
	 * @param entityFieldExpression
	 * @param methodDesc
	 */
	public void addResultField(String resultProperty, Class<?> resultPropertyType, String entityFieldExpression, String methodDesc)
	{
		//if a field is already added as direct return value
		if(isSingleFieldReturn)
		{
			throw new InvalidMappingException("Encountered second return field addition after setting direct return field");
		}
		
		//if subproperty field is already added and later if direct return is getting added
		if(!this.resultFields.isEmpty() && resultProperty == null)
		{
			throw new InvalidMappingException("Encountered addition direct-return field (with null property) after adding a subproperty return field");
		}
		
		//split the entity field expression
		String entityFieldParts[] = entityFieldExpression.trim().split("\\s*\\.\\s*");

		ResultField resultField = new ResultField(resultProperty, nextFieldCode(), resultPropertyType);
		
		//if this mapping is for direct property mapping
		if(entityFieldParts.length == 1)
		{
			resultField.fieldDetails = entityDetails.getFieldDetailsByField(entityFieldParts[0]);
			
			//if the field mapping is wrong
			if(resultField.fieldDetails == null)
			{
				throw new InvalidMappingException( String.format("Invalid field mapping '%1' found in result parameter '%2' of '%3'", 
							entityFieldExpression, entityFieldExpression, methodDesc) );
			}
			
			resultField.table = codeToTable.get(DEF_TABLE_CODE);
			resultFields.add(resultField);

			if(resultProperty == null)
			{
				isSingleFieldReturn = true;
			}
			
			return;
		}
		
		//if the mapping is for nested entity field (with foreign key relationships)
		ObjectWrapper<FieldDetails> fieldDetailsHolder = new ObjectWrapper<>();
		TableInfo tableInfo = getTableInfo(entityDetails, entityFieldParts, "condition", entityFieldExpression, methodDesc, fieldDetailsHolder);
		resultField.table = tableInfo;
		resultField.fieldDetails = fieldDetailsHolder.getValue();

		resultFields.add(resultField);

		if(resultProperty == null)
		{
			isSingleFieldReturn = true;
		}
	}
	
	/**
	 * Adds specified table and its dependency tables to the specified conditional query
	 * @param query
	 * @param tableCode
	 * @param includedTables
	 */
	private void addTables(IConditionalQuery query, String tableCode, Set<String> includedTables)
	{
		String currentCode = tableCode;
		TableInfo tableInfo = null;
		
		while(currentCode != null)
		{
			//if current table is already added to the query
			if(includedTables.contains(currentCode))
			{
				return;
			}
			
			tableInfo = codeToTable.get(currentCode);
			
			query.addTable(new QueryTable(tableInfo.tableName, currentCode));
			includedTables.add(currentCode);
			
			if(tableInfo.joinTableCode != null)
			{
				query.addJoinCondition(new QueryJoinCondition(tableInfo.tableCode, tableInfo.column, tableInfo.joinTableCode, tableInfo.joinTableColumn));
			}
			
			currentCode = tableInfo.joinTableCode;
		}
	}
	
	/**
	 * Loads the conditions, tables and fields to the specified conditional query using specified
	 * params
	 * @param query
	 * @param params
	 */
	public void loadConditionalQuery(IConditionalQuery query, Object params[])
	{
		ParameterContext context = new ParameterContext(params);
		Set<String> includedTables = new HashSet<>();
		Object value = null;
		
		//load the result fields to specified query
		for(ResultField field : this.resultFields)
		{
			//add tables and fields to specifies query
			addTables(query, field.table.tableCode, includedTables);
			query.addResultField(new QueryResultField(field.table.tableCode, field.fieldDetails.getColumn(), field.code));
		}
		
		//load the conditions to the query
		for(Condition condition : this.conditions)
		{
			//fetch the value for current condition
			try
			{
				value = PropertyUtils.getProperty(context, condition.getConditionExpression());
			}catch(Exception ex)
			{
				throw new IllegalStateException("An error occurred while fetching condition value for expression -" + condition.getConditionExpression(), ex);
			}
			
			//if value is not provided, ignore current condition
			if(value == null)
			{
				continue;
			}
			
			//add tables and conditions to specifies query
			addTables(query, condition.table.tableCode, includedTables);
			query.addCondition(new QueryCondition(condition.table.tableCode, condition.fieldDetails.getColumn(), condition.operator, value));
		}
	}
	
	/**
	 * Parses and converts specified record into specified result type
	 * @param record
	 * @param resultType
	 * @param conversionService
	 * @param persistenceExecutionContext
	 * @return
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <T> T parseResult(Record record, Class<T> resultType, ConversionService conversionService, PersistenceExecutionContext persistenceExecutionContext) throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException
	{
		if(isSingleFieldReturn)
		{
			ResultField resField = this.resultFields.get(0);
			Object res = conversionService.convertToJavaType(record.getObject(resField.code), resField.fieldDetails);
			
			return (T)ConvertUtils.convert(res, resField.fieldType);
		}
		
		T result = resultType.newInstance();
		Object value = null;
		ProxyEntityCreator proxyEntityCreator = null;
		ForeignConstraintDetails foreignConstraint = null;
		EntityDetails foreignEntityDetails = null;
		
		RepositoryFactory repositoryFactory = persistenceExecutionContext.getRepositoryFactory();
		
		for(ResultField resultField : this.resultFields)
		{
			value = record.getObject(resultField.code);
			
			//as only table owned properties are maintained under returnColumnToField
			//		this would be parent (target entity) that needs to be loaded
			if(resultField.fieldDetails.isRelationField())
			{
				foreignConstraint = resultField.fieldDetails.getForeignConstraintDetails();
				foreignEntityDetails = foreignConstraint.getTargetEntityDetails();
				
				//TODO: Take care of mapped/unmapped collection/non-collection relationships
				
				proxyEntityCreator = new ProxyEntityCreator(foreignEntityDetails, 
						repositoryFactory.getRepositoryForEntity((Class)foreignEntityDetails.getEntityType()), value);
				value = proxyEntityCreator.getProxyEntity();
			}
			//if current field is a simple field (non relation field)
			else
			{
				value = conversionService.convertToJavaType(value, resultField.fieldDetails);
				value = ConvertUtils.convert(value, resultField.fieldType);
			}
		
			if(value == null)
			{
				continue;
			}
			
			PropertyUtils.setProperty(result, resultField.property, value);
		}
		
		return result;
	}
	
	/**
	 * Converts specified records into specified return type beans
	 * @param records
	 * @param returnType
	 * @param resultCollection
	 * @param conversionService
	 * @param persistenceExecutionContext
	 */
	public <T> void parseResults(List<Record> records, Class<T> returnType, Collection<T> resultCollection, 
			ConversionService conversionService, PersistenceExecutionContext persistenceExecutionContext)
	{
		for(Record record : records)
		{
			try
			{
				resultCollection.add(parseResult(record, returnType, conversionService, persistenceExecutionContext));
			}catch(Exception ex)
			{
				throw new IllegalArgumentException("An error occurred while parsing record - " + record, ex);
			}
		}
	}
}
