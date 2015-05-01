package com.fw.dao.qry.impl;

import java.sql.SQLException;

import javax.sql.DataSource;

import com.fw.dao.qry.DBConnection;

public class DSConnectionSource extends AbstractConnectionSource
{
	private DataSource dataSource;

	public DataSource getDataSource()
	{
		return dataSource;
	}

	public void setDataSource(DataSource dataSource)
	{
		this.dataSource = dataSource;
	}

	@Override
	public DBConnection getConnection() throws SQLException
	{
		return new SimpleDBConnection(dataSource.getConnection());
	}
}
