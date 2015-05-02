package com.fw.dao.qry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fw.dao.qry.impl.MapQueryFilter;
import com.fw.dao.qry.impl.Record;
import com.fw.dao.qry.impl.RecordDataDigester;

/**
 * This class is the heart of Magic DAO. This the means by which the queries will be executed.
 */
public class QueryManager
{
	private static ThreadLocal<QueryManagerInstance> threadLocal = new ThreadLocal<QueryManagerInstance>();
	
	private static class QueryManagerInstance
	{
		private QueryManager queryManager;
		private int count;
		
		public QueryManagerInstance(QueryManager queryManager, int count)
		{
			this.queryManager = queryManager;
			this.count = count;
		}
	}

	private static Logger logger = LogManager.getLogger(QueryManager.class);
	private static final QueryFilter DUMMY_FILTER = new MapQueryFilter(new HashMap<String, Object>());

	private QuerySource querySource;
	private TransactionManager transactionManager;
	

	/**
	 * Creates QueryManager instance using specified query-source
	 * @param qrySource Query source to be used
	 */
	public QueryManager(QuerySource qrySource, TransactionManager transactionManager)
	{
		this.querySource = qrySource;
		this.transactionManager = transactionManager;
	}
	
	public static QueryManager getQueryManager()
	{
		QueryManagerInstance instance = threadLocal.get();
		
		if(instance != null)
		{
			return instance.queryManager;
		}
		
		return null;
	}
	
	public QuerySource getQuerySource()
	{
		return querySource;
	}
	
	private void setOnThreadLocal()
	{
		QueryManagerInstance instance = threadLocal.get();
		
		if(instance == null)
		{
			instance = new QueryManagerInstance(this, 1);
			threadLocal.set(instance);
			return;
		}
		
		instance.count++;
	}
	
	private void clearThreadLocal()
	{
		QueryManagerInstance instance = threadLocal.get();
		
		if(instance != null)
		{
			instance.count--;
			
			if(instance.count <= 0)
			{
				threadLocal.remove();
			}
		}
	}

	public Transaction newTransaction() throws SQLException
	{
		return newTransaction(Connection.TRANSACTION_REPEATABLE_READ);
	}

	public Transaction newOrExistingTransaction() throws SQLException
	{
		return newOrExistingTransaction(Connection.TRANSACTION_REPEATABLE_READ);
	}

	public Transaction newTransaction(int level) throws SQLException
	{
		return transactionManager.newTransaction(level, querySource.getConnectionSource());
	}
	
	public Transaction newOrExistingTransaction(int level) throws SQLException
	{
		return transactionManager.newOrExistingTransaction(level, querySource.getConnectionSource());
	}

	public boolean isTransactionActive()
	{
		return transactionManager.isTransactionActive();
	}

	public Transaction currentTransaction() throws SQLException
	{
		return transactionManager.currentTransaction(querySource.getConnectionSource());
	}

	public Object getGlobalProperty(String name)
	{
		return querySource.getGlobalProperty(name);
	}

	public synchronized DBConnection getDBConnection(String queryName) throws SQLException
	{
		if(isTransactionActive())
		{
			return currentTransaction().getConnection(queryName);
		}
		
		ConnectionSource connectionSource = querySource.getConnectionSource();

		if(connectionSource == null)
			throw new IllegalStateException("Failed to fetch connection source for query: " + queryName);

		return connectionSource.getConnection();
	}

	protected void commitConnection(DBConnection con) throws SQLException
	{
		con.commit();
	}

	protected void closeResources(DBConnection con, Statement stmt, ResultSet rs)
	{
		try
		{
			if(rs != null)
				rs.close();

			if(stmt != null)
				stmt.close();

			if(con != null)
				con.close();

		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

	/**
	 * Equivalent to {@link #getQuery(String, QueryFilter) getQuery(name,DUMMY_FILTER)} 
	 * @param name
	 * @return
	 */
	public String getQuery(String name)
	{
		setOnThreadLocal();
		
		try
		{
			return getQuery(name, null, null);
		}finally
		{
			clearThreadLocal();
		}
	}
	
	public String getQueryParam(String queryName, String param)
	{
		Query query = getQueryObject(queryName);
		
		if(query == null)
		{
			return null;
		}
		
		return query.getParam(param);
	}

	/**
	 * Fetches the query with specified "name" after filtering the query contents using
	 * specified filter.
	 * 
	 * @param name Name of the query
	 * @param filter Query filter to be used
	 * @return Returns filtered contents of the query (without property values).
	 */
	public String getQuery(String name, QueryFilter filter)
	{
		setOnThreadLocal();
		
		try
		{
			return getQuery(name, filter, null);
		}finally
		{
			clearThreadLocal();
		}
	}

	/**
	 * <P>Fetches the query with specified "name" after filtering the query contents using
	 * specified filter.</P>
	 * 
	 * <P>This method will build query by replacing replaceable-params with "?" wherever applicable.
	 * And will populate "finalParams" with the values that needs to be passed as parameters to 
	 * prepared statement built using the returned query.</P>
	 * 
	 *  <P><B>Note: </B>Parameters (passed using "params" and resulted due to replaceable-params) whose value is null
	 *  will not be included into "finalParams" instead null-string (defined by query filter) will be substituted in 
	 *  the param place</P>
	 *  
	 * @param name Name of the query
	 * @param filter Query filter to be used
	 * @param finalParams List which will be populated with the parameter values
	 * @param params Parameter values that needs to be used for "?" parameters in the query
	 * @return Query string after filtering, required value/null substitution.
	 */
	public String getQuery(String name, QueryFilter filter, List<Object> finalParams, Object... params)
	{
		if(filter == null)
			filter = DUMMY_FILTER;

		setOnThreadLocal();
		
		try
		{
			Query query = querySource.getQuery(name);

			if(query == null)
				throw new NoSuchQueryException("No query exists with specified name: " + name);

			return query.toText(filter, finalParams, params);
		}catch(Exception ex)
		{
			throw new IllegalStateException("An error occured while building query with name: " + name, ex);
		}finally
		{
			clearThreadLocal();
		}
	}
	
	public boolean hasQuery(String name)
	{
		return querySource.hasQuery(name);
	}

	private Query getQueryObject(String name)
	{
		Query query = querySource.getQuery(name);

		if(query == null)
			throw new IllegalStateException("No query is configured with specified name: " + name);

		return query;
	}

	/**
	 * This is equivalent to calling 
	 * {@link #buildStatement(String, Connection, QueryFilter, Object...) buildStatement(name,connection,DUMMY_FILTER,params)}
	 * 
	 * @param name Query name
	 * @param connection SQL Connection on which prepared statement needs to be built
	 * @param params Parameters for the prepared statement parameters (?)
	 * @return Prepared statement build using specified query and connection
	 * @throws SQLException
	 */
	public PreparedStatement buildStatement(String name, DBConnection connection, Object... params) throws SQLException
	{
		setOnThreadLocal();
		
		try
		{
			return buildStatement(name, connection, DUMMY_FILTER, params);
		}finally
		{
			clearThreadLocal();
		}
	}

	/**
	 * Uses {@link #getQuery(String, QueryFilter, List, Object...) getQuery(name,filter,finalParams,params)} to fetch query string, 
	 * builds the prepared statement and then sets the required parameters. Then the prepared statement is returned.
	 * 
	 * @param name Query name
	 * @param connection SQL Connection on which prepared statement needs to be built
	 * @param filter Query filter to be used
	 * @param params Parameters for the prepared statement parameters (?)
	 * @return Prepared statement after setting required parameter values
	 * @throws SQLException
	 */
	public PreparedStatement buildStatement(String name, DBConnection dbConnection, QueryFilter filter, Object... params) throws SQLException
	{
		setOnThreadLocal();
		
		try
		{
			LinkedList<Object> finalParams = new LinkedList<Object>();
			Connection connection = dbConnection.getConnection();
	
			String qryStr = getQuery(name, filter, finalParams, params);
	
			PreparedStatement pstmt = null;
	
			try
			{
				pstmt = connection.prepareStatement(qryStr);
			}catch(SQLException ex)
			{
				logger.error("An error occured while building statement with below query: \n" + "Query Name: " + name + "\nQuery: " + qryStr);
				throw ex;
			}
	
			if(!finalParams.isEmpty())
			{
				if(logger.isDebugEnabled())
				{
					logger.debug("Building query: " + name 
							+ "\nWith Connection: " + dbConnection
							+ "\nFilter: " + filter
							+ "\nWith Params: " + finalParams 
							+ "\n**********************************************\nQuery Is:" 
							+ "\n" + qryStr 
							+ "\n**********************************************");
				}
	
				int idx = 1;
	
				for(Object param : finalParams)
				{
					pstmt.setObject(idx++, param);
				}
			}
			else if(logger.isDebugEnabled())
			{
				logger.debug("Building query: " + name 
						+ "\nWith Connection: " + dbConnection 
						+ "\n**********************************************\nQuery Is:" 
						+ "\n" + qryStr 
						+ "\n**********************************************");
			}
	
			return pstmt;
		}finally
		{
			clearThreadLocal();
		}
	}

	/**
	 * This is equivalent to calling 
	 * 		{@link #executeUpdate(String, QueryFilter, ConnectionSource, boolean, Object...) executeUpdate(name,DUMMY_FILTER,null,true,params)}
	 * 
	 * @param name
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public int executeUpdate(String name, Object... params) throws SQLException
	{
		return executeUpdate(name, DUMMY_FILTER, params);
	}

	/**
	 * This is equivalent to calling 
	 * 		{@link #executeUpdate(String, QueryFilter, ConnectionSource, boolean, Object...) executeUpdate(name,filter,null,true,params)}
	 * 
	 * @param name
	 * @param filter
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public int executeUpdate(String name, QueryFilter filter, Object... params) throws SQLException
	{
		DBConnection connection = getDBConnection(name);
		PreparedStatement pstmt = null;

		setOnThreadLocal();
		
		try
		{
			pstmt = buildStatement(name, connection, filter, params);
			int res = pstmt.executeUpdate();

			commitConnection(connection);
			return res;
		}catch(Exception ex)
		{
			throw new SQLException("An error occured while executing query: " + name, ex);
		}finally
		{
			clearThreadLocal();
			closeResources(connection, pstmt, null);
		}
	}

	public Integer[] executeBulkUpdates(String name, BulkQueryFilter filter) throws SQLException
	{
		DBConnection dbConnection = getDBConnection(name);
		Connection connection = dbConnection.getConnection();
		PreparedStatement pstmt = null;

		Query.QueryResult qryResult = null;
		String qryStr = null;

		setOnThreadLocal();
		
		try
		{
			try
			{
				Query query = querySource.getQuery(name);
				qryResult = query.buildBulkQuery(filter);
				qryStr = qryResult.toString(filter);
			}catch(Exception ex)
			{
				throw new IllegalStateException("An error occured while building bulk query with name: " + name, ex);
			}
	
			try
			{
				pstmt = connection.prepareStatement(qryStr);
			}catch(SQLException ex)
			{
				logger.error("An error occured while building statement with below query: \n" + "Query Name: " + name + "\nQuery: " + qryStr);
	
				throw ex;
			}
	
			if(logger.isDebugEnabled())
			{
				logger.debug("Building query (for bulk update): " + name 
						+ "\nWith Connection: " + dbConnection 
						+ "\n**********************************************\nQuery Is (for bulk update):" + "\n" + qryStr 
						+ "\n**********************************************");
			}
	
			List<Object> finalParams = new ArrayList<Object>(20);
			int idx = 1;
			List<Integer> updCounts = new LinkedList<Integer>();
			int commitCount = filter.getCommitCount(), count = 0;
	
			try
			{
				while(filter.next())
				{
					finalParams.clear();
	
					try
					{
						qryResult.populateParams(filter, finalParams);
					}catch(Exception ex)
					{
						throw new IllegalStateException("An error occured while executing query: " + name, ex);
					}
	
					idx = 1;
	
					if(logger.isDebugEnabled())
						logger.debug("Executing bulk query with: " + finalParams);
	
					for(Object obj : finalParams)
					{
						if(obj instanceof SQLNull)
						{
							pstmt.setNull(idx, ((SQLNull)obj).getType());
						}
						else
						{
							pstmt.setObject(idx, obj);
						}
	
						idx++;
					}
	
					updCounts.add(pstmt.executeUpdate());
					pstmt.clearParameters();
					count++;
	
					if(commitCount > 0 && count >= commitCount)
					{
						commitConnection(dbConnection);
						count = 0;
					}
				}

				commitConnection(dbConnection);
			}finally
			{
				closeResources(dbConnection, pstmt, null);
			}
	
			return updCounts.toArray(new Integer[0]);
		}finally
		{
			clearThreadLocal();
		}
	}

	public boolean executeDDL(String name, QueryFilter filter) throws SQLException
	{
		DBConnection connection = getDBConnection(name);
		Statement stmt = null;

		setOnThreadLocal();
		
		try
		{
			String qryStr = getQuery(name, filter, null);

			if(logger.isDebugEnabled())
			{
				logger.debug("Building query (for bulk update): " + name 
						+ "\nWith Connection: " + connection 
						+ "\n**********************************************\nQuery Is:" + "\n" + qryStr 
						+ "\n**********************************************");
			}

			stmt = connection.getConnection().createStatement();
			return stmt.execute(qryStr);
		}catch(Exception ex)
		{
			throw new SQLException("An error occured while executing DDL query: " + name, ex);
		}finally
		{
			clearThreadLocal();
			closeResources(connection, stmt, null);
		}
	}

	public int fetchInt(String name, Object... params) throws SQLException
	{
		return fetchInt(name, DUMMY_FILTER, -1, params);
	}

	public int fetchInt(String name, QueryFilter filter, Object... params) throws SQLException
	{
		return fetchInt(name, filter, -1, params);
	}

	public int fetchInt(String name, QueryFilter filter, int defValue, Object... params) throws SQLException
	{
		DBConnection connection = getDBConnection(name);
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		setOnThreadLocal();
		
		try
		{
			pstmt = buildStatement(name, connection, filter, params);
			rs = pstmt.executeQuery();

			if(!rs.next())
				return defValue;

			return rs.getInt(1);
		}catch(Exception ex)
		{
			throw new SQLException("An error occured while executing query: " + name, ex);
		}finally
		{
			clearThreadLocal();
			closeResources(connection, pstmt, rs);
		}
	}

	public long fetchLong(String name, QueryFilter filter, long defValue, Object... params) throws SQLException
	{
		DBConnection connection = getDBConnection(name);
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		setOnThreadLocal();
		
		try
		{
			pstmt = buildStatement(name, connection, filter, params);
			rs = pstmt.executeQuery();

			if(!rs.next())
				return defValue;

			return rs.getLong(1);
		}catch(Exception ex)
		{
			throw new SQLException("An error occured while executing query: " + name, ex);
		}finally
		{
			clearThreadLocal();
			closeResources(connection, pstmt, rs);
		}
	}

	public String fetchString(String name, Object... params) throws SQLException
	{
		return fetchString(name, DUMMY_FILTER, params);
	}
	
	public String fetchString(String name, QueryFilter filter, Object... params) throws SQLException
	{
		DBConnection connection = getDBConnection(name);
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		setOnThreadLocal();
		
		try
		{
			pstmt = buildStatement(name, connection, filter, params);
			rs = pstmt.executeQuery();

			if(!rs.next())
				return null;

			return rs.getString(1);
		}catch(Exception ex)
		{
			throw new SQLException("An error occured while executing query: " + name, ex);
		}finally
		{
			clearThreadLocal();
			closeResources(connection, pstmt, rs);
		}
	}

	
	public Object fetchObject(String name, Object... params) throws SQLException
	{
		return fetchObject(name, DUMMY_FILTER, params);
	}

	public Object fetchObject(String name, QueryFilter filter, Object... params) throws SQLException
	{
		List<Object> resLst = fetchSingleColumnList(name, filter, 1, params);

		if(resLst == null || resLst.isEmpty())
			return null;

		return resLst.get(0);
	}

	public List<Object> fetchSingleColumnList(String name, Object... params) throws SQLException
	{
		return fetchSingleColumnList(name, DUMMY_FILTER, Integer.MAX_VALUE, params);
	}

	public List<Object> fetchSingleColumnList(String name, QueryFilter filter, Object... params) throws SQLException
	{
		return fetchSingleColumnList(name, filter, Integer.MAX_VALUE, params);
	}

	public List<Object> fetchSingleColumnList(String name, QueryFilter filter, int limit, Object... params) throws SQLException
	{
		DBConnection connection = getDBConnection(name);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		LinkedList<Object> records = new LinkedList<Object>();

		setOnThreadLocal();
		
		try
		{
			pstmt = buildStatement(name, connection, filter, params);
			rs = pstmt.executeQuery();

			String colName = rs.getMetaData().getColumnLabel(1);
			Query query = getQueryObject(name);
			FunctionInstance func = query.getColumnExpression(colName);
			Object value = null;
			QueryResultDataProvider dataProvider = (func == null)? null: new QueryResultDataProvider(new QueryResultData(query, filter, rs));
			int recNo = 0;

			while(rs.next())
			{
				if(func != null)
				{
					value = func.invoke(dataProvider);
					records.add(value);
				}
				else
					records.add(rs.getObject(1));

				recNo++;

				if(recNo >= limit)
					break;
			}

			return records;
		}catch(Exception ex)
		{
			throw new SQLException("An error occured while executing query: " + name, ex);
		}finally
		{
			clearThreadLocal();
			closeResources(connection, pstmt, rs);
		}
	}

	public Map<Object, Object> fetchMap(String name, Object... params) throws SQLException
	{
		return fetchMap(name, DUMMY_FILTER, null, null, params);
	}

	public Map<Object, Object> fetchMap(String name, QueryFilter filter, Object... params) throws SQLException
	{
		return fetchMap(name, filter, null, null, params);
	}

	public Map<Object, Object> fetchMap(String name, QueryFilter filter, String keyCol, String valCol, Object... params) throws SQLException
	{
		DBConnection connection = getDBConnection(name);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Map<Object, Object> resMap = new HashMap<Object, Object>();

		setOnThreadLocal();
		
		try
		{
			Query query = getQueryObject(name);

			pstmt = buildStatement(name, connection, filter, params);
			rs = pstmt.executeQuery();

			if(keyCol == null || valCol == null)
			{
				keyCol = rs.getMetaData().getColumnLabel(1);
				valCol = rs.getMetaData().getColumnLabel(2);
			}

			FunctionInstance keyFunc = query.getColumnExpression(keyCol);
			FunctionInstance valFunc = query.getColumnExpression(valCol);

			Object key = null, value = null;
			QueryResultDataProvider dataProvider = (keyFunc == null && valFunc == null)? null: new QueryResultDataProvider(new QueryResultData(query, filter, rs));

			while(rs.next())
			{
				key = (keyFunc == null)? rs.getObject(keyCol): keyFunc.invoke(dataProvider);
				value = (valFunc == null)? rs.getObject(valCol): valFunc.invoke(dataProvider);

				resMap.put(key, value);
			}

			return resMap;
		}catch(Exception ex)
		{
			throw new SQLException("An error occured while executing query: " + name, ex);
		}finally
		{
			clearThreadLocal();
			closeResources(connection, pstmt, rs);
		}
	}

	public <T>List<T> fetchBeans(String name, DataDigester<T> digester, Object... params) throws SQLException
	{
		return fetchBeans(name, DUMMY_FILTER, digester, params);
	}

	public <T>List<T> fetchBeans(String name, QueryFilter filter, Object... params) throws SQLException
	{
		return fetchBeans(name, filter, null, params);
	}

	public <T>List<T> fetchBeans(String name, Object... params) throws SQLException
	{
		return fetchBeans(name, DUMMY_FILTER, null, params);
	}

	@SuppressWarnings("unchecked")
	public <T>List<T> fetchBeans(String name, QueryFilter filter, DataDigester<T> digester, Object... params) throws SQLException
	{
		Query query = getQueryObject(name);

		if(digester == null)
			digester = (DataDigester<T>)querySource.getDataDigester(query);

		if(digester == null)
			throw new IllegalStateException("No data digester is specified.");

		DBConnection connection = getDBConnection(name);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		QueryResultData rsData = null;
		LinkedList<T> records = new LinkedList<T>();

		setOnThreadLocal();
		
		try
		{
			pstmt = buildStatement(name, connection, filter, params);
			rs = pstmt.executeQuery();

			if(!rs.next())
				return null;

			rsData = new QueryResultData(query, filter, rs);

			T bean = null;

			do
			{
				bean = digester.digest(rsData);

				if(bean != null)
					records.add(bean);

				if(rsData.getStopProcessing())
					break;

			}while(rs.next());

			digester.finalizeDigester();
			return records;
		}catch(Exception ex)
		{
			throw new SQLException("An error occured while executing query: " + name, ex);
		}finally
		{
			clearThreadLocal();
			closeResources(connection, pstmt, rs);
		}
	}

	public <T>List<T> executeFetch(String name, DataDigester<T> digester, Object... params) throws SQLException
	{
		return fetchBeans(name, DUMMY_FILTER, digester, params);
	}

	public void executeFetch(String name, QueryFilter filter, DataDigester<?> digester, Object... params) throws SQLException
	{
		Query query = getQueryObject(name);

		if(digester == null)
			digester = (DataDigester<?>)querySource.getDataDigester(query);

		if(digester == null)
			throw new IllegalStateException("No data digester is specified.");

		DBConnection connection = getDBConnection(name);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		QueryResultData rsData = null;

		setOnThreadLocal();
		
		try
		{
			pstmt = buildStatement(name, connection, filter, params);
			rs = pstmt.executeQuery();

			if(rs.next())
				rsData = new QueryResultData(query, filter, rs);
			else
				return;

			do
			{
				digester.digest(rsData);

				if(rsData.getStopProcessing())
					break;

			}while(rs.next());

			digester.finalizeDigester();
		}catch(Exception ex)
		{
			throw new SQLException("An error occured while executing query: " + name, ex);
		}finally
		{
			clearThreadLocal();
			closeResources(connection, pstmt, rs);
		}
	}

	public Object fetchBean(String name, Object... params) throws SQLException
	{
		return fetchBean(name, DUMMY_FILTER, null, params);
	}

	public <T>T fetchBean(String name, DataDigester<T> digester, Object... params) throws SQLException
	{
		return fetchBean(name, DUMMY_FILTER, digester, params);
	}

	public Object fetchBean(String name, QueryFilter filter, Object... params) throws SQLException
	{
		return fetchBean(name, filter, null, params);
	}

	@SuppressWarnings("unchecked")
	public <T>T fetchBean(String name, QueryFilter filter, DataDigester<T> digester, Object... params) throws SQLException
	{
		Query query = getQueryObject(name);

		if(digester == null)
			digester = (DataDigester<T>)querySource.getDataDigester(query);

		if(digester == null)
			throw new IllegalStateException("No data digester is specified.");

		DBConnection connection = getDBConnection(name);
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		setOnThreadLocal();
		
		try
		{
			pstmt = buildStatement(name, connection, filter, params);
			rs = pstmt.executeQuery();

			if(!rs.next())
				return null;

			QueryResultData rsData = new QueryResultData(query, filter, rs);

			T data = digester.digest(rsData);
			digester.finalizeDigester();
			return data;
		}catch(Exception ex)
		{
			throw new SQLException("An error occured while executing query: " + name, ex);
		}finally
		{
			clearThreadLocal();
			closeResources(connection, pstmt, rs);
		}
	}

	public Record fetchRecord(String name, Object... params) throws SQLException
	{
		return fetchRecord(name, DUMMY_FILTER, params);
	}

	public Record fetchRecord(String name, QueryFilter filter, Object... params) throws SQLException
	{
		return fetchBean(name, filter, new RecordDataDigester(), params);
	}

	public List<Record> fetchRecords(String name, Object... params) throws SQLException
	{
		return fetchRecords(name, DUMMY_FILTER, params);
	}

	public List<Record> fetchRecords(String name, QueryFilter filter, Object... params) throws SQLException
	{
		return fetchBeans(name, filter, new RecordDataDigester(), params);
	}

}
