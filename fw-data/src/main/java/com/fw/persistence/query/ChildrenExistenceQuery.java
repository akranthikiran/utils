package com.fw.persistence.query;

import java.util.ArrayList;
import java.util.List;

import com.fw.persistence.EntityDetails;

public class ChildrenExistenceQuery extends Query implements IChildQuery
{
	private List<QueryCondition> childConditions = new ArrayList<>();
	private List<QueryCondition> parentConditions = new ArrayList<>();

	private EntityDetails childEntityDetails;
	private EntityDetails parentEntityDetails;

	private List<String> childColumns = new ArrayList<>();
	private List<String> parentColumns = new ArrayList<>();
	
	public ChildrenExistenceQuery(EntityDetails childEntityDetails, EntityDetails parentEntityDetails)
	{
		super(childEntityDetails);

		this.childEntityDetails = childEntityDetails;
		this.parentEntityDetails = parentEntityDetails;
	}

	public String getChildTableName()
	{
		return childEntityDetails.getTableName();
	}

	public String getParentTableName()
	{
		return parentEntityDetails.getTableName();
	}

	/**
	 * Adds value to {@link #childConditions Conditions}
	 *
	 * @param condition condition to be added
	 */
	public void addChildCondition(QueryCondition condition)
	{
		if(childConditions == null)
		{
			childConditions = new ArrayList<QueryCondition>();
		}

		childConditions.add(condition);
	}

	public List<QueryCondition> getChildConditions()
	{
		return childConditions;
	}

	/** 
	 * Adds value to {@link #parentConditions parent Conditions}
	 *
	 * @param condition condition to be added
	 */
	public void addParentCondition(QueryCondition condition)
	{
		if(parentConditions == null)
		{
			parentConditions = new ArrayList<QueryCondition>();
		}

		parentConditions.add(condition);
	}

	public List<QueryCondition> getParentConditions()
	{
		return parentConditions;
	}

	public void addMapping(String childColumn, String parentColumn)
	{
		childColumns.add(childColumn);
		parentColumns.add(parentColumn);
	}

	public List<String> getChildColumns()
	{
		return childColumns;
	}
	
	public List<String> getParentColumns()
	{
		return parentColumns;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder(super.toString());
		builder.append("[Child Conditions: ");

		toString(childConditions, builder);
		
		builder.append(" || Parent Conditions: ");

		toString(parentConditions, builder);

		builder.append("]");
		return builder.toString();
	}
}
