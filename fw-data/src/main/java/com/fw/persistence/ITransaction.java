package com.fw.persistence;

public interface ITransaction extends AutoCloseable
{
	public void commit() throws TransactionException;
	
	public void rollback() throws TransactionException;
	
	public void close() throws TransactionException;
}
