package com.fw.persistence.query;

import com.fw.persistence.EntityDetails;
import com.fw.persistence.query.data.TableStructure;

/**
 * Drop query to drop underlying entity table 
 * @author akiran
 */
public class DropTableQuery extends Query
{
	private TableStructure tableStructure;
	
	public DropTableQuery(EntityDetails entityDetails)
	{
		super(entityDetails);
		
		this.tableStructure = new TableStructure(entityDetails);
	}
	
	public TableStructure getTableStructure()
	{
		return tableStructure;
	}
	
	public String getTableName()
	{
		return tableStructure.getTableName();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder(getClass().getName());
		builder.append("{ ").append(getTableName()).append(" }");
		return builder.toString();
	}
}
