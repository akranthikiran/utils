package com.fw.persistence.query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.fw.persistence.AuditDetails;
import com.fw.persistence.AuditType;
import com.fw.persistence.EntityDetails;

public class AuditEntryQuery extends Query
{
	private Set<String> columns;
	private List<ColumnParam> auditColumns = new ArrayList<ColumnParam>();
	private List<ConditionParam> conditions = new ArrayList<ConditionParam>();
	
	public AuditEntryQuery(EntityDetails entityDetails, AuditType auditType, String changedBy)
	{
		super(entityDetails);
		
		this.columns = entityDetails.getColumns();

		AuditDetails auditDetails = entityDetails.getAuditDetails();
		
		addAuditColumn(new ColumnParam(auditDetails.getChangedByColumn(), changedBy, 0));
		addAuditColumn(new ColumnParam(auditDetails.getTypeColumn(), auditType.toString(), 0));
		addAuditColumn(new ColumnParam(auditDetails.getTimeColumn(), new Date(), 0));
	}
	
	/** 
	 * Adds value to {@link #conditions Conditions}
	 *
	 * @param condition condition to be added
	 */
	public void addCondition(ConditionParam condition)
	{
		conditions.add(condition);
	}

	public List<ConditionParam> getConditions()
	{
		return conditions;
	}
	
	/** 
	 * Adds value to {@link #columns Columns}
	 *
	 * @param column column to be added
	 */
	public void addAuditColumn(ColumnParam column)
	{
		auditColumns.add(column);
	}

	public List<ColumnParam> getAuditColumns()
	{
		return auditColumns;
	}
	
	public String getAuditTableName()
	{
		return entityDetails.getAuditDetails().getTableName();
	}

	public Set<String> getColumns()
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

		builder.append("Table: ").append(entityDetails.getTableName()).append(",");
		builder.append("Audit-Table: ").append(getAuditTableName()).append(",");
		builder.append("Columns: ").append(columns).append(",");
		builder.append("Audit Columns: ").append(auditColumns);
		
		builder.append("]");
		return builder.toString();
	}
}
