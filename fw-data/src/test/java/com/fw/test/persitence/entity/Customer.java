package com.fw.test.persitence.entity;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Table(name = "CUSTOMER")
public class Customer
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(name = "NAME")
	private String name;

	@ManyToMany
	@JoinTable(name = "CUST_GROUP_CUST")
	private List<CustomerGroup> customerGroups;

	@OneToMany(mappedBy = "customer", cascade = {CascadeType.PERSIST})
	private List<Order> orders;

	public Customer(String name, List<CustomerGroup> customerGroups, List<Order> orders)
	{
		this.name = name;
		this.customerGroups = customerGroups;
		this.orders = orders;
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
	 * @return the {@link #customerGroups customerGroups}
	 */
	public List<CustomerGroup> getCustomerGroups()
	{
		return customerGroups;
	}

	/**
	 * @param customerGroups the {@link #customerGroups customerGroups} to set
	 */
	public void setCustomerGroups(List<CustomerGroup> customerGroups)
	{
		this.customerGroups = customerGroups;
	}

	/**
	 * @return the {@link #orders orders}
	 */
	public List<Order> getOrders()
	{
		return orders;
	}

	/**
	 * @param orders the {@link #orders orders} to set
	 */
	public void setOrders(List<Order> orders)
	{
		this.orders = orders;
	}
	
	
}
