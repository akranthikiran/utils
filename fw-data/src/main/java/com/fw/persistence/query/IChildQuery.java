package com.fw.persistence.query;

public interface IChildQuery
{
	public void addChildCondition(ConditionParam condition);
	
	public void addParentCondition(ConditionParam condition);
	
	public void addMapping(String childColumn, String parentColumn);
}
