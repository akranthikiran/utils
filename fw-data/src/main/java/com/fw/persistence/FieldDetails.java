package com.fw.persistence;

import java.lang.reflect.Field;

import javax.persistence.GenerationType;

import com.fw.persistence.annotations.DataType;

public class FieldDetails
{
	public static final int FLAG_ID = 1;
	public static final int FLAG_AUTO_FETCH = 4;
	
	private Field field;
	private String column;
	private DataType dbDataType;
	private int flags;
	private GenerationType generationType;
	private String sequenceName;
	
	private String overriddenColumnName;
	
	/**
	 * Foreign constraint on this field, if specified
	 */
	private ForeignConstraintDetails foreignConstraintDetails;
	
	private FieldDetails(FieldDetails details)
	{
		this.field = details.field;
		this.column = details.column;
		this.dbDataType = details.dbDataType;
		this.overriddenColumnName = details.overriddenColumnName;
	}
	
	public FieldDetails(Field field, String column, DataType dbDataType)
	{
		if(field == null)
		{
			throw new NullPointerException("Field can not be null");
		}
		
		if(column == null || column.trim().length() == 0)
		{
			throw new NullPointerException("Column can not be null or empty");
		}
		
		this.field = field;
		this.column = column;
		this.dbDataType = dbDataType;

		if(!field.isAccessible())
		{
			field.setAccessible(true);
		}
	}

	public FieldDetails(Field field, String column, DataType dbDataType, boolean idField, GenerationType generationType, boolean autoFetch, String sequenceName)
	{
		this(field, column, dbDataType);
		
		if(generationType == GenerationType.SEQUENCE && (sequenceName == null || sequenceName.trim().length() == 0))
		{
			throw new NullPointerException("For sequence generation type, sequence name is mandatory");
		}
		
		this.flags = idField ? (this.flags | FLAG_ID) : flags;
		this.flags = autoFetch ? (this.flags | FLAG_AUTO_FETCH) : flags;
		
		this.generationType = generationType;
		this.sequenceName = sequenceName;
	}
	
	public String getName()
	{
		return field.getName();
	}
	
	public Field getField()
	{
		return field;
	}
	
	void setColumn(String column)
	{
		this.column = column;
	}

	public String getColumn()
	{
		return column;
	}
	
	public DataType getDbDataType()
	{
		return dbDataType;
	}

	public boolean isIdField()
	{
		return ((flags & FLAG_ID) == FLAG_ID);
	}
	
	public boolean isAutoFetch()
	{
		return ((flags & FLAG_AUTO_FETCH) == FLAG_AUTO_FETCH);
	}

	public GenerationType getGenerationType()
	{
		return generationType;
	}
	
	public String getSequenceName()
	{
		return sequenceName;
	}
	
	public Object getValue(Object bean)
	{
		try
		{
			return field.get(bean);
		}catch(Exception ex)
		{
			throw new IllegalStateException("Failed to fetch value from field: " + field.getName(), ex);
		}
	}

	public void setValue(Object bean, Object value)
	{
		try
		{
			field.set(bean, value);
		}catch(Exception ex)
		{
			throw new IllegalStateException("Failed to setting value from field: " + field.getName(), ex);
		}
	}
	
	public String getOverriddenColumnName()
	{
		return overriddenColumnName;
	}

	public void setOverriddenColumnName(String overriddenColumnName)
	{
		this.overriddenColumnName = overriddenColumnName;
	}
	
	public FieldDetails cloneForAudit()
	{
		return new FieldDetails(this);
	}
	
	

	/**
	 * @return the {@link #foreignConstraintDetails foreignConstraintDetails}
	 */
	public ForeignConstraintDetails getForeignConstraintDetails()
	{
		return foreignConstraintDetails;
	}

	/**
	 * @param foreignConstraintDetails the {@link #foreignConstraintDetails foreignConstraintDetails} to set
	 */
	public void setForeignConstraintDetails(ForeignConstraintDetails foreignConstraintDetails)
	{
		this.foreignConstraintDetails = foreignConstraintDetails;
	}
	
	/**
	 * Returns true, if this field indicates a relation to other entity
	 * @return
	 */
	public boolean isRelationField()
	{
		return (foreignConstraintDetails != null);
	}
	
	/**
	 * Returns true, if this field is relation field but relation is not owned by current entity
	 * @return
	 */
	public boolean isMappedRelationField()
	{
		return (foreignConstraintDetails != null && foreignConstraintDetails.isMappedRelation());
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder(super.toString());
		builder.append("[");

		builder.append("Field: ").append(field);
		builder.append(",").append("Column: ").append(column);
		builder.append(",").append("ID Field: ").append(isIdField());

		builder.append("]");
		return builder.toString();
	}
}
