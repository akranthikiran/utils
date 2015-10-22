package com.fw.persistence.query;

/**
 * Result field of the query
 * @author akiran
 */
public class QueryTable
{
	private String table;
	
	/**
	 * Table (code) to be used for this condition
	 */
	private String tableCode;

	public QueryTable(String table, String tableCode)
	{
		this.table = table;
		this.tableCode = tableCode;
	}

	/**
	 * @return the {@link #table table}
	 */
	public String getTable()
	{
		return table;
	}

	/**
	 * @param table the {@link #table table} to set
	 */
	public void setTable(String table)
	{
		this.table = table;
	}

	/**
	 * @return the {@link #tableCode tableCode}
	 */
	public String getTableCode()
	{
		return tableCode;
	}

	/**
	 * @param tableCode the {@link #tableCode tableCode} to set
	 */
	public void setTableCode(String tableCode)
	{
		this.tableCode = tableCode;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder("[");

		builder.append(table).append(" ").append(tableCode);
		builder.append("]");

		return builder.toString();
	}

}
