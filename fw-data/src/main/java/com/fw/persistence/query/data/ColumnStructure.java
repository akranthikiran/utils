package com.fw.persistence.query.data;

import java.lang.reflect.Field;

import javax.persistence.Column;
import javax.persistence.GenerationType;

import com.fw.persistence.FieldDetails;
import com.fw.persistence.PersistenceException;
import com.fw.persistence.annotations.DataType;
import com.fw.persistence.annotations.DataTypeMapping;

public class ColumnStructure
{
	private String name;
	private int length = 255;
	private DataType type;
	private boolean nullable = true;
	private boolean autoIncrement;
	private boolean sequenceIncrement;
	private boolean idField;
	
	public ColumnStructure(Class<?> entityType, FieldDetails fieldDetails)
	{
		Field field = fieldDetails.getField();
		Column column = field.getAnnotation(Column.class);
		DataTypeMapping dataTypeMapping = field.getAnnotation(DataTypeMapping.class);
		
		String overriddenColumn = fieldDetails.getOverriddenColumnName();
		this.autoIncrement = (fieldDetails.isIdField() && fieldDetails.getGenerationType() == GenerationType.IDENTITY);
		this.sequenceIncrement = (fieldDetails.isIdField() && fieldDetails.getGenerationType() == GenerationType.SEQUENCE);
		this.idField = fieldDetails.isIdField();
		
		if(overriddenColumn != null && overriddenColumn.trim().length() > 0)
		{
			this.name = overriddenColumn;
		}
		else if(column == null || column.name().trim().length() == 0)
		{
			name = field.getName();
			
			name = name.replace("_", "");
			name = name.replaceAll("([A-Z])", "_$1");
			name = name.toUpperCase();
		}
		else
		{
			name = column.name().trim();
		}
		
		if(column != null)
		{
			this.length = column.length();
			this.type = (dataTypeMapping != null) ? dataTypeMapping.type() : null;
			this.nullable = column.nullable();
			
			if(this.type == DataType.UNKNOWN)
			{
				this.type = null;
			}
		}
		
		if(this.type == null || this.type == DataType.UNKNOWN)
		{
			Class<?> fieldType = field.getType();
			
			//if the field is relation field
			if(fieldDetails.isRelationField())
			{
				//get column type from target entity id field type
				this.type = DataType.getDataType(fieldDetails.getForeignConstraintDetails().getTargetEntityDetails().getIdField().getField().getType());
			}
			//if current field is normal field
			else
			{
				//get column type based on field's java type
				this.type = DataType.getDataType(fieldType);
			}
			
			if(this.type == null)
			{
				if(fieldDetails.getDbDataType() == null)
				{
					throw new PersistenceException("Unsupported data type '" + fieldType.getName() + "' encountered. [Field: " + field.getName() + ", Enttity Type: " + entityType.getName());
				}
				
				this.type = fieldDetails.getDbDataType();
			}
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

	public int getLength()
	{
		return length;
	}

	public void setLength(int length)
	{
		this.length = length;
	}

	public DataType getType()
	{
		return type;
	}

	public void setType(DataType type)
	{
		this.type = type;
	}
	
	public String getTypeName()
	{
		return type.getName();
	}

	public boolean isNullable()
	{
		return nullable;
	}

	public void setNullable(boolean nullable)
	{
		this.nullable = nullable;
	}

	public boolean isAutoIncrement()
	{
		return autoIncrement;
	}
	
	public boolean isSequenceIncrement()
	{
		return sequenceIncrement;
	}
	
	public boolean isIdField()
	{
		return idField;
	}
}
