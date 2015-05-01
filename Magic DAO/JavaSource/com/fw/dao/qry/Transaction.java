package com.fw.dao.qry;

import java.sql.SQLException;

public interface Transaction extends AutoCloseable
{
	public void commit();
	public void rollback();
	public void close();
	public boolean isClosed();
	
	public DBConnection getConnection(String queryName) throws SQLException;
}
