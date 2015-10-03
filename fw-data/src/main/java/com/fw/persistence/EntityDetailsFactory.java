package com.fw.persistence;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fw.persistence.annotations.AccessType;
import com.fw.persistence.annotations.AutoFetchType;
import com.fw.persistence.annotations.DataType;
import com.fw.persistence.annotations.DataTypeMapping;
import com.fw.persistence.annotations.FieldAccess;
import com.fw.persistence.annotations.ForeignConstraint;
import com.fw.persistence.annotations.ForeignConstraints;
import com.fw.persistence.annotations.Index;
import com.fw.persistence.annotations.Indexed;
import com.fw.persistence.annotations.Indexes;
import com.fw.persistence.annotations.Mapping;
import com.fw.persistence.annotations.MappingCondition;
import com.fw.persistence.annotations.UniqueConstraint;
import com.fw.persistence.annotations.UniqueConstraints;
import com.fw.persistence.conversion.ConversionService;
import com.fw.persistence.query.CreateIndexQuery;
import com.fw.persistence.query.CreateTableQuery;

public class EntityDetailsFactory
{
	private static Logger logger = LogManager.getLogger(EntityDetailsFactory.class);

	private static Map<Class<?>, EntityDetails> typeToDetails = new HashMap<>();
	private static final String SPECIAL_CHAR_PATTERN = "[\\W\\_]+";
	
	/**
	 * Removes non aplha numeric characters (including underscore) from column names and sets it as key and the actual column
	 * name as value of the resultant map. This can be used to find column mapping for undeclared columns.
	 * 
	 * @param entityType
	 * @param dataStore
	 * @return
	 */
	private static Map<String, String> flattenColumnNames(String tableName, IDataStore dataStore)
	{
		Set<String> columns = dataStore.getColumnNames(tableName);
		String flattenName = null;
		
		Map<String, String> map = new HashMap<>();
		
		for(String column: columns)
		{
			//remove all special characters including underscore
			flattenName = column.replaceAll(SPECIAL_CHAR_PATTERN, "");
			
			//convert flatten name to lower case to finalize the flattening
			map.put(flattenName.toLowerCase(), column);
		}
		
		logger.trace("Got columns for table '{}' as {}", tableName, map);
		
		return map;
	}
	
	private static void checkFieldValidity(EntityDetails entityDetails, String fields[], String constraintType, String name, String parentClass)
	{
		for(String fieldName: fields)
		{
			if(!entityDetails.hasField(fieldName))
			{
				throw new InvalidMappingException("Invalid field '" + fieldName + "' specified in @" + constraintType + " '" + name + "' in class - " + parentClass);
			}
		}
	}
	
	private static void buildUniqueConstraint(UniqueConstraint uniqueConstraint, EntityDetails entityDetails, Field field)
	{
		if(field == null && uniqueConstraint.fields().length == 0)
		{
			throw new InvalidMappingException("No fields are defined in @UniqueConstraint '" + uniqueConstraint.name() 
					+ "' specified at class level in class: " + entityDetails.getEntityType().getName());
		}
		
		String fields[] = uniqueConstraint.fields();
		
		if(field != null)
		{
			fields = Arrays.copyOf(fields, fields.length + 1);
			fields[fields.length - 1] = field.getName();
		}
		
		checkFieldValidity(entityDetails, fields, UniqueConstraint.class.getSimpleName(), uniqueConstraint.name(), entityDetails.getEntityType().getName());
		
		UniqueConstraintDetails constraint = new UniqueConstraintDetails(uniqueConstraint.name(), fields, uniqueConstraint.message(), uniqueConstraint.validate());
		entityDetails.addUniqueKeyConstraint(constraint);
		
		logger.trace("Added unique-constraint {} to entity: {}", uniqueConstraint, entityDetails);
	}
	
	private static void buildForeignConstraint(ForeignConstraint foreignConstraint, EntityDetails entityDetails, 
					IDataStore dataStore, boolean createTables)
	{
		ConversionService conversionService = dataStore.getConversionService();
		String name = foreignConstraint.name();
		
		//fetch foreign fields and validate them
		EntityDetails foreignEntityDetails = null;
		
		try
		{
			foreignEntityDetails = getEntityDetails(foreignConstraint.foreignEntity(), dataStore, createTables);
		}catch(Exception ex)
		{
			throw new InvalidMappingException("An error occurred while fetching foreign entity '" 
					+ foreignConstraint.foreignEntity().getName() + "' details for  @ForeignConstraint '" + foreignConstraint.name() + "'", ex);
		}

		//fetch mappings and validate them
		Mapping mappings[] = foreignConstraint.mappings();
		Map<String, String> fieldMap = new HashMap<>();
		
		for(Mapping mapping: mappings)
		{
			if(!entityDetails.hasField(mapping.from()))
			{
				throw new InvalidMappingException("Invalid field '" + mapping.from() + "' specified in @ForeignConstraint '" + name 
						+ "' in class - " + entityDetails.getEntityType().getName());
			}
			
			if(!foreignEntityDetails.hasField(mapping.to()))
			{
				throw new InvalidMappingException("Invalid parent field '" + mapping.to() + "' specified in @ForeignConstraint '" + name 
						+ "' in class - " + entityDetails.getEntityType().getName());
			}
			
			fieldMap.put(mapping.from(), mapping.to());
		}
		
		//fetch conditions
		MappingCondition conditions[] = foreignConstraint.conditions();
		Map<String, Object> conditionMap = new HashMap<>();
		FieldDetails fieldDetails = null;
		
		for(MappingCondition condition: conditions)
		{
			fieldDetails = entityDetails.getFieldDetailsByField(condition.field());
			
			if(fieldDetails == null)
			{
				throw new InvalidMappingException("Invalid field '" + condition.field() + "' specified for condition in @ForeignConstraint '" + name 
						+ "' in class - " + entityDetails.getEntityType().getName());
			}
			
			conditionMap.put(fieldDetails.getName(), conversionService.convertToDBType(condition.value(), fieldDetails));
		}
		
		//fetch parent conditions
		conditions = foreignConstraint.parentConditions();
		Map<String, Object> parentConditionMap = new HashMap<>();
		
		for(MappingCondition condition: conditions)
		{
			fieldDetails = foreignEntityDetails.getFieldDetailsByField(condition.field());
			
			if(fieldDetails == null)
			{
				throw new InvalidMappingException("Invalid field '" + condition.field() + "' specified for parent-condition in @ForeignConstraint '" + name 
						+ "' in class - " + entityDetails.getEntityType().getName());
			}
			
			parentConditionMap.put(fieldDetails.getName(), conversionService.convertToDBType(condition.value(), fieldDetails));
		}
		
		ForeignConstraintDetails newConstraint = new ForeignConstraintDetails(foreignConstraint.name(), fieldMap, conditionMap, parentConditionMap,
					foreignEntityDetails, entityDetails, foreignConstraint.message(), foreignConstraint.validate(), foreignConstraint.deleteCascade());
		
		entityDetails.addForeignConstraintDetails(newConstraint);
		foreignEntityDetails.addChildConstraint(newConstraint);
		
		logger.trace("Added foreign-constraint {} to entity: {}", newConstraint, entityDetails);
	}
	
	private static void buildIndexDetails(EntityDetails entityDetails, String name, String... fields)
	{
		if(fields == null || fields.length == 0)
		{
			throw new InvalidConfigurationException("No/empty list of fields specified for indexing");
		}
		
		if(name == null || name.trim().length() == 0)
		{
			StringBuilder nameBuilder = new StringBuilder("IDX_").append(entityDetails.getEntityType().getSimpleName().toUpperCase());
			
			for(String field: fields)
			{
				if(!entityDetails.hasField(field))
				{
					throw new InvalidMappingException("Invalid field name '" + field + "' encountered for indexing");
				}
				
				nameBuilder.append("_").append(field.toUpperCase());
			}
			
			name = nameBuilder.toString();
		}
		
		entityDetails.addIndexDetails(new IndexDetails(name, fields));
	}
	
	public static synchronized EntityDetails getEntityDetails(Class<?> entityType, IDataStore dataStore, boolean createTables)
	{
		EntityDetails entityDetails = typeToDetails.get(entityType);
		
		if(entityDetails != null)
		{
			return entityDetails;
		}

		logger.trace("*********************************************************");
		logger.trace("Building entity details for type: " + entityType.getName());
		
		Table table = entityType.getAnnotation(Table.class);
		
		if(table == null)
		{
			throw new InvalidMappingException("No @Table annotation found on entity type: " + entityType.getName());
		}

		FieldAccess fieldAccess = entityType.getAnnotation(FieldAccess.class);
		entityDetails = new EntityDetails(table.name(), entityType);
		AccessType accessType = (fieldAccess == null) ? AccessType.ALL : fieldAccess.value(); 
		
		Class<?> cls = entityType;
		Map<String, String> flattenColumnMap = null;
		
		try
		{
			flattenColumnMap = flattenColumnNames(entityDetails.getTableName(), dataStore);
		}catch(RuntimeException ex)
		{
			if(!createTables)
			{
				logger.error("An error occurred while fetching coumns from table - " + entityDetails.getTableName(), ex);
				throw ex;
			}
			
			logger.info("An error occurred while fetching column details for table '" + entityDetails.getTableName() + "'. Assuming table does not exist and needs to be created");
		}
		
		//loop through the class hierarchy and fetch column mappings
		while(true)
		{
			if(cls.getName().startsWith("java"))
			{
				break;
			}
			
			fetchFieldMappings(cls, entityDetails, accessType, flattenColumnMap);
			cls = cls.getSuperclass();
		}

		//set entity details on map, set it before processing constraints
			// so that self linking will not cause recursion
		typeToDetails.put(entityType, entityDetails);
		
		UniqueConstraints uniqueConstraints = null;
		ForeignConstraints foreignConstraints = null;
		Indexes indexes = null;
		cls = entityType;
		
		//loop through the class hierarchy and fetch constraint details at class level
		while(true)
		{
			if(cls.getName().startsWith("java"))
			{
				break;
			}
			
			//fetch class level unique constraint details
			uniqueConstraints = cls.getAnnotation(UniqueConstraints.class);
			
			if(uniqueConstraints != null)
			{
				for(UniqueConstraint constraint: uniqueConstraints.value())
				{
					buildUniqueConstraint(constraint, entityDetails, null);
				}
			}
			
			foreignConstraints = cls.getAnnotation(ForeignConstraints.class);
			
			if(foreignConstraints != null)
			{
				for(ForeignConstraint constraint: foreignConstraints.value())
				{
					buildForeignConstraint(constraint, entityDetails, dataStore, createTables);	
				}
			}
			
			indexes = cls.getAnnotation(Indexes.class);
			
			if(indexes != null)
			{
				for(Index index: indexes.value())
				{
					buildIndexDetails(entityDetails, index.name(), index.fields());
				}
			}
			
			cls = cls.getSuperclass();
		}
		
		//fetch constraints at field level
		fetchFieldConstraints(entityDetails, dataStore);
		
		if(flattenColumnMap == null)
		{
			logger.debug("As no column mapping found, assuming table needs to be created.");
			createRequiredTable(entityDetails, dataStore);
		}

		//check if id field is specified
		if(entityDetails.getIdField() == null)
		{
			throw new InvalidMappingException("No id field is specified for entity-type: " + entityType.getName());
		}
		
		logger.trace("Completed building of entity details {}", entityDetails);
		logger.trace("*********************************************************");
		return entityDetails;
	}
	
	private static void fetchFieldMappings(Class<?> cls, EntityDetails entityDetails, AccessType accessType, Map<String, String> flattenColumnMap)
	{
		Field fields[] = cls.getDeclaredFields();
		Column column = null;
		String columnName = null;
		Indexed indexed = null;
		DataType dbType = null;
		DataTypeMapping dataTypeMapping = null;
		
		for(Field field: fields)
		{
			if(
				Modifier.isStatic(field.getModifiers())
				|| field.getAnnotation(Transient.class) != null	
			  )
			{
				continue;
			}
			
			column = field.getAnnotation(Column.class);
			dataTypeMapping = field.getAnnotation(DataTypeMapping.class);
			
			if(column == null && accessType == AccessType.DECLARED_ONLY)
			{
				continue;
			}
			
			columnName = (column != null && column.name().length() > 0) ? column.name().trim() : field.getName();
			dbType = (dataTypeMapping != null) ? dataTypeMapping.type() : DataType.UNKNOWN;
			
			//flatten the column name
			columnName = columnName.replaceAll(SPECIAL_CHAR_PATTERN, "");
			columnName = columnName.toLowerCase();
			
			//get actual column name from flatten column map
			columnName = (flattenColumnMap != null) ? flattenColumnMap.get(columnName) : null;
			
			if(columnName == null)
			{
				//if flattenColumnMap is not null, it indicates the table is already existing
				// but field is missing
				if(flattenColumnMap != null)
				{
					throw new InvalidMappingException("Failed to find column mapping for field: " + field.getName() + " in entity: " + entityDetails.getEntityType().getName());
				}
				
				columnName = field.getName();
			}
			
			buildFieldDetails(field, columnName, dbType, entityDetails);
			
			/*
			idField = field.getAnnotation(IdField.class);

			if(idField == null)
			{
				fieldDetails = new FieldDetails(field, columnName, (field.getAnnotation(ReadOnly.class) != null) );
				
				logger.trace("Adding field details {} to entity {}", fieldDetails, entityDetails);
			}
			else
			{
				String sequenceName = idField.sequenceName();
				
				if(idField.autogeneration() == AutogenerationType.SEQUENCE && (sequenceName == null || sequenceName.trim().length() == 0))
				{
					sequenceName = "SEQ_" + entityDetails.getEntityType().getSimpleName().toUpperCase() + "_" + field.getName().toUpperCase();
				}
				
				fieldDetails = new FieldDetails(field, columnName, true, idField.autogeneration(), idField.autofetch(), true, sequenceName);
				
				logger.trace("Adding ID field details {} to entity {}", fieldDetails, entityDetails);
			}
			
			entityDetails.addFieldDetails(fieldDetails);
			*/
			
			indexed = field.getAnnotation(Indexed.class);
			
			if(indexed != null)
			{
				buildIndexDetails(entityDetails, indexed.name(), field.getName());
			}
		}
	}
	
	private static FieldDetails buildFieldDetails(Field field, String columnName, DataType dataType, EntityDetails entityDetails)
	{
		FieldDetails fieldDetails = null;
		Id idField = field.getAnnotation(Id.class);
		GeneratedValue generatedValue = field.getAnnotation(GeneratedValue.class);

		if(idField == null)
		{
			fieldDetails = new FieldDetails(field, columnName, dataType);
			
			logger.trace("Adding field details {} to entity {}", fieldDetails, entityDetails);
		}
		else
		{
			AutoFetchType autoFetchAnnot = field.getAnnotation(AutoFetchType.class);
			
			String sequenceName = (generatedValue != null) ? generatedValue.generator() : null;
			GenerationType generationType = (generatedValue != null) ? generatedValue.strategy() : null;
			boolean autoFetch = (autoFetchAnnot != null) ? autoFetchAnnot.value() : true;
			
			//if invalid generation type is specified but not supported
			if(generationType != null && generationType != GenerationType.IDENTITY && generationType != GenerationType.SEQUENCE)
			{
				throw new IllegalStateException(String.format("Invalid generation-type '%s' specified for field '%s' of entity - %s", 
						generationType, field.getName(), entityDetails.getEntityType().getName()));
			}
			
			//if generation type is sequence, get sequence name
			if(generationType == GenerationType.SEQUENCE && (sequenceName == null || sequenceName.trim().length() == 0))
			{
				sequenceName = "SEQ_" + entityDetails.getEntityType().getSimpleName().toUpperCase() + "_" + field.getName().toUpperCase();
			}
			
			fieldDetails = new FieldDetails(field, columnName, dataType, true, generationType, autoFetch, sequenceName);
			
			logger.trace("Adding ID field details {} to entity {}", fieldDetails, entityDetails);
		}
		
		entityDetails.addFieldDetails(fieldDetails);
		return fieldDetails;
	}

	private static void fetchFieldConstraints(EntityDetails entityDetails, IDataStore dataStore)
	{
		Field field = null;
		UniqueConstraint uniqueConstraint = null;
		
		for(FieldDetails fieldDetails: entityDetails.getFieldDetails())
		{
			field = fieldDetails.getField();
			
			uniqueConstraint = field.getAnnotation(UniqueConstraint.class);
			
			if(uniqueConstraint != null)
			{
				buildUniqueConstraint(uniqueConstraint, entityDetails, field);
			}
		}
	}
	
	/**
	 * Creates required tables, sequences, indexes etc, required by this entity
	 * @param entityDetails
	 * @param dataStore
	 */
	private static void createRequiredTable(EntityDetails entityDetails, IDataStore dataStore)
	{
		FieldDetails idFieldDetails = entityDetails.getIdField();
		
		//check if sequence needs to be created for id field, if yes, create it
		if(idFieldDetails != null && idFieldDetails.getGenerationType() == GenerationType.SEQUENCE)
		{
			dataStore.checkAndCreateSequence(idFieldDetails.getSequenceName());
		}
		
		//create the main table for the entity type
		CreateTableQuery createTableQuery = new CreateTableQuery(entityDetails);
		dataStore.createTable(createTableQuery);

		//reset the column mapping, to take new column names (if any) into consideration
		entityDetails.resetColumnMapping(createTableQuery.getTableStructure().getFieldMapping());
		
		//create required table indexes
		String columns[] = null, fields[] = null;
		int idx = 0;
		
		//check and create required indexed
		for(IndexDetails index: entityDetails.getIndexDetailsList())
		{
			fields = index.getFields();
			columns = new String[fields.length];
			idx = 0;
			
			for(String field: fields)
			{
				columns[idx] = entityDetails.getFieldDetailsByField(field).getColumn();
				idx++;
			}
			
			dataStore.createIndex(new CreateIndexQuery(entityDetails, index.getName(), columns));
		}
	}
}
