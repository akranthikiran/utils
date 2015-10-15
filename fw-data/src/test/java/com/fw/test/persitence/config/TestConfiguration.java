package com.fw.test.persitence.config;

import java.util.ArrayList;
import java.util.List;

import com.fw.ccg.xml.XMLBeanParser;
import com.fw.persistence.repository.RepositoryFactory;

/**
 * Provides data source configuration for test cases
 * 
 * @author akiran
 */
public class TestConfiguration
{
	private static TestConfiguration instance = null;

	private List<RepositoryFactory> repositoryFactories = new ArrayList<>();

	private TestConfiguration()
	{}

	public static synchronized TestConfiguration getTestConfiguration()
	{
		if(instance == null)
		{
			instance = new TestConfiguration();
			XMLBeanParser.parse(TestConfiguration.class.getResourceAsStream("/test-configuration.xml"), instance);
		}

		return instance;
	}

	/**
	 * Adds value to {@link #repositoryFactories repositoryFactories}
	 *
	 * @param repositoryFactory
	 *            repositoryFactory to be added
	 */
	public void addRepositoryFactory(RepositoryFactory repositoryFactory)
	{
		if(repositoryFactories == null)
		{
			repositoryFactories = new ArrayList<RepositoryFactory>();
		}

		repositoryFactories.add(repositoryFactory);
	}

	/**
	 * Fetches configured repository factories
	 * @return
	 */
	public List<RepositoryFactory> getRepositoryFactories()
	{
		return repositoryFactories;
	}
}
