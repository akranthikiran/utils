package com.fw.persistence.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.fw.persistence.EntityDetails;

/**
 * Abstract class for condition based queries.
 * @author akiran
 */
public abstract class AbstractConditionalQuery extends Query implements IConditionalQuery
{
	/**
	 * Holds query result fields
	 */
	private List<QueryResultField> resultFields;
	
	/**
	 * Holds query conditions
	 */
	private List<QueryCondition> conditions = new ArrayList<>();
	
	/**
	 * Holds query join conditions
	 */
	private List<QueryJoinCondition> joinConditions = new ArrayList<>();
	
	/**
	 * Holds tables that need to be used in query
	 */
	private List<QueryTable> tables;
	
	public AbstractConditionalQuery(EntityDetails entityDetails)
	{
		super(entityDetails);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.fw.persistence.query.IConditionalQuery#addCondition(com.fw.
	 * persistence.query.ConditionParam)
	 */
	@Override
	public void addCondition(QueryCondition condition)
	{
		if(conditions == null)
		{
			conditions = new ArrayList<>();
		}
		
		conditions.add(condition);
	}
	
	public List<QueryCondition> getConditions()
	{
		return conditions;
	}
	
	/**
	 * Clears all previously added non-join conditions if any
	 */
	public void clearConditions()
	{
		if(conditions == null)
		{
			return;
		}
		
		conditions.clear();
	}
	
	/* (non-Javadoc)
	 * @see com.fw.persistence.query.IConditionalQuery#addJoinCondition(com.fw.persistence.query.QueryJoinCondition)
	 */
	@Override
	public void addJoinCondition(QueryJoinCondition condition)
	{
		joinConditions.add(condition);
	}
	
	public List<QueryJoinCondition> getJoinConditions()
	{
		return joinConditions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.fw.persistence.query.IConditionalQuery#addResultField(com.fw.
	 * persistence.query.ResultField)
	 */
	@Override
	public void addResultField(QueryResultField resultField)
	{
		if(resultFields == null)
		{
			resultFields = new ArrayList<>();
		}
		
		resultFields.add(resultField);
	}
	
	public List<QueryResultField> getResultFields()
	{
		return resultFields;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.fw.persistence.query.IConditionalQuery#addTable(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public void addTable(QueryTable table)
	{
		if(tables == null)
		{
			tables = new ArrayList<>();
		}
		
		tables.add(table);
	}
	
	public List<QueryTable> getTables()
	{
		if(tables == null)
		{
			return Arrays.asList(new QueryTable(getTableName(), "T0"));
		}
		
		return tables;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder(super.toString());
		builder.append("[");

		if(CollectionUtils.isNotEmpty(this.resultFields))
		{
			builder.append("\n\tFields: ").append(this.resultFields);
		}
		
		if(CollectionUtils.isNotEmpty(this.tables))
		{
			builder.append("\n\tTables: ").append(this.tables);
		}
		
		if(CollectionUtils.isNotEmpty(this.joinConditions))
		{
			builder.append("\n\tJoins: ").append(this.joinConditions);
		}

		if(CollectionUtils.isNotEmpty(this.conditions))
		{
			builder.append("\n\tConditions: ").append(this.conditions);
		}

		builder.append("\n]");
		return builder.toString();
	}

}
