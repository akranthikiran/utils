package com.fw.persistence.repository.search;

import java.util.ArrayList;
import java.util.List;

/**
 * Search query object for search methods with dynamic conditions
 * 
 * @author akiran
 */
public class SearchQuery
{
	private List<SearchCondition> conditions = new ArrayList<>();
	
	public SearchQuery()
	{}
	
	public SearchQuery(SearchCondition... conditions)
	{
		for(SearchCondition cond : conditions)
		{
			this.addCondition(cond);
		}
	}

	/**
	 * Adds value to {@link #conditions conditions}
	 *
	 * @param condition
	 *            condition to be added
	 */
	public void addCondition(SearchCondition condition)
	{
		conditions.add(condition);
	}
	
	public void setConditions(List<SearchCondition> conditions)
	{
		this.conditions = conditions;
	}

	public List<SearchCondition> getConditions()
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

		builder.append("Conditions: ").append(conditions);

		builder.append("]");
		return builder.toString();
	}

}
