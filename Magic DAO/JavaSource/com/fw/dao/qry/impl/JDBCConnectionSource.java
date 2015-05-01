package com.fw.dao.qry.impl;

import java.sql.DriverManager;
import java.sql.SQLException;

import com.fw.dao.qry.DBConnection;

public class JDBCConnectionSource extends AbstractConnectionSource
{
	private String dbUrl;
	private String user;
	private String password;
	private String driver;

	public JDBCConnectionSource()
	{}

	public JDBCConnectionSource(String dbUrl, String user, String password, String driver)
	{
		super();
		this.dbUrl = dbUrl;
		this.user = user;
		this.password = password;
		this.driver = driver;
	}

	public void setDbUrl(String dbUrl)
	{
		this.dbUrl = dbUrl;
	}

	public void setUser(String user)
	{
		this.user = user;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public void setDriver(String driver)
	{
		this.driver = driver;
	}

	@Override
	public DBConnection getConnection() throws SQLException
	{
		try
		{
			Class.forName(driver);
		}catch(Exception ex)
		{
			throw new SQLException("Invalid driver class name specified: " + driver, ex);
		}

		return new SimpleDBConnection(DriverManager.getConnection(dbUrl, user, password));
	}
}
