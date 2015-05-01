package com.fw.dao.qry;

import java.sql.SQLException;

/**
 * ConnectionSource acts as the source of DB connections for QueryManager.
 * <BR/><BR/>
 * QueryManager by itself does not maintain transactions. So if multiple queries needs to be executed
 * as a single ATOMIC transaction, then a explicit ConnectionSource should be used. And commit should 
 * be done only at the successful completion of all queries.
 * <BR/><BR/>
 * QueryManager invokes commit() at end of each DML statements executed via executeUpdate(). Its upto the
 * implementations to maintain transactions.
 * <BR/><BR/>
 * Note: Instead of returning java.sql.Connection, getConnection() returns DBConnection implementations.
 * This is to facilitate developers to add Wrapper around SQL Connection with required attributes. The 
 * same attributes will be accessible during closeConnection() & commit()
 * @param <DBC> Wrapper class for Connection Object.
 */
public interface ConnectionSource
{
	/**
	 * Invoked by query manager before query execution to obtain required DB connection.
	 * @return Instance of DBConnection to be used
	 * @throws SQLException
	 */
	public DBConnection getConnection() throws SQLException;
	
	/**
	 * Name of this connection source. This will be useful when multiple DBs needs to 
	 * be used.<BR/>
	 * This value can be null. 
	 * @return
	 */
	public String getName();
	
}
