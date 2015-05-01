package com.fw.persistence.query;

import java.util.ArrayList;
import java.util.List;

import com.fw.persistence.EntityDetails;

public class DeleteQuery extends Query implements IConditionalQuery
{
	private List<ConditionParam> conditions = new ArrayList<>();
	
	public DeleteQuery(EntityDetails entityDetails)
	{
		super(entityDetails);
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
		StringBuilder builder = new StringBuilder(getClass().getName());
		builder.append("[Conditions: ");

		toString(conditions, builder);

		builder.append("]");
		return builder.toString();
	}
}
