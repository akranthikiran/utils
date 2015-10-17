package com.fw.test.persitence.entity;

import java.util.List;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import com.fw.persistence.annotations.UniqueConstraint;

@Table(name = "CUST_GROUP")
public class CustomerGroup
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@UniqueConstraint(name = "NAME")
	private String name;
	
	@ManyToMany(mappedBy = "customerGroups")
	private List<Customer> customers;

	public CustomerGroup(String name, List<Customer> customers)
	{
		this.name = name;
		this.customers = customers;
	}

	/**
	 * @return the {@link #id id}
	 */
	public long getId()
	{
		return id;
	}

	/**
	 * @param id the {@link #id id} to set
	 */
	public void setId(long id)
	{
		this.id = id;
	}

	/**
	 * @return the {@link #name name}
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name the {@link #name name} to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return the {@link #customers customers}
	 */
	public List<Customer> getCustomers()
	{
		return customers;
	}

	/**
	 * @param customers the {@link #customers customers} to set
	 */
	public void setCustomers(List<Customer> customers)
	{
		this.customers = customers;
	}
	
	
}
