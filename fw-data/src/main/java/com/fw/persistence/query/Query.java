package com.fw.persistence.query;

import java.util.Iterator;
import java.util.List;

import com.fw.persistence.EntityDetails;

public abstract class Query
{
	protected EntityDetails entityDetails;
	
	public Query(EntityDetails entityDetails)
	{
		this.entityDetails = entityDetails;
	}

	public EntityDetails getEntityDetails()
	{
		return entityDetails;
	}
	
	public String getTableName()
	{
		return entityDetails.getTableName();
	}

	public static void toString(List<QueryCondition> conditions, StringBuilder builder)
	{
		if(conditions == null || conditions.isEmpty())
		{
			builder.append("{}");
			return;
		}
		
		Iterator<QueryCondition> it = conditions.iterator();
		
		builder.append("{");
		
		while(it.hasNext())
		{
			builder.append(it.next());
			
			if(it.hasNext())
			{
				builder.append(" AND ");
			}
		}
		
		builder.append("}");
	}
}
