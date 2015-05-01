package com.fw.dao.qry.impl;

import java.sql.SQLException;

import com.fw.dao.qry.DataDigester;
import com.fw.dao.qry.QueryResultData;

public class ObjectDataDigester implements DataDigester<Object[]>
{
	@Override
    public Object[] digest(QueryResultData rsData) throws SQLException
    {
		return rsData.toObjectArray();
    }

	@Override
	public void finalizeDigester() 
	{}
}
