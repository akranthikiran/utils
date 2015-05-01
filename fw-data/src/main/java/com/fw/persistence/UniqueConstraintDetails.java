package com.fw.persistence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UniqueConstraintDetails
{
	private String name;
	private List<String> fields = new ArrayList<>();
	private String message;
	private boolean validate;

	public UniqueConstraintDetails(String name, String fields[], String message, boolean validate)
	{
		if(name == null || name.trim().length() == 0)
		{
			throw new NullPointerException("Name can not be null or empty");
		}
		
		if(fields == null || fields.length == 0)
		{
			throw new NullPointerException("Fields can not be null or empty");
		}

		this.name = name;
		this.message = (message == null || message.trim().length() == 0) ? null : message.trim();
		this.validate = validate;
		
		this.fields.addAll(Arrays.asList(fields));
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getFieldsString()
	{
		return fields.toString();
	}

	public List<String> getFields()
	{
		return Collections.unmodifiableList(fields);
	}
	
	public boolean hasField(String field)
	{
		return fields.contains(field);
	}

	public String getMessage()
	{
		return message;
	}

	void setMessage(String message)
	{
		this.message = message;
	}

	public boolean isValidate()
	{
		return validate;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder(super.toString());
		builder.append("[");

		builder.append("Name: ").append(name);
		builder.append(",").append("Fields: ").append(fields);

		builder.append("]");
		return builder.toString();
	}
}
