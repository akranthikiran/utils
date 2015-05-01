package com.fw.test.persitence.entity;

import com.fw.persistence.annotations.Audit;
import com.fw.persistence.annotations.Column;
import com.fw.persistence.annotations.DataType;
import com.fw.persistence.annotations.DeleteCascade;
import com.fw.persistence.annotations.ForeignConstraint;
import com.fw.persistence.annotations.ForeignConstraints;
import com.fw.persistence.annotations.IdField;
import com.fw.persistence.annotations.Mapping;
import com.fw.persistence.annotations.Table;

@Audit(table = "TASK_AUDITING")
@ForeignConstraints({
	@ForeignConstraint(name = "story", foreignEntity = Story.class, mappings = { @Mapping(from = "storyId", to = "id")}, deleteCascade = DeleteCascade.DELETE_WITH_PARENT)
})
@Table("TASK")
public class Task
{
	@Column(type = DataType.LONG)
	@IdField
	private String id;

	@Column(type = DataType.LONG)
	private String storyId;

	private String name;
	
	public Task()
	{}
	
	public Task(String storyId, String name)
	{
		this.storyId = storyId;
		this.name = name;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getStoryId()
	{
		return storyId;
	}

	public void setStoryId(String storyId)
	{
		this.storyId = storyId;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}
}
