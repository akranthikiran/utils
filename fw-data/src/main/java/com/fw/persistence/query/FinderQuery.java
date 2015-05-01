package com.fw.persistence.query;

import java.util.ArrayList;
import java.util.List;

import com.fw.persistence.EntityDetails;

public class FinderQuery extends Query implements IConditionalQuery
{
	private List<String> columns;
	private List<ConditionParam> conditions;

	public FinderQuery(EntityDetails entityDetails)
	{
		super(entityDetails);
	}

	/** 
	 * Adds value to {@link #columns Columns}
	 *
	 * @param column column to be added
	 */
	public void addColumn(String column)
	{
		if(columns == null)
		{
			columns = new ArrayList<String>();
		}

		columns.add(column);
	}

	public List<String> getColumns()
	{
		return columns;
	}

	/** 
	 * Adds value to {@link #conditions Conditions}
	 *
	 * @param condition condition to be added
	 */
	public void addCondition(ConditionParam condition)
	{
		if(conditions == null)
		{
			conditions = new ArrayList<ConditionParam>();
		}

		conditions.add(condition);
	}

	public List<ConditionParam> getConditions()
	{
		return conditions;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder(super.toString());
		builder.append("[");

		builder.append("Columns: ").append(columns);
		builder.append(",").append("Conditions: ");
		
		toString(conditions, builder);

		builder.append("]");
		return builder.toString();
	}
}
