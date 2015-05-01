package com.fw.persistence;

public interface ITransactionManager<T extends ITransaction>
{
	public T newTransaction() throws TransactionException;
	
	public T currentTransaction() throws TransactionException;
	
	public TransactionWrapper<T> newOrExistingTransaction() throws TransactionException;
}
