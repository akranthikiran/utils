package com.fw.persistence.query;

import java.util.ArrayList;
import java.util.List;

import com.fw.persistence.EntityDetails;

public class SaveQuery extends Query
{
	private List<ColumnParam> columns;

	public SaveQuery(EntityDetails entityDetails)
	{
		super(entityDetails);
	}

	/** 
	 * Adds value to {@link #columns Columns}
	 *
	 * @param column column to be added
	 */
	public void addColumn(ColumnParam column)
	{
		if(columns == null)
		{
			columns = new ArrayList<ColumnParam>();
		}

		columns.add(column);
	}

	public List<ColumnParam> getColumns()
	{
		return columns;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder(super.toString());
		builder.append("[");

		builder.append("Columns: ").append(columns);

		builder.append("]");
		return builder.toString();
	}
}
