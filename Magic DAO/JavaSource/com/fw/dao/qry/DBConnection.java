package com.fw.dao.qry;

import java.sql.Connection;
import java.sql.SQLException;

public interface DBConnection
{
	public Connection getConnection() throws SQLException;
	
	public void rollback() throws SQLException;
	public void commit() throws SQLException;
	public void close() throws SQLException;
}
