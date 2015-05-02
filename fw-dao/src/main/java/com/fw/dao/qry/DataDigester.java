package com.fw.dao.qry;

import java.sql.SQLException;

/**
 * For every row fetched by QueryManager will invoke digest() on the corresponding digester.
 * Digester is expected to digest the data and provide the resultant value, this might be a
 * bean, a map, etc.
 * @param <T>
 */
public interface DataDigester<T>
{
	/**
	 * Called by the QueryManager for every record being fetched.
	 * @param rsData Current row data in the form of QueryResultData
	 * @return Resultant of processing current row 
	 * @throws SQLException
	 */
	public T digest(QueryResultData rsData) throws SQLException;
	public void finalizeDigester();
}
