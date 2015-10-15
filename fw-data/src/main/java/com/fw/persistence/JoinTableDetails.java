package com.fw.persistence;

/**
 * Represents join table (intermediate table) that is needed for many-to-many
 * relation
 * 
 * @author akiran
 */
public class JoinTableDetails
{
	/**
	 * Join table name
	 */
	private String tableName;

	/**
	 * Join column name. Used to link with the owner entity table
	 */
	private String joinColumn;

	/**
	 * Inverse join column name. Used to link with non-owner entity table
	 * (target table)
	 */
	private String inverseJoinColumn;
	
	public JoinTableDetails()
	{}
	
	public JoinTableDetails(String tableName, String joinColumn, String inverseJoinColumn)
	{
		this.tableName = tableName;
		this.joinColumn = joinColumn;
		this.inverseJoinColumn = inverseJoinColumn;
	}

	/**
	 * @return the {@link #tableName tableName}
	 */
	public String getTableName()
	{
		return tableName;
	}

	/**
	 * @param tableName
	 *            the {@link #tableName tableName} to set
	 */
	public void setTableName(String tableName)
	{
		this.tableName = tableName;
	}

	/**
	 * @return the {@link #joinColumn joinColumn}
	 */
	public String getJoinColumn()
	{
		return joinColumn;
	}

	/**
	 * @param joinColumn
	 *            the {@link #joinColumn joinColumn} to set
	 */
	public void setJoinColumn(String joinColumn)
	{
		this.joinColumn = joinColumn;
	}

	/**
	 * @return the {@link #inverseJoinColumn inverseJoinColumn}
	 */
	public String getInverseJoinColumn()
	{
		return inverseJoinColumn;
	}

	/**
	 * @param inverseJoinColumn
	 *            the {@link #inverseJoinColumn inverseJoinColumn} to set
	 */
	public void setInverseJoinColumn(String inverseJoinColumn)
	{
		this.inverseJoinColumn = inverseJoinColumn;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder(super.toString());
		builder.append("[");

		builder.append("Table: ").append(tableName);
		builder.append(",").append("Join Column: ").append(joinColumn);
		builder.append(",").append("Inverse Join Column: ").append(inverseJoinColumn);

		builder.append("]");
		return builder.toString();
	}

}
