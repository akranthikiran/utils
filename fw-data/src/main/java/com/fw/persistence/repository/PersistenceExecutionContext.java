package com.fw.persistence.repository;

public class PersistenceExecutionContext
{
	private RepositoryFactory repositoryFactory;
	
	public PersistenceExecutionContext(RepositoryFactory repositoryFactory)
	{
		this.repositoryFactory = repositoryFactory;
	}

	public RepositoryFactory getRepositoryFactory()
	{
		return repositoryFactory;
	}
}
