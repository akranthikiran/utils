package com.fw.persistence.query;

import java.util.ArrayList;
import java.util.List;

import com.fw.persistence.EntityDetails;

public class UpdateQuery extends AbstractConditionalQuery
{
	private List<ColumnParam> columns;

	public UpdateQuery(EntityDetails entityDetails)
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

}
