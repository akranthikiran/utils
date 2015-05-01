package com.fw.persistence;

import java.util.Date;

public class AuditSearchQuery
{
	private Date fromDate;
	private Date toDate;
	private Object entityId;
	private AuditType auditType;
	private String changedBy;

	public AuditSearchQuery()
	{
	}
	
	public AuditSearchQuery(Date fromDate, Date toDate, Object entityId, AuditType auditType)
	{
		this.fromDate = fromDate;
		this.toDate = toDate;
		this.entityId = entityId;
		this.auditType = auditType;
	}

	public Date getFromDate()
	{
		return fromDate;
	}

	public void setFromDate(Date fromDate)
	{
		this.fromDate = fromDate;
	}

	public Date getToDate()
	{
		return toDate;
	}

	public void setToDate(Date toDate)
	{
		this.toDate = toDate;
	}

	public Object getEntityId()
	{
		return entityId;
	}

	public void setEntityId(Object entityId)
	{
		this.entityId = entityId;
	}

	public AuditType getAuditType()
	{
		return auditType;
	}

	public String getAuditTypeName()
	{
		return auditType.name();
	}

	public void setAuditType(AuditType auditType)
	{
		this.auditType = auditType;
	}

	public String getChangedBy()
	{
		return changedBy;
	}

	public void setChangedBy(String changedBy)
	{
		this.changedBy = changedBy;
	}
}
