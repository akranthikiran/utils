package com.fw.test.persitence.queries;

import com.fw.persistence.Operator;
import com.fw.persistence.repository.annotations.Condition;

/**
 * Test query object to find employees
 * @author akiran
 */
public class EmpSearchQuery
{
	@Condition("name")
	private String name;

	@Condition(op = Operator.LIKE)
	private String phoneNo;

	@Condition(value = "age", op = Operator.GE)
	private Integer minAge;

	@Condition(value = "age", op = Operator.LE)
	private Integer maxAge;

	public EmpSearchQuery(String name, String phoneNo, Integer minAge, Integer maxAge)
	{
		this.name = name;
		this.phoneNo = phoneNo;
		this.minAge = minAge;
		this.maxAge = maxAge;
	}

	/**
	 * @return the {@link #name name}
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name
	 *            the {@link #name name} to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return the {@link #phoneNo phoneNo}
	 */
	public String getPhoneNo()
	{
		return phoneNo;
	}

	/**
	 * @param phoneNo
	 *            the {@link #phoneNo phoneNo} to set
	 */
	public void setPhoneNo(String phoneNo)
	{
		this.phoneNo = phoneNo;
	}

	/**
	 * @return the {@link #minAge minAge}
	 */
	public Integer getMinAge()
	{
		return minAge;
	}

	/**
	 * @param minAge
	 *            the {@link #minAge minAge} to set
	 */
	public void setMinAge(Integer minAge)
	{
		this.minAge = minAge;
	}

	/**
	 * @return the {@link #maxAge maxAge}
	 */
	public Integer getMaxAge()
	{
		return maxAge;
	}

	/**
	 * @param maxAge
	 *            the {@link #maxAge maxAge} to set
	 */
	public void setMaxAge(Integer maxAge)
	{
		this.maxAge = maxAge;
	}

}
