package com.fw.persistence.query;

import java.util.ArrayList;
import java.util.List;

import com.fw.persistence.EntityDetails;

public class UpdateQuery extends Query implements IConditionalQuery
{
	private List<ColumnParam> columns;
	private List<ConditionParam> conditions;

	public UpdateQuery(EntityDetails entityDetails)
	{
		super(entityDetails);
	}

	/** 
	 * Adds value to {@link #columns Columns}
	 *
	 * @param column column to be added
	 */
	public void addColumn(ColumnParam column)
	{
		if(columns == null)
		{
			columns = new ArrayList<ColumnParam>();
		}

		columns.add(column);
	}

	public List<ColumnParam> getColumns()
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
	
	public boolean hasConditions()
	{
		return (conditions != null && !conditions.isEmpty());
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder(super.toString());
		builder.append("[Columns: ");

		if(columns != null)
		{
			for(ColumnParam column: columns)
			{
				builder.append(column).append(", ");
			}
		}
		
		builder.append("}, Conditions: ");
		
		toString(conditions, builder);

		builder.append("]");
		return builder.toString();
	}
}
