package com.fw.persistence.query.data;

import java.util.Map;

import com.fw.persistence.EntityDetails;
import com.fw.persistence.FieldDetails;
import com.fw.persistence.ForeignConstraintDetails;

public class ForeignConstraintStructure
{
	public static final String FOREIGN_CONSTRAINT_PREFIX = "FK_";
	
	private String name;
	private String columns[];
	private String parentTable;
	private String parentColumns[];
	
	public ForeignConstraintStructure(EntityDetails entityDetails, ForeignConstraintDetails constraint, Map<String, String> fieldMapping)
	{
		this.name = FOREIGN_CONSTRAINT_PREFIX + entityDetails.getEntityType().getSimpleName().toUpperCase() + "_" + constraint.getName().toUpperCase();
		this.parentTable = constraint.getForeignEntity().getTableName();
		
		Map<String, String> fieldMap = constraint.getFields();
		FieldDetails fieldDetails = null, parentFieldDetails = null;
		EntityDetails parentEntityDetails = constraint.getForeignEntity();
		
		this.columns = new String[fieldMap.size()];
		this.parentColumns = new String[fieldMap.size()];
		
		int idx = 0;
		
		for(String field: fieldMap.keySet())
		{
			fieldDetails = entityDetails.getFieldDetailsByField(field);
			parentFieldDetails = parentEntityDetails.getFieldDetailsByField(fieldMap.get(field));
			
			this.columns[idx] = fieldMapping.get(fieldDetails.getColumn());
			
			//parent field details would already have got mapped, so we dont need external mapping
			this.parentColumns[idx] = parentFieldDetails.getColumn();
		}
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String[] getColumns()
	{
		return columns;
	}

	public void setColumns(String[] columns)
	{
		this.columns = columns;
	}

	public String getParentTable()
	{
		return parentTable;
	}

	public void setParentTable(String parentTable)
	{
		this.parentTable = parentTable;
	}

	public String[] getParentColumns()
	{
		return parentColumns;
	}

	public void setParentColumns(String[] parentColumns)
	{
		this.parentColumns = parentColumns;
	}

}
