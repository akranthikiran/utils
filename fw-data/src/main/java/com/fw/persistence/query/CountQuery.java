package com.fw.persistence.query;

import java.util.ArrayList;
import java.util.List;

import com.fw.persistence.EntityDetails;

public class CountQuery extends Query implements IConditionalQuery
{
	private List<ConditionParam> conditions = new ArrayList<>();

	public CountQuery(EntityDetails entityDetails)
	{
		super(entityDetails);
	}

	/** 
	 * Adds value to {@link #conditions Conditions}
	 *
	 * @param condition condition to be added
	 */
	@Override
	public void addCondition(ConditionParam condition)
	{
		conditions.add(condition);
	}

	public List<ConditionParam> getConditions()
	{
		return conditions;
	}
	
	public void reset()
	{
		conditions.clear();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder(getClass().getName());
		builder.append("[Conditions: ");

		toString(conditions, builder);

		builder.append("]");
		return builder.toString();
	}
}
