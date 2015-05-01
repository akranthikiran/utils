package com.fw.persistence;

import java.util.Date;

import com.fw.persistence.annotations.AutogenerationType;
import com.fw.persistence.annotations.Column;
import com.fw.persistence.annotations.DataType;
import com.fw.persistence.annotations.IdField;

public class AuditEntryEntity
{
	@IdField(autogeneration = AutogenerationType.AUTO)
	@Column(type = DataType.LONG)
	private String _auditId;

	@Column(type = DataType.STRING, length = 50)
	private AuditType _auditType;

	@Column(type = DataType.DATE_TIME)
	private Date _auditTime;

	@Column(length = 500)
	private String _auditChangedBy;

	public String get_auditId()
	{
		return _auditId;
	}

	public void set_auditId(String _auditId)
	{
		this._auditId = _auditId;
	}

	public AuditType get_auditType()
	{
		return _auditType;
	}

	public void set_auditType(AuditType _auditType)
	{
		this._auditType = _auditType;
	}

	public Date get_auditTime()
	{
		return _auditTime;
	}

	public void set_auditTime(Date _auditTime)
	{
		this._auditTime = _auditTime;
	}

	public String get_auditChangedBy()
	{
		return _auditChangedBy;
	}

	public void set_auditChangedBy(String _auditChangedBy)
	{
		this._auditChangedBy = _auditChangedBy;
	}

}
