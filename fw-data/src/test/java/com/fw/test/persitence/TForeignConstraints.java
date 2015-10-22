package com.fw.test.persitence;

import java.util.Arrays;

import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.fw.persistence.ICrudRepository;
import com.fw.persistence.GenericRepository;
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

	/**
	 * Tests tables are created as per expectations when foreign keys are involved
	 * @param factory
	 */
	@Test(dataProvider = "repositoryFactories")
	public void testCreateTables(RepositoryFactory factory)
	{
		ICrudRepository<OrderItem> repo = factory.getRepositoryForEntity(OrderItem.class);
		Assert.assertNotNull(repo);
		
		//TODO: Validate create tables structure
	}
	
	@Test(dataProvider = "repositoryFactories")
	public void testRecursiveInsertion(RepositoryFactory factory)
	{
		GenericRepository genericRepository = new GenericRepository(factory);
		
		CustomerGroup group1 = new CustomerGroup("Group1", null);
		CustomerGroup group2 = new CustomerGroup("Group2", null);
		CustomerGroup group3 = new CustomerGroup("Group3", null);

		genericRepository.save(group1);
		genericRepository.save(group2);
		genericRepository.save(group3);
		
		OrderItem item11 = new OrderItem("soap", 10, null);
		OrderItem item12 = new OrderItem("box", 20, null);
		
		OrderItem item21 = new OrderItem("soap", 10, null);
		OrderItem item22 = new OrderItem("brush", 3, null);
		
		OrderItem item31 = new OrderItem("book", 20, null);
		
		Order order1 = new Order("order1", 10, null, Arrays.asList(item11, item12));
		Order order2 = new Order("order2", 10, null, Arrays.asList(item21, item22));
		Order order3 = new Order("order3", 10, null, Arrays.asList(item31));
		
		
		Customer customer1 = new Customer("Customer1", Arrays.asList(group1, group3), Arrays.asList(order1, order2));
		Customer customer2 = new Customer("Customer2", Arrays.asList(group1, group2), Arrays.asList(order3));
		
		genericRepository.save(customer1);
		genericRepository.save(customer2);
	}
	
}
