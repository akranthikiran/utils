package com.fw.persistence.query;

public interface IChildQuery
{
	public void addChildCondition(QueryCondition condition);
	
	public void addParentCondition(QueryCondition condition);
	
	public void addMapping(String childColumn, String parentColumn);
}
