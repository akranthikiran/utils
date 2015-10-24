package com.fw.test.persitence;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.AfterClass;
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
	protected List<Object[]> factories = new ArrayList<>();
	
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
	public void initFactories()
	{
		//loop through configured data sources
		for(RepositoryFactory factory : TestConfiguration.getTestConfiguration().getRepositoryFactories())
		{
			factories.add(new Object[] {factory});
			initFactoryBeforeClass(factory);
		}
	}
	
	@AfterClass
	public void cleanupFactories()
	{
		RepositoryFactory factory  = null;
		
		for(Object data[] : this.factories)
		{
			try
			{
				factory = (RepositoryFactory)data[0];

				this.cleanFactoryAfterClass(factory);
			}catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}

	}
	
	protected void initFactoryBeforeClass(RepositoryFactory factory)
	{}
	
	protected void cleanFactoryAfterClass(RepositoryFactory factory)
	{}
}
