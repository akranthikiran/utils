package com.fw.persistence.query;

import java.util.ArrayList;
import java.util.List;

import com.fw.persistence.EntityDetails;

public class SaveOrUpdateQuery extends Query
{
	private List<ColumnParam> insertColumns;
	private List<ColumnParam> updateColumns;

	public SaveOrUpdateQuery(EntityDetails entityDetails)
	{
		super(entityDetails);
	}

	/** 
	 * Adds value to {@link #insertColumns Insert Columns}
	 *
	 * @param column column to be added
	 */
	public void addInsertColumn(ColumnParam column)
	{
		if(insertColumns == null)
		{
			insertColumns = new ArrayList<ColumnParam>();
		}

		insertColumns.add(column);
	}

	public List<ColumnParam> getInsertColumns()
	{
		return insertColumns;
	}

	/** 
	 * Adds value to {@link #updateColumns Update Columns}
	 *
	 * @param column column to be added
	 */
	public void addUpdateColumn(ColumnParam column)
	{
		if(updateColumns == null)
		{
			updateColumns = new ArrayList<ColumnParam>();
		}

		updateColumns.add(column);
	}

	public List<ColumnParam> getUpdateColumns()
	{
		return updateColumns;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder(super.toString());
		builder.append("[");

		builder.append("Insert-Columns: ").append(insertColumns).append(", ");
		builder.append("Update-Columns: ").append(updateColumns);

		builder.append("]");
		return builder.toString();
	}
}
