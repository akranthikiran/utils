package com.fw.persistence.query;

import com.fw.persistence.EntityDetails;

public class FinderQuery extends AbstractConditionalQuery
{
	public FinderQuery(EntityDetails entityDetails)
	{
		super(entityDetails);
	}
}
