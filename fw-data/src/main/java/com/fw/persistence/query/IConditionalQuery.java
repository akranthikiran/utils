package com.fw.persistence.query;

/**
 * Abstraction of condition queries like - Select, Delete and Update
 * @author akiran
 */
public interface IConditionalQuery
{
	/**
	 * Adds a condition for this query
	 * @param condition
	 */
	public void addCondition(QueryCondition condition);

	/**
	 * Adds a join condition for this query
	 * @param condition
	 */
	public void addJoinCondition(QueryJoinCondition condition);

	/**
	 * Adds result field for this query
	 * @param resultField
	 */
	public void addResultField(QueryResultField resultField);
	
	/**
	 * Adds table name to this query 
	 */
	public void addTable(QueryTable table);
	
	
}
