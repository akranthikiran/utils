package com.fw.persistence.query;

import java.util.Arrays;

import com.fw.persistence.EntityDetails;

public class CreateIndexQuery extends Query
{
	private String indexName;
	private String columns[];
	
	public CreateIndexQuery(EntityDetails entityDetails, String indexName, String columns[])
	{
		super(entityDetails);

		this.indexName = indexName;
		this.columns = columns;
	}
	
	public String getIndexName()
	{
		return indexName;
	}
	
	public String[] getColumns()
	{
		return columns;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder(getClass().getName()).append("[Name: ");
		
		builder.append(indexName).append(", Columns: ").append(Arrays.toString(columns));
		
		builder.append("]");
		
		return builder.toString();
	}
}
