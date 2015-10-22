package com.fw.test.persitence.queries;

import com.fw.persistence.repository.annotations.Field;

public class EmpSearchResult
{
	@Field("name")
	private String empName;
	
	@Field("age")
	private int age;

	/**
	 * @return the {@link #empName empName}
	 */
	public String getEmpName()
	{
		return empName;
	}

	/**
	 * @param empName the {@link #empName empName} to set
	 */
	public void setEmpName(String empName)
	{
		this.empName = empName;
	}

	/**
	 * @return the {@link #age age}
	 */
	public int getAge()
	{
		return age;
	}

	/**
	 * @param age the {@link #age age} to set
	 */
	public void setAge(int age)
	{
		this.age = age;
	}
	
	
}
