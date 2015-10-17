package com.fw.test.persitence.entity;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Table(name = "ORDERS")
public class Order
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column
	private Date orderDate;
	
	@ManyToOne
	@Column(name = "CUST_ID")
	private Customer customer;
	
	@OneToMany(mappedBy = "order")
	private List<OrderItem> items;
	
	public Order(Date orderDate, Customer customer, List<OrderItem> items)
	{
		this.orderDate = orderDate;
		this.customer = customer;
		this.items = items;
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
	 * @return the {@link #orderDate orderDate}
	 */
	public Date getOrderDate()
	{
		return orderDate;
	}

	/**
	 * @param orderDate the {@link #orderDate orderDate} to set
	 */
	public void setOrderDate(Date orderDate)
	{
		this.orderDate = orderDate;
	}

	/**
	 * @return the {@link #customer customer}
	 */
	public Customer getCustomer()
	{
		return customer;
	}

	/**
	 * @param customer the {@link #customer customer} to set
	 */
	public void setCustomer(Customer customer)
	{
		this.customer = customer;
	}

	/**
	 * @return the {@link #items items}
	 */
	public List<OrderItem> getItems()
	{
		return items;
	}

	/**
	 * @param items the {@link #items items} to set
	 */
	public void setItems(List<OrderItem> items)
	{
		this.items = items;
	}
	
	
}
