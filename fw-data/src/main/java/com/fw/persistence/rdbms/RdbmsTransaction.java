package com.fw.persistence.rdbms;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fw.persistence.ITransaction;
import com.fw.persistence.TransactionException;

public class RdbmsTransaction implements ITransaction
{
	private static Logger logger = LogManager.getLogger(RdbmsTransaction.class);
	
	private boolean closed = false;
	private boolean committed = false;
	private boolean rolledBack = false;
	
	private RdbmsTransactionManager manager;
	
	
	private Connection connection;
	
	RdbmsTransaction(RdbmsTransactionManager manager, Connection connection)
	{
		this.manager = manager;
		this.connection = connection;
		
		try
		{
			this.connection.setAutoCommit(false);
		}catch(SQLException ex)
		{
			throw new IllegalStateException("An error occurred while marking connection's auto-commit as false", ex);
		}
	}

	@Override
	public void close() throws TransactionException
	{
		if(!rolledBack)
		{
			if(!committed)
			{
				logger.debug("Closed is called on unclosed transaction, rolling back transaction");
				rollback();
				return;
			}
		}
		
		try
		{
			connection.close();
		}catch(SQLException ex)
		{
			throw new TransactionException("Failed to close DB connection", ex);
		}
		
		
		manager.removeTransaction();
		closed =  true;
		logger.trace("Closed transaction");
	}
	
	private void checkIfClosed()
	{
		if(!closed)
		{
			return;
		}
		
		throw new IllegalStateException("Current transaction is already closed");
	}

	@Override
	public void commit() throws TransactionException
	{
		checkIfClosed();
		
		try
		{
			connection.commit();
			committed = true;
			logger.trace("committed transaction");
		}catch(SQLException ex)
		{
			throw new TransactionException("An error occurred while commiting connection", ex);
		}finally
		{
			this.close();
		}
	}

	@Override
	public void rollback() throws TransactionException
	{
		checkIfClosed();
		
		try
		{
			rolledBack = true;
			connection.rollback();
			logger.trace("Rolling back transaction");
		}catch(SQLException ex)
		{
			throw new TransactionException("An error occurred while rolling back connection", ex);
		}finally
		{
			this.close();
		}
	}
	
	public Connection getConnection()
	{
		return connection;
	}
}
