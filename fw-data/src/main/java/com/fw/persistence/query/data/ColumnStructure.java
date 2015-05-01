package com.fw.persistence.query.data;

import java.lang.reflect.Field;

import com.fw.persistence.FieldDetails;
import com.fw.persistence.PersistenceException;
import com.fw.persistence.annotations.AutogenerationType;
import com.fw.persistence.annotations.Column;
import com.fw.persistence.annotations.DataType;

public class ColumnStructure
{
	private String name;
	private int length = Column.DEFAULT_LENGTH;
	private DataType type;
	private boolean nullable = true;
	private boolean autoIncrement;
	private boolean sequenceIncrement;
	private boolean idField;
	
	public ColumnStructure(Class<?> entityType, FieldDetails fieldDetails)
	{
		Field field = fieldDetails.getField();
		Column column = field.getAnnotation(Column.class);
		
		String overriddenColumn = fieldDetails.getOverriddenColumnName();
		this.autoIncrement = (fieldDetails.isIdField() && fieldDetails.getAutogenerationType() == AutogenerationType.AUTO);
		this.sequenceIncrement = (fieldDetails.isIdField() && fieldDetails.getAutogenerationType() == AutogenerationType.SEQUENCE);
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
			this.type = column.type();
			this.nullable = column.nullable();
			
			if(this.type == DataType.UNKNOWN)
			{
				this.type = null;
			}
		}
		
		if(this.type == null)
		{
			this.type = DataType.getDataType(field.getType());
			
			if(this.type == null)
			{
				throw new PersistenceException("Unsupported data type '" + field.getType() + "' encountered. [Field: " + field.getName() + ", Enttity Type: " + entityType.getName());
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
