package com.fw.persistence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EntityDetails
{
	private String tableName;
	private Class<?> entityType;

	private Map<String, FieldDetails> fieldToDetails = new HashMap<>();
	private Map<String, FieldDetails> columnToDetails = new HashMap<>();

	private List<UniqueConstraintDetails> uniqueConstraints = new ArrayList<>();
	private Set<String> uniqueConstraintFields = new HashSet<>();

	private List<ForeignConstraintDetails> foreignConstraints = new ArrayList<>();

	private List<ForeignConstraintDetails> childConstraints = new ArrayList<>();

	private List<IndexDetails> indexDetailsList = new ArrayList<>();

	private FieldDetails idField;

	public EntityDetails(String tableName, Class<?> entityType)
	{
		if(tableName == null || tableName.trim().length() == 0)
		{
			throw new NullPointerException("Table-name can not be null or empty");
		}

		if(entityType == null)
		{
			throw new NullPointerException("Entity-type can not be null");
		}

		this.tableName = tableName;
		this.entityType = entityType;
	}

	void resetColumnMapping(Map<String, String> fieldMapping)
	{
		columnToDetails.clear();

		FieldDetails fieldDetails = null;
		String column = null;

		for(String name : fieldToDetails.keySet())
		{
			fieldDetails = fieldToDetails.get(name);
			column = fieldMapping.get(name);

			if(column == null)
			{
				throw new IllegalArgumentException("Field Mapping for field: " + name);
			}

			fieldDetails.setColumn(column);
			columnToDetails.put(column, fieldDetails);
		}
	}

	public String getTableName()
	{
		return tableName;
	}

	public Class<?> getEntityType()
	{
		return entityType;
	}

	void addFieldDetails(FieldDetails fieldDetails)
	{
		if(fieldToDetails.containsKey(fieldDetails.getField()))
		{
			throw new InvalidMappingException("Field '" + fieldDetails.getField() + "' is mapped multiple times");
		}

		if(columnToDetails.containsKey(fieldDetails.getColumn()))
		{
			throw new InvalidMappingException("Column '" + fieldDetails.getColumn() + "' is mapped by multiple field: [" + fieldDetails.getField() + ", " + columnToDetails.get(fieldDetails.getColumn()).getField() + "]");
		}

		fieldToDetails.put(fieldDetails.getField().getName(), fieldDetails);
		columnToDetails.put(fieldDetails.getColumn(), fieldDetails);

		if(fieldDetails.isIdField())
		{
			if(this.idField != null)
			{
				throw new InvalidMappingException("Multiple id fields are defined for entity: " + entityType.getName());
			}

			this.idField = fieldDetails;
		}
	}

	public FieldDetails getFieldDetailsByField(String field)
	{
		return fieldToDetails.get(field);
	}

	public FieldDetails getFieldDetailsByColumn(String column)
	{
		return columnToDetails.get(column);
	}

	public Collection<FieldDetails> getFieldDetails()
	{
		return fieldToDetails.values();
	}

	public boolean hasField(String name)
	{
		return fieldToDetails.containsKey(name);
	}

	void addUniqueKeyConstraint(UniqueConstraintDetails uniqueConstraintDetails)
	{
		if(uniqueConstraintDetails == null)
		{
			throw new NullPointerException("Unique constraint details can not be null");
		}

		if(uniqueConstraintFields.contains(uniqueConstraintDetails.getFieldsString()))
		{
			throw new InvalidMappingException("Multile unique constraints defined on same fields: " + uniqueConstraintDetails.getFieldsString());
		}

		this.uniqueConstraints.add(uniqueConstraintDetails);
		this.uniqueConstraintFields.add(uniqueConstraintDetails.getFieldsString());
	}

	public Collection<UniqueConstraintDetails> getUniqueConstraints()
	{
		return Collections.unmodifiableCollection(uniqueConstraints);
	}

	void addForeignConstraintDetails(ForeignConstraintDetails foreignConstraintDetails)
	{
		if(foreignConstraintDetails == null)
		{
			throw new NullPointerException("Unique constraint details can not be null");
		}

		this.foreignConstraints.add(foreignConstraintDetails);
	}

	public Collection<ForeignConstraintDetails> getForeignConstraints()
	{
		return Collections.unmodifiableCollection(foreignConstraints);
	}

	public FieldDetails getIdField()
	{
		return idField;
	}

	public boolean hasIdField()
	{
		return (idField != null);
	}

	/**
	 * Gets the unique constraints which involves specified field
	 * @param field
	 * @return
	 */
	public List<UniqueConstraintDetails> getUniqueConstraints(String field)
	{
		List<UniqueConstraintDetails> constraints = null;

		for(UniqueConstraintDetails constraint : this.uniqueConstraints)
		{
			if(!constraint.hasField(field))
			{
				continue;
			}

			if(constraints == null)
			{
				constraints = new ArrayList<>();
			}

			constraints.add(constraint);
		}

		return constraints;
	}

	/** 
	 * Adds value to {@link #childConstraints Child Constraints}
	 *
	 * @param childConstraint childConstraint to be added
	 */
	public void addChildConstraint(ForeignConstraintDetails childConstraint)
	{
		//if specified constraint is owned by current entity, ignore child end relation
		if(childConstraint.isMappedRelation())
		{
			return;
		}
		
		if(childConstraints == null)
		{
			childConstraints = new ArrayList<ForeignConstraintDetails>();
		}

		childConstraints.add(childConstraint);
	}

	public List<ForeignConstraintDetails> getChildConstraints()
	{
		return Collections.unmodifiableList(childConstraints);
	}

	/** 
	 * Adds value to {@link #indexDetailsList Index Details List}
	 *
	 * @param indexDetails indexDetails to be added
	 */
	public void addIndexDetails(IndexDetails indexDetails)
	{
		for(IndexDetails oldIndex : this.indexDetailsList)
		{
			if(oldIndex.getName().equals(indexDetails.getName()))
			{
				throw new InvalidConfigurationException("Duplicate index name encountered - " + indexDetails.getName());
			}

			if(Arrays.equals(oldIndex.getFields(), indexDetails.getFields()))
			{
				throw new InvalidConfigurationException("Multiple indexes are defined on same fields: " + Arrays.asList(indexDetails.getFields()));
			}
		}

		indexDetailsList.add(indexDetails);
	}

	public List<IndexDetails> getIndexDetailsList()
	{
		return indexDetailsList;
	}

	public Set<String> getColumns()
	{
		return Collections.unmodifiableSet(columnToDetails.keySet());
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder(super.toString());
		builder.append("[");

		builder.append("Table: ").append(tableName);
		builder.append(",").append("Type: ").append(entityType);

		builder.append("]");
		return builder.toString();
	}

	public EntityDetails cloneForAudit(String tableName)
	{
		EntityDetails newEntityDetails = new EntityDetails(tableName, entityType);

		for(FieldDetails fieldDetails: this.fieldToDetails.values())
		{
			fieldDetails = fieldDetails.cloneForAudit();
			
			newEntityDetails.fieldToDetails.put(fieldDetails.getField().getName(), fieldDetails);
			newEntityDetails.columnToDetails.put(fieldDetails.getColumn(), fieldDetails);
		}

		return newEntityDetails;
	}
}
