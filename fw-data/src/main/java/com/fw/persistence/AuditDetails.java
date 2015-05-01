package com.fw.persistence;

public class AuditDetails
{
	private String tableName;

	private String idColumn;
	private String typeColumn;
	private String timeColumn;
	private String changedByColumn;

	public AuditDetails(String tableName, String idColumn, String typeColumn, String timeColumn, String changedByColumn)
	{
		this.tableName = tableName;
		this.idColumn = idColumn;
		this.typeColumn = typeColumn;
		this.timeColumn = timeColumn;
		this.changedByColumn = changedByColumn;
	}

	public String getTableName()
	{
		return tableName;
	}

	public String getIdColumn()
	{
		return idColumn;
	}

	public String getTypeColumn()
	{
		return typeColumn;
	}

	public String getTimeColumn()
	{
		return timeColumn;
	}

	public String getChangedByColumn()
	{
		return changedByColumn;
	}

}
