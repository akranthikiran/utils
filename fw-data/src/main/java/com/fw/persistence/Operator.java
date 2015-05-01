package com.fw.persistence;

public enum Operator
{
	EQ("="), LT("<"), LE("<="), GT(">"), GE(">="), NE("!="), LIKE("LIKE"), IN("IN"), NOT_IN("NOT IN");
	
	private String operator;

	private Operator(String operator)
	{
		this.operator = operator;
	}

	@Override
	public String toString()
	{
		return operator;
	}
	
	
}
