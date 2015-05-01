package com.fw.persistence.query;

import java.util.ArrayList;
import java.util.List;

import com.fw.persistence.EntityDetails;

public class FetchChildrenIdsQuery extends Query implements IChildQuery
{
	private List<ConditionParam> childConditions = new ArrayList<>();
	private List<ConditionParam> parentConditions = new ArrayList<>();

	private EntityDetails childEntityDetails;
	private EntityDetails parentEntityDetails;

	private List<String> childColumns = new ArrayList<>();
	private List<String> parentColumns = new ArrayList<>();
	
	public FetchChildrenIdsQuery(EntityDetails childEntityDetails, EntityDetails parentEntityDetails)
	{
		super(childEntityDetails);

		this.childEntityDetails = childEntityDetails;
		this.parentEntityDetails = parentEntityDetails;
	}
	
	public String getChildIdColumn()
	{
		return childEntityDetails.getIdField().getColumn();
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
	public void addChildCondition(ConditionParam condition)
	{
		if(childConditions == null)
		{
			childConditions = new ArrayList<ConditionParam>();
		}

		childConditions.add(condition);
	}

	public List<ConditionParam> getChildConditions()
	{
		return childConditions;
	}

	/** 
	 * Adds value to {@link #parentConditions parent Conditions}
	 *
	 * @param condition condition to be added
	 */
	public void addParentCondition(ConditionParam condition)
	{
		if(parentConditions == null)
		{
			parentConditions = new ArrayList<ConditionParam>();
		}

		parentConditions.add(condition);
	}

	public List<ConditionParam> getParentConditions()
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
