package com.fw.test.persitence.entity;

import com.fw.persistence.annotations.AutogenerationType;
import com.fw.persistence.annotations.Column;
import com.fw.persistence.annotations.DataType;
import com.fw.persistence.annotations.ForeignConstraint;
import com.fw.persistence.annotations.ForeignConstraints;
import com.fw.persistence.annotations.IdField;
import com.fw.persistence.annotations.Mapping;
import com.fw.persistence.annotations.MappingCondition;
import com.fw.persistence.annotations.Table;

@Table("ADDRESS")
@ForeignConstraints({
	@ForeignConstraint(
			name="PARENT_ID", 
			mappings={@Mapping(from="parentId", to="id")},
			conditions={@MappingCondition(field="parentType", value = Address.PARENT_TYPE_EMPLOYEE)},
			foreignEntity=Employee.class,
			message="Invalid parent id specified ${parentId}"
	)
})
public class Address
{
	public static final String ERROR_MESSAGE_DUPLICATE_EMAIL = "Specified email-id already exists: ${emailId}";
	
	public static final String PARENT_TYPE_EMPLOYEE = "EMPLOYEE";
	public static final String PARENT_TYPE_OTHER = "OTHER";
	
	@Column(type = DataType.LONG)
	@IdField(autogeneration = AutogenerationType.SEQUENCE, sequenceName = "SEQ_ADDRESS_ID")
	private String id;

	private String address;

	@Column(type = DataType.LONG)
	private String parentId;
	
	private String parentType;
	
	public Address()
	{}

	public Address(String id, String address, String parentId, String parentType)
	{
		this.id = id;
		this.address = address;
		this.parentId = parentId;
		this.parentType = parentType;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getAddress()
	{
		return address;
	}

	public void setAddress(String address)
	{
		this.address = address;
	}

	public String getParentId()
	{
		return parentId;
	}

	public void setParentId(String parentId)
	{
		this.parentId = parentId;
	}

	public String getParentType()
	{
		return parentType;
	}

	public void setParentType(String parentType)
	{
		this.parentType = parentType;
	}
}
