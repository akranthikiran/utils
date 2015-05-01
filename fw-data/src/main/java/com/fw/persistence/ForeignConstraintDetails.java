package com.fw.persistence;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fw.persistence.annotations.DeleteCascade;

public class ForeignConstraintDetails
{
	private String name;
	private Map<String, String> fields = new HashMap<>();
	
	//child table field to expected value
	private Map<String, Object> conditions;
	//child table column to expected value
	private Map<String, Object> columnConditions;
	
	//parent table field to expected value
	private Map<String, Object> parentConditions;
	//parent table column to expected value
	private Map<String, Object> parentColumnConditions;
	
	
	private String message;
	private EntityDetails foreignEntity;
	private EntityDetails entity;
	private boolean validate;
	private DeleteCascade deleteCascade;

	public ForeignConstraintDetails(String name, Map<String, String> fields, Map<String, Object> conditions, Map<String, Object> parentConditions, 
			EntityDetails foreignEntity, EntityDetails entity, String message, boolean validate, DeleteCascade deleteCascade)
	{
		if(name == null || name.trim().length() == 0)
		{
			throw new NullPointerException("Name can not be null or empty");
		}
		
		if(fields == null || fields.isEmpty())
		{
			throw new NullPointerException("Fields can not be null or empty");
		}

		if(foreignEntity == null)
		{
			throw new NullPointerException("Foreign-entity can not be null");
		}
		
		if(entity == null)
		{
			throw new NullPointerException("Entity can not be null");
		}
		
		if(deleteCascade == null)
		{
			throw new NullPointerException("Delete cascade can not be null");
		}

		this.name = name;
		this.message = (message == null || message.trim().length() == 0) ? null : message.trim();
		this.foreignEntity = foreignEntity;
		this.validate = validate;
		this.conditions = conditions;
		this.parentConditions = parentConditions;
		this.deleteCascade = deleteCascade;
		this.entity = entity;
		this.fields.putAll(fields);
	}
	
	public Map<String, String> getFields()
	{
		return Collections.unmodifiableMap(fields);
	}

	public String getMessage()
	{
		return message;
	}

	void setMessage(String message)
	{
		this.message = message;
	}

	public String getName()
	{
		return name;
	}

	public EntityDetails getForeignEntity()
	{
		return foreignEntity;
	}
	
	public boolean isValidate()
	{
		return validate;
	}

	
	public Map<String, Object> getChildColumnConditions()
	{
		if(this.columnConditions != null)
		{
			return this.columnConditions;
		}
		
		//building column based map dynamically, will make sure right column names are used
		//		note that, when tables needs to be created, the initial column names will be same as field names
		//		later it will be replaced with correct column name
		synchronized(this)
		{
			if(conditions == null || conditions.isEmpty())
			{
				this.columnConditions = Collections.emptyMap();
				return columnConditions;
			}
			
			Map<String, Object> columnConditions = new HashMap<>();
			
			for(String field: this.conditions.keySet())
			{
				columnConditions.put(entity.getFieldDetailsByField(field).getColumn(), this.conditions.get(field));
			}
			
			return (this.columnConditions = columnConditions);
		}
	}
	
	public boolean hasConditions()
	{
		return (conditions != null && !conditions.isEmpty());
	}

	public Map<String, Object> getParentColumnConditions()
	{
		if(this.parentColumnConditions != null)
		{
			return this.parentColumnConditions;
		}
		
		//building column based map dynamically, will make sure right column names are used
		//		note that, when tables needs to be created, the initial column names will be same as field names
		//		later it will be replaced with correct column name
		synchronized(this)
		{
			if(parentConditions == null || parentConditions.isEmpty())
			{
				this.parentColumnConditions = Collections.emptyMap();
				return parentColumnConditions;
			}
			
			Map<String, Object> columnConditions = new HashMap<>();
			
			for(String field: this.parentConditions.keySet())
			{
				columnConditions.put(foreignEntity.getFieldDetailsByField(field).getColumn(), this.parentConditions.get(field));
			}
			
			return (this.parentColumnConditions = columnConditions);
		}
	}
	
	public boolean hasParentConditions()
	{
		return (parentConditions != null && !parentConditions.isEmpty());
	}
	
	public DeleteCascade getDeleteCascade()
	{
		return deleteCascade;
	}
	
	public EntityDetails getEntityDetails()
	{
		return entity;
	}
	
	public boolean hasAnyConditions()
	{
		if(conditions != null && !conditions.isEmpty())
		{
			return true;
		}
		
		if(parentConditions != null && !parentConditions.isEmpty())
		{
			return true;
		}
		
		return false;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder(super.toString());
		builder.append("[");

		builder.append("Name: ").append(name);
		builder.append(",").append("Parent Entity: ").append(foreignEntity);
		builder.append(",").append("Field Map: ").append(fields);
		
		if(conditions != null && !conditions.isEmpty())
		{
			builder.append(",").append("Conditions: ").append(conditions);
		}

		if(parentConditions != null && !parentConditions.isEmpty())
		{
			builder.append(",").append("Parent-Conditions: ").append(parentConditions);
		}

		builder.append("]");
		return builder.toString();
	}
}
