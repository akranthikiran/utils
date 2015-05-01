package com.fw.persistence.repository;

import com.fw.persistence.IPersistenceContext;

public class PersistenceExecutionContext
{
	private RepositoryFactory repositoryFactory;
	private IPersistenceContext persistenceContext;
	
	public PersistenceExecutionContext(RepositoryFactory repositoryFactory, IPersistenceContext persistenceContext)
	{
		this.repositoryFactory = repositoryFactory;
		this.persistenceContext = persistenceContext;
	}

	public RepositoryFactory getRepositoryFactory()
	{
		return repositoryFactory;
	}

	public IPersistenceContext getPersistenceContext()
	{
		return persistenceContext;
	}
}
