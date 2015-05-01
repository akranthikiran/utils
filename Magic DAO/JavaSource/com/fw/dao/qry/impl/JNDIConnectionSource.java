package com.fw.dao.qry.impl;

import java.sql.SQLException;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import com.fw.dao.qry.DBConnection;

public class JNDIConnectionSource extends AbstractConnectionSource
{
	private String contextEnvPath;
	private String dataSourceName;
	private Properties properties;

	private DataSource dataSource;

	public JNDIConnectionSource()
	{}

	public JNDIConnectionSource(String contextEnvPath, String dataSourceName, Properties properties)
	{
		this.contextEnvPath = contextEnvPath;
		this.dataSourceName = dataSourceName;
		this.properties = properties;
	}

	public void setContextEnvPath(String contextEnvPath)
	{
		this.contextEnvPath = contextEnvPath;
	}

	public void setDataSourceName(String dataSourceName)
	{
		this.dataSourceName = dataSourceName;
	}

	public void addProperty(String name, String value)
	{
		properties.put(name, value);
	}

	@Override
	public DBConnection getConnection() throws SQLException
	{
		if(dataSource != null)
			return new SimpleDBConnection(dataSource.getConnection());

		try
		{
			InitialContext initContext = new InitialContext(properties);
			Context context = (Context)initContext.lookup(contextEnvPath);
			this.dataSource = (DataSource)context.lookup(dataSourceName);
		}catch(Exception ex)
		{
			throw new SQLException("An error occured while looking up for data source", ex);
		}

		return new SimpleDBConnection(dataSource.getConnection());
	}
}
