package com.fw.dao.qry.impl;

import java.sql.Connection;
import java.sql.SQLException;

import com.fw.dao.qry.DBConnection;

public class SimpleDBConnection implements DBConnection
{
	private Connection connection;

	public SimpleDBConnection(Connection connection)
	{
		this(connection, false);
	}

	public SimpleDBConnection(Connection connection, boolean autoCommit)
	{
		this.connection = connection;

		try
		{
			connection.setAutoCommit(autoCommit);
		}catch(Exception ex)
		{
			throw new IllegalStateException("An error occured while setting autoCommit flag", ex);
		}
	}

	protected SimpleDBConnection()
	{}

	protected void setConnection(Connection con)
	{
		this.connection = con;
	}

	@Override
	public void close() throws SQLException
	{
		connection.close();
	}

	@Override
	public void commit() throws SQLException
	{
		connection.commit();
	}

	@Override
	public Connection getConnection() throws SQLException
	{
		return connection;
	}

	@Override
	public void rollback() throws SQLException
	{
		connection.rollback();
	}
}
