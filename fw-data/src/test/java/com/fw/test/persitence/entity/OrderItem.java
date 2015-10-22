package com.fw.test.persitence.entity;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Table(name = "ORDER_ITEM")
public class OrderItem
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column
	private String itemName;

	@Column
	private int quantity;
	
	@ManyToOne
	@Column(name = "ORDER_ID")
	private Order order;

	public OrderItem()
	{}
	
	public OrderItem(String itemName, int quantity, Order order)
	{
		this.itemName = itemName;
		this.quantity = quantity;
		this.order = order;
	}
	
	/**
	 * @return the {@link #itemName itemName}
	 */
	public String getItemName()
	{
		return itemName;
	}

	/**
	 * @param itemName the {@link #itemName itemName} to set
	 */
	public void setItemName(String itemName)
	{
		this.itemName = itemName;
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
	 * @return the {@link #quantity quantity}
	 */
	public int getQuantity()
	{
		return quantity;
	}

	/**
	 * @param quantity the {@link #quantity quantity} to set
	 */
	public void setQuantity(int quantity)
	{
		this.quantity = quantity;
	}

	/**
	 * @return the {@link #order order}
	 */
	public Order getOrder()
	{
		return order;
	}

	/**
	 * @param order the {@link #order order} to set
	 */
	public void setOrder(Order order)
	{
		this.order = order;
	}
	
	
}
