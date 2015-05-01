package com.fw.persistence.rdbms;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fw.persistence.ITransactionManager;
import com.fw.persistence.TransactionException;
import com.fw.persistence.TransactionWrapper;

public class RdbmsTransactionManager implements ITransactionManager<RdbmsTransaction>
{
	private static Logger logger = LogManager.getLogger(RdbmsTransactionManager.class);
	
	private Map<Thread, RdbmsTransaction> threadToTransaction = new HashMap<>();
	
	private DataSource dataSource;
	
	public void setDataSource(DataSource dataSource)
	{
		this.dataSource = dataSource;
	}
	
	private RdbmsTransaction createTransaction() throws TransactionException
	{
		RdbmsTransaction transaction = null;
		
		try
		{
			transaction = new RdbmsTransaction(this, dataSource.getConnection());
			logger.trace("Created new transaction: {}", transaction);
		}catch(SQLException ex)
		{
			throw new TransactionException("An error occurred while opnening new DB connection", ex);
		}
		
		threadToTransaction.put(Thread.currentThread(), transaction);
		return transaction;
	}
	
	@Override
	public RdbmsTransaction newTransaction() throws TransactionException
	{
		RdbmsTransaction transaction = threadToTransaction.get(Thread.currentThread());
		
		if(transaction != null)
		{
			logger.error("A transaction is already in progress by current thread: {}", transaction);
			throw new IllegalStateException("A transaction is already in progress by current thread");
		}
		
		return createTransaction();
	}
	
	@Override
	public RdbmsTransaction currentTransaction() throws TransactionException
	{
		RdbmsTransaction transaction = threadToTransaction.get(Thread.currentThread());
		
		if(transaction != null)
		{
			return transaction;
		}
		
		throw new IllegalStateException("No transaction is started by current thread");
	}
	
	@Override
	public TransactionWrapper<RdbmsTransaction> newOrExistingTransaction() throws TransactionException
	{
		RdbmsTransaction transaction = threadToTransaction.get(Thread.currentThread());
		
		if(transaction != null)
		{
			logger.trace("Using existing transaction: {}", transaction);
			return new TransactionWrapper<RdbmsTransaction>(transaction, true);
		}
		
		return new TransactionWrapper<RdbmsTransaction>(createTransaction(), false);
	}

	void removeTransaction()
	{
		threadToTransaction.remove(Thread.currentThread());
	}

}
