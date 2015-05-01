package com.fw.test.persitence.entity;

import com.fw.persistence.annotations.Audit;
import com.fw.persistence.annotations.Column;
import com.fw.persistence.annotations.DataType;
import com.fw.persistence.annotations.IdField;
import com.fw.persistence.annotations.Table;
import com.fw.persistence.annotations.UniqueConstraint;
import com.fw.persistence.annotations.UniqueConstraints;

@Audit
@Table("STORY")
@UniqueConstraints(@UniqueConstraint(name = "name", fields = {"name"}))
public class Story
{
	@Column(type = DataType.LONG)
	@IdField
	private String id;

	private String name;
	
	private String description;
	
	public Story()
	{}

	public Story(String name, String description)
	{
		this.name = name;
		this.description = description;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}
}
