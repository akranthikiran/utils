package com.fw.persistence.query.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fw.persistence.EntityDetails;
import com.fw.persistence.FieldDetails;
import com.fw.persistence.ForeignConstraintDetails;
import com.fw.persistence.UniqueConstraintDetails;

public class TableStructure
{
	private String tableName;
	private List<ColumnStructure> columns = new ArrayList<>();
	private List<UniqueConstraintStructure> uniqueConstraints = new ArrayList<>();
	private List<ForeignConstraintStructure> foreignConstraints = new ArrayList<>();
	
	/**
	 * Field name to column name mapping
	 */
	private Map<String, String> fieldMapping = new HashMap<>();
	
	public TableStructure(EntityDetails entityDetails)
	{
		Class<?> entityType = entityDetails.getEntityType();
		tableName = entityDetails.getTableName();

		//Load columns for the table
		ColumnStructure column = null;
		
		if(entityDetails.hasIdField())
		{
			column = new ColumnStructure(entityType, entityDetails.getIdField());
			
			columns.add(column);
			fieldMapping.put(entityDetails.getIdField().getName(), column.getName());
		}		
		
		for(FieldDetails field: entityDetails.getFieldDetails())
		{
			if(fieldMapping.containsKey(field.getName()))
			{
				continue;
			}
			
			//if the field is relation field, but not maintained within the curent table
			if(field.isMappedRelationField() || field.isTableJoined())
			{
				continue;
			}
			
			column = new ColumnStructure(entityType, field);
			
			columns.add(column);
			fieldMapping.put(field.getName(), column.getName());
		}
		
		//Load unique constraints of the table
		for(UniqueConstraintDetails constraint: entityDetails.getUniqueConstraints())
		{
			this.uniqueConstraints.add(new UniqueConstraintStructure(entityDetails, constraint, fieldMapping));
		}
		
		//Load foreign constraints of the table
		for(ForeignConstraintDetails constraint: entityDetails.getForeignConstraints())
		{
			//if this is mapping constraint and actual relation is
			//		maintained by target entity
			if(constraint.isMappedRelation() || constraint.getJoinTableDetails() != null)
			{
				//don't add this constraint as part of table structure
				continue;
			}
			
			this.foreignConstraints.add(new ForeignConstraintStructure(entityDetails, constraint, fieldMapping));
		}
	}

	public String getTableName()
	{
		return tableName;
	}

	public void setTableName(String tableName)
	{
		this.tableName = tableName;
	}

	public List<ColumnStructure> getColumns()
	{
		return columns;
	}

	public void setColumns(List<ColumnStructure> columns)
	{
		this.columns = columns;
	}

	public List<UniqueConstraintStructure> getUniqueConstraints()
	{
		return uniqueConstraints;
	}

	public void setUniqueConstraints(List<UniqueConstraintStructure> uniqueConstraints)
	{
		this.uniqueConstraints = uniqueConstraints;
	}

	public List<ForeignConstraintStructure> getForeignConstraints()
	{
		return foreignConstraints;
	}

	public void setForeignConstraints(List<ForeignConstraintStructure> foreignConstraints)
	{
		this.foreignConstraints = foreignConstraints;
	}

	public boolean isConstraintsAvailable()
	{
		return (!uniqueConstraints.isEmpty() || !foreignConstraints.isEmpty());
	}

	public boolean isForeignConstraintsAvailable()
	{
		return (!foreignConstraints.isEmpty());
	}
	
	public Map<String, String> getFieldMapping()
	{
		return fieldMapping;
	}
}
