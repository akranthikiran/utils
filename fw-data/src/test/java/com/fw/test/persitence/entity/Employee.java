package com.fw.test.persitence.entity;

import com.fw.persistence.annotations.Column;
import com.fw.persistence.annotations.DataType;
import com.fw.persistence.annotations.IdField;
import com.fw.persistence.annotations.Index;
import com.fw.persistence.annotations.Indexed;
import com.fw.persistence.annotations.Indexes;
import com.fw.persistence.annotations.ReadOnly;
import com.fw.persistence.annotations.Table;
import com.fw.persistence.annotations.UniqueConstraint;

@Table("EMPLOYEE")
@Indexes({
	@Index(fields={"phoneNo", "name"})
})
public class Employee
{
	public static final String ERROR_MESSAGE_DUPLICATE_EMAIL = "Specified email-id already exists: ${emailId}";
	
	@Column(type = DataType.LONG)
	@IdField
	private String id;

	@ReadOnly
	@UniqueConstraint(name="EmpNo")
	@Column(name = "EMP_NO")
	private String employeeNo;
	
	@UniqueConstraint(name = "EmailId", message = ERROR_MESSAGE_DUPLICATE_EMAIL)
	private String emailId;
	
	@Indexed
	@Column(name = "ENAME")
	private String name;
	
	private String phoneNo;
	
	public Employee()
	{}
	
	public Employee(String employeeNo, String emailId, String name, String phoneNo)
	{
		this.employeeNo = employeeNo;
		this.emailId = emailId;
		this.name = name;
		this.phoneNo = phoneNo;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getEmployeeNo()
	{
		return employeeNo;
	}

	public void setEmployeeNo(String employeeNo)
	{
		this.employeeNo = employeeNo;
	}

	public String getEmailId()
	{
		return emailId;
	}

	public void setEmailId(String emailId)
	{
		this.emailId = emailId;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getPhoneNo()
	{
		return phoneNo;
	}

	public void setPhoneNo(String phoneNo)
	{
		this.phoneNo = phoneNo;
	}
}
