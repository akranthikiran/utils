package com.fw.persistence;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TransactionWrapper<T extends ITransaction> implements ITransaction
{
	private static Logger logger = LogManager.getLogger(TransactionWrapper.class);
	
	private T transaction;
	private boolean existingTransaction;
	
	private boolean committed = false;
	
	public TransactionWrapper(T transaction, boolean existingTransaction)
	{
		this.transaction = transaction;
		this.existingTransaction = existingTransaction;
	}

	@Override
	public void commit() throws TransactionException
	{
		committed = true;
		
		if(existingTransaction)
		{
			logger.debug("Commit is called on existing transaction. Ignoring commit call: {}", this);
			return;
		}
		
		logger.trace("Commiting transaction: {}", this);
		transaction.commit();
	}

	@Override
	public void rollback() throws TransactionException
	{
		logger.debug("Rolling back transaction: {}", this);
		transaction.rollback();
	}

	@Override
	public void close() throws TransactionException
	{
		if(existingTransaction)
		{
			if(committed)
			{
				logger.trace("Close is called on committed existing transaction. Ignoring commit: {}", this);
				return;
			}
			
			logger.debug("Close is called on un-committed existing transaction. Closing the main transaction: {}", this);
			transaction.close();
			return;
		}

		logger.trace("Closing transaction: {}", this);
		transaction.close();
	}
	
	public boolean isExistingTransaction()
	{
		return existingTransaction;
	}

	public T getTransaction()
	{
		return transaction;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder(super.toString());
		builder.append("[");

		builder.append("Transaction: ").append(transaction);
		builder.append(",").append("Existing: ").append(existingTransaction);

		builder.append("]");
		return builder.toString();
	}
}
