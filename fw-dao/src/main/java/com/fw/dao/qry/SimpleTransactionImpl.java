package com.fw.dao.qry;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Note: none of the methods of this class are synchronized because a transaction is visible for single thread.
 * @author akkink1
 *
 */
public class SimpleTransactionImpl implements AutoCloseable, Transaction
{
	private static Logger logger = LogManager.getLogger(SimpleTransactionImpl.class);
	private static int NEXT_ID = 1;
	
	private static class DBConnectionWrapper implements DBConnection
	{
		private DBConnection actualConnection;
		
		public DBConnectionWrapper(DBConnection actualConnection)
		{
			if(actualConnection == null)
			{
				throw new NullPointerException("Actual connection can not be null");
			}
			
			this.actualConnection = actualConnection;
		}

		@Override
		public Connection getConnection() throws SQLException
		{
			//TODO: this method should return connection wrapper, which will not be able to commit/rollback
			return actualConnection.getConnection();
		}

		@Override
		public void rollback() throws SQLException
		{
		}

		@Override
		public void commit() throws SQLException
		{
		}

		@Override
		public void close() throws SQLException
		{
		}
	}

	private int id;
	private DBConnectionWrapper connection = null;
	private boolean closed = false;

	private TransactionManager parent;
	private int isolationLevel;
	private ConnectionSource connectionSource;

	private static synchronized int nextId()
	{
		return NEXT_ID++;
	}

	SimpleTransactionImpl(TransactionManager parent, ConnectionSource connectionSource) throws SQLException
	{
		this(parent, connectionSource, Connection.TRANSACTION_REPEATABLE_READ);
	}

	SimpleTransactionImpl(TransactionManager parent, ConnectionSource connectionSource, int level) throws SQLException
	{
		this.id = nextId();

		this.isolationLevel = level;
		this.connectionSource = connectionSource;

		this.parent = parent;
	}

	int getId()
	{
		return id;
	}

	public DBConnection getConnection(String queryName) throws SQLException
	{
		if(closed)
		{
			throw new SQLException("This transaction is already closed.");
		}
		
		if(connection != null)
		{
			return connection;
		}

		this.connection = new DBConnectionWrapper(connectionSource.getConnection());

		Connection con = this.connection.getConnection();
		con.setAutoCommit(false);
		con.setTransactionIsolation(isolationLevel);
		
		return this.connection;
	}

	public void commit()
	{
		if(closed)
		{
			throw new IllegalStateException("Transaction is already closed.");
		}

		try
		{
			if(connection != null)
			{
				connection.actualConnection.commit();
				connection.actualConnection.close();
			}
		}catch(Exception ex)
		{
			logger.error("An error occurred while performing transaction commit.", ex);
			throw new IllegalStateException("Failed to commit db-connection", ex);
		}finally
		{
			closed = true;
			parent.closeTransaction();
		}
	}

	public void rollback()
	{
		if(closed)
		{
			throw new IllegalStateException("Transaction is already closed.");
		}
		
		try
		{
			if(connection != null)
			{
				connection.actualConnection.rollback();
				connection.actualConnection.close();
			}
		}catch(Exception ex)
		{
			logger.error("An error occurred while performing transaction roll-back.", ex);
			throw new IllegalStateException("Failed to rollback db-connection", ex);
		}finally
		{
			closed = true;
			parent.closeTransaction();
		}
	}

	public boolean isClosed()
	{
		return closed;
	}
	
	@Override
	public void close()
	{
		if(closed)
		{
			return;
		}
		
		logger.debug("Closed is invoked on unclosed transaction. Invoking rollback");
		rollback();
	}
}
