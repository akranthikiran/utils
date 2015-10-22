package com.fw.test.persitence.entity;

import java.util.List;

import javax.persistence.CascadeType;
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
	private String title;
	
	@Column
	private int orderNo;
	
	@ManyToOne
	@Column(name = "CUST_ID")
	private Customer customer;
	
	@OneToMany(mappedBy = "order", cascade = {CascadeType.PERSIST})
	private List<OrderItem> items;
	
	public Order()
	{}
	
	public Order(String title, int orderNo, Customer customer, List<OrderItem> items)
	{
		this.title = title;
		this.orderNo = orderNo;
		this.customer = customer;
		this.items = items;
	}
	
	/**
	 * @return the {@link #title title}
	 */
	public String getTitle()
	{
		return title;
	}

	/**
	 * @param title the {@link #title title} to set
	 */
	public void setTitle(String title)
	{
		this.title = title;
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
	 * @return the {@link #orderNo orderNo}
	 */
	public int getOrderNo()
	{
		return orderNo;
	}

	/**
	 * @param orderNo the {@link #orderNo orderNo} to set
	 */
	public void setOrderNo(int orderNo)
	{
		this.orderNo = orderNo;
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
