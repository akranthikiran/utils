package com.fw.persistence.query;

import com.fw.persistence.EntityDetails;
import com.fw.persistence.query.data.TableStructure;

public class CreateTableQuery extends Query
{
	private TableStructure tableStructure;

	/**
	 * Indicates if this is join table or not
	 */
	private boolean isJoinTable;

	public CreateTableQuery(EntityDetails entityDetails)
	{
		super(entityDetails);

		this.tableStructure = new TableStructure(entityDetails);
	}

	public CreateTableQuery(EntityDetails entityDetails, boolean joinTable)
	{
		this(entityDetails);
		this.isJoinTable = joinTable;
	}

	public TableStructure getTableStructure()
	{
		return tableStructure;
	}

	public String getTableName()
	{
		return tableStructure.getTableName();
	}

	/**
	 * @return the {@link #isJoinTable isJoinTable}
	 */
	public boolean isJoinTable()
	{
		return isJoinTable;
	}

	/**
	 * @param isJoinTable
	 *            the {@link #isJoinTable isJoinTable} to set
	 */
	public void setJoinTable(boolean isJoinTable)
	{
		this.isJoinTable = isJoinTable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder(getClass().getName());
		return builder.toString();
	}
}
