package com.fw.test.persitence;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;

import com.fw.persistence.repository.RepositoryFactory;
import com.fw.test.persitence.config.TestConfiguration;

/**
 * Base class for test class. This initializes the required data sources and factories for 
 * test classes.
 * @author akiran
 */
public class TestSuiteBase 
{
	private List<Object[]> factories = new ArrayList<>();
	
	/**
	 * Test NG data provider method to provide data stores and factories
	 * @return
	 */
	@DataProvider(name = "repositoryFactories")
	public Object[][] getDataStores()
	{
		return factories.toArray(new Object[0][]);
	}
	
	/**
	 * Testng before-class method to load required configurations for different data stores
	 */
	@BeforeClass
	public void init()
	{
		//loop through configured data sources
		for(RepositoryFactory factory : TestConfiguration.getTestConfiguration().getRepositoryFactories())
		{
			factories.add(new Object[] {factory});
		}
	}
}
