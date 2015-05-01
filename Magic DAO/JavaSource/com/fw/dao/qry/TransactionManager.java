package com.fw.dao.qry;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TransactionManager
{
	private static Map<String, TransactionManager> nameToManager = new HashMap<>();
	private static Logger logger = LogManager.getLogger(TransactionManager.class);
	
	/**
	 * Note: none of the methods of this class are synchronized because a transaction is visible for single thread.
	 * @author akkink1
	 *
	 */
	private class TransactionWrapper implements Transaction
	{
		private Transaction transaction;
		private TransactionWrapper parentTransaction;
		
		private List<TransactionWrapper> childTransactions;
		private Stack<TransactionWrapper> transactionStack;
		
		private ConnectionSource connectionSource;
		
		private boolean commitInvoked = false, removedFromStack = false;
		private boolean closed = false;
		
		public TransactionWrapper(Transaction transaction, TransactionWrapper parentTransaction, ConnectionSource connectionSource)
		{
			this.transaction = transaction;
			this.parentTransaction = parentTransaction;
			this.connectionSource = connectionSource;
			
			if(parentTransaction != null)
			{
				parentTransaction.addChildTransaction(this);
			}
		}
		
		private void addChildTransaction(TransactionWrapper transactionWrapper)
		{
			if(closed)
			{
				throw new IllegalStateException("Child transaction is getting added on closed transaction");
			}
			
			if(parentTransaction != null)
			{
				throw new IllegalStateException("Tried to add child transaction to child transaction");
			}
			
			if(childTransactions == null)
			{
				childTransactions = new ArrayList<>(); 
				transactionStack = new Stack<>();
			}
			
			childTransactions.add(transactionWrapper);
			transactionStack.add(transactionWrapper);
		}
		
		@Override
		public void commit()
		{
			if(closed)
			{
				throw new IllegalStateException("Commit is called on closed transaction");
			}
			
			//if this is parent transaction
			if(childTransactions != null)
			{
				//for each child transaction, invoke commit on wrapped transaction
				for(TransactionWrapper transWrapper: childTransactions)
				{
					if(transWrapper.transaction.isClosed())
					{
						continue;
					}
					
					transWrapper.transaction.commit();
				}
				
				if(!transaction.isClosed())
				{
					transaction.commit();
				}
				
				closed = true;
				logger.debug("Commit is invoked on parent transaction. This and all child transactions are commited.");
				return;
			}
			
			if(parentTransaction == null)
			{
				transaction.commit();
				closed = true;
				return;
			}
			
			commitInvoked = true;
			closed = true;
			logger.debug("Commit is invoked on child transaction. Commit will wait till parent transaction is commited");
		}

		@Override
		public void rollback()
		{
			if(closed)
			{
				throw new IllegalStateException("Rollback is called on closed transaction");
			}
			
			//if this is parent transaction
			if(childTransactions != null)
			{
				//for each child transaction, invoke rollback on wrapped transaction
				for(TransactionWrapper transWrapper: childTransactions)
				{
					if(transWrapper.transaction.isClosed())
					{
						continue;
					}
					
					transWrapper.transaction.rollback();
				}
				
				//rollback actual transaction
				if(!transaction.isClosed())
				{
					transaction.rollback();
				}
				
				closed = true;
				return;
			}
			
			//if this is a simple transaction, simply rollback 
			if(parentTransaction == null)
			{
				transaction.rollback();
				closed = true;
				return;
			}
			
			//if this is a child transaction, invoke rollback on parent transaction
			logger.debug("Rollback is called on child transaction, rolling back all transactions on current thread");
			parentTransaction.rollback();
			closed = true;
		}

		@Override
		public void close()
		{
			if(!removedFromStack)
			{
				if(parentTransaction != null)
				{
					parentTransaction.transactionStack.pop();
				}
				
				removedFromStack = true;
			}
			
			if(commitInvoked || closed)
			{
				return;
			}
			
			logger.debug("Close is invoked on un-commited transaction. Rolling back all active transactions on current thread");
			this.rollback();
		}
		
		@Override
		public boolean isClosed()
		{
			return closed;
		}

		@Override
		public DBConnection getConnection(String queryName) throws SQLException
		{
			if(closed)
			{
				throw new IllegalStateException("getConnection() is invoked on closed transaction");
			}
			
			return transaction.getConnection(queryName);
		}
		
		public TransactionWrapper getActiveTransaction()
		{
			if(closed)
			{
				throw new IllegalStateException("getActiveTransaction() is invoked on closed transaction");
			}
			
			if(transactionStack == null || transactionStack.isEmpty())
			{
				return this;
			}
			
			return transactionStack.peek();
		}
		
		/**
		 * If a transaction with specified connection source is already present, use same transaction wrapped with new wrapper. 
		 * So that changes are visible across the application running 
		 * on same thread
		 * 
		 * @param connectionSource
		 * @return
		 * @throws SQLException
		 */
		public TransactionWrapper newChildTransaction(ConnectionSource connectionSource) throws SQLException
		{
			if(closed)
			{
				throw new IllegalStateException("Trying to create child transaction with closed transaction");
			}
			
			if(this.connectionSource == connectionSource)
			{
				return new TransactionWrapper(this.transaction, this, connectionSource);
			}
			
			if(childTransactions != null)
			{
				for(TransactionWrapper childTrans: this.childTransactions)
				{
					if(childTrans.connectionSource == connectionSource)
					{
						return new TransactionWrapper(childTrans.transaction, this, connectionSource);
					}
				}
			}
		
			logger.debug("Creating new child-transaction as no transaction exists with specified connection source: " + connectionSource);
			
			Transaction trans = new SimpleTransactionImpl(TransactionManager.this, connectionSource, Connection.TRANSACTION_REPEATABLE_READ);
			TransactionWrapper wrapperTrans = new TransactionWrapper(trans, this, connectionSource);
			
			return wrapperTrans;
		}
		
	}
	
	private String name;
	private HashMap<Thread, TransactionWrapper> keyToTrans = new HashMap<>();
	
	private TransactionManager(String name)
	{
		this.name = name;
	}
	
	public String getName()
	{
		return name;
	}

	public static synchronized TransactionManager getTransactionManager(String name)
	{
		if(name == null || name.trim().length() == 0)
		{
			throw new NullPointerException("Name can not be null or empty");
		}
		
		TransactionManager manager = nameToManager.get(name);
		
		if(manager != null)
		{
			return manager;
		}
		
		manager = new TransactionManager(name);
		nameToManager.put(name, manager);
		
		return manager;
	}
	
	public Transaction newTransaction(int level, ConnectionSource connectionSource) throws SQLException
	{
		Thread currentThread = Thread.currentThread();
		
		if(keyToTrans.containsKey(currentThread))
		{
			throw new IllegalStateException("A trasaction is already active by current thread");
		}
	
		Transaction trans = new SimpleTransactionImpl(this, connectionSource, level);
		TransactionWrapper wrapperTrans = new TransactionWrapper(trans, null, connectionSource);
		
		keyToTrans.put(currentThread, wrapperTrans);
		return wrapperTrans;
	}

	public Transaction newOrExistingTransaction(int level, ConnectionSource connectionSource) throws SQLException
	{
		Thread currentThread = Thread.currentThread();
		TransactionWrapper parentTransaction = keyToTrans.get(currentThread);
		
		TransactionWrapper wrapperTrans = null;

		//if parent transaction is not present then create new transaction and this will be parent for
		//		new transactions on same thread
		if(parentTransaction == null)
		{
			logger.debug("Creating new transaction as no transaction existing on thred");
			
			Transaction trans = new SimpleTransactionImpl(this, connectionSource, level);
			wrapperTrans = new TransactionWrapper(trans, null, connectionSource);
			
			keyToTrans.put(currentThread, wrapperTrans);
		}
		else
		{
			//if parent transaction is present, create child transaction wrapper with existing or new connection based on connection source
			wrapperTrans = parentTransaction.newChildTransaction(connectionSource);
		}
		
		return wrapperTrans;
	}

	public boolean isTransactionActive()
	{
		return keyToTrans.containsKey(Thread.currentThread());
	}

	public Transaction currentTransaction(ConnectionSource connectionSource) throws SQLException
	{
		TransactionWrapper trans = keyToTrans.get(Thread.currentThread());

		if(trans == null)
		{
			throw new IllegalStateException("No active transaction by current thread");
		}

		return trans.getActiveTransaction();
	}

	void closeTransaction()
	{
		keyToTrans.remove(Thread.currentThread());
	}

	
}
