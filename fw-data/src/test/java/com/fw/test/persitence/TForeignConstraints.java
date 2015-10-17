package com.fw.test.persitence;

import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.fw.persistence.ICrudRepository;
import com.fw.persistence.repository.RepositoryFactory;
import com.fw.test.persitence.entity.Customer;
import com.fw.test.persitence.entity.CustomerGroup;
import com.fw.test.persitence.entity.Order;
import com.fw.test.persitence.entity.OrderItem;

public class TForeignConstraints extends TestSuiteBase
{
	@AfterMethod
	public void cleanup(ITestResult result)
	{
		Object params[] = result.getParameters();
		RepositoryFactory factory = (RepositoryFactory)params[0];
		
		//cleanup the emp table
		factory.dropRepository(OrderItem.class);
		factory.dropRepository(Order.class);
		factory.dropRepository(Customer.class);
		factory.dropRepository(CustomerGroup.class);
	}

	@Test(dataProvider = "repositoryFactories")
	public void testCreateTables(RepositoryFactory factory)
	{
		ICrudRepository<OrderItem> repo = factory.getRepositoryForEntity(OrderItem.class);
		Assert.assertNotNull(repo);
	}
}
