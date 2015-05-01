package com.fw.persistence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fw.persistence.EntityDetails;

/**
 * Represents a generic select query that can be executed against multiple entities
 * from any repository.
 */
public class SelectQuery
{
	/**
	 * Defines the way two queries are combined
	 */
	public static enum CombinationType
	{
		UNION, UNION_ALL, INTERSECTION;
	}
	
	public static interface Argument
	{}
	
	public static class StringArgument implements Argument
	{
		private String value;

		public StringArgument(String value)
		{
			this.value = value;
		}
		
		public String getValue()
		{
			return value;
		}
	}
	
	public static class FieldArgument implements Argument
	{
		private String field;

		public FieldArgument(String field)
		{
			this.field = field;
		}

		public String getField()
		{
			return field;
		}
	}
	
	public static class ValueArgument implements Argument
	{
		private Object value;

		public ValueArgument(Object value)
		{
			this.value = value;
		}

		public Object getValue()
		{
			return value;
		}
	}
	
	public static class Function
	{
		private String name;
		private Argument[] arguments;
		
		private Function(String name, Argument[] arguments)
		{
			this.name = name;
			this.arguments = arguments;
		}

		public String getName()
		{
			return name;
		}

		public Argument[] getArguments()
		{
			return arguments;
		}
	}
	
	public static class Field
	{
		private Object field;
		private String label;
		
		private Field(Object field, String label)
		{
			this.field = field;
			this.label = label;
		}

		public Object getField()
		{
			return field;
		}

		public String getLabel()
		{
			return label;
		}
	}
	
	/**
	 * Represents condition during record fetching
	 */
	public static class Condition
	{
		private String field;
		private Operator operator;
		private Object rightOperand;
		
		private Condition(String field, Operator operator, Object rightOperand)
		{
			this.field = field;
			this.operator = operator;
			this.rightOperand = rightOperand;
		}

		public String getField()
		{
			return field;
		}

		public Operator getOperator()
		{
			return operator;
		}

		public Object getRightOperand()
		{
			return rightOperand;
		}
	}
	
	public static class QueryCombination
	{
		private CombinationType type;
		private SelectQuery query;
		
		private QueryCombination(CombinationType type, SelectQuery query)
		{
			this.type = type;
			this.query = query;
		}
		public CombinationType getType()
		{
			return type;
		}
		public SelectQuery getQuery()
		{
			return query;
		}
	}
	
	private Class<?> entityType;
	
	private List<Field> fields = new ArrayList<>();
	private List<Condition> conditions;
	
	private List<QueryCombination> combinedQueries;
	
	private List<String> orderByFields;
	
	private List<String> groupByFields;

	public SelectQuery(Class<?> entityType)
	{
		this.entityType = entityType;
	}
	
	public Class<?> getEntityType()
	{
		return entityType;
	}
	
	public void addField(String name)
	{
		if(name == null || name.trim().length() == 0)
		{
			throw new NullPointerException("Name can not be null or empty");
		}
		
		this.fields.add(new Field(name, null));
	}
	
	public void addFunctionField(String label, String name, Argument... arguments)
	{
		if(label == null || label.trim().length() == 0)
		{
			throw new NullPointerException("Label can not be null or empty");
		}
		
		if(name == null || name.trim().length() == 0)
		{
			throw new NullPointerException("Name can not be null or empty");
		}
		
		this.fields.add(new Field(new Function(name, arguments), label));
	}
	
	public void addQueryField(String label, SelectQuery query)
	{
		if(label == null || label.trim().length() == 0)
		{
			throw new NullPointerException("Label can not be null or empty");
		}
		
		if(query == null)
		{
			throw new NullPointerException("Query can not be null");
		}
		
		this.fields.add(new Field(query, label));
	}
	
	public List<Field> getFields()
	{
		return Collections.unmodifiableList(fields);
	}
}
