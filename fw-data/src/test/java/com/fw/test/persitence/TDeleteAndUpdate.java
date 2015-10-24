package com.fw.test.persitence;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.fw.persistence.repository.RepositoryFactory;
import com.fw.test.persitence.entity.Employee;
import com.fw.test.persitence.entity.IEmployeeRepository;
import com.fw.test.persitence.queries.EmpSearchResult;
import com.fw.utils.CommonUtils;

public class TDeleteAndUpdate extends TestSuiteBase
{
	@Override
	protected void initFactoryBeforeClass(RepositoryFactory factory)
	{
		IEmployeeRepository repo = factory.getRepository(IEmployeeRepository.class);
		repo.save(new Employee("1230", "user0@test.com", "user1", "1234560", 20));
		repo.save(new Employee("1231", "user1@test.com", "user2", "1234561", 25));
		repo.save(new Employee("1232", "user2@test.com", "user2", "1234562", 30));
		repo.save(new Employee("1233", "user3@test.com", "user3", "1234563", 35));
		repo.save(new Employee("1234", "user4@test.com", "user4", "1234564", 40));
		repo.save(new Employee("1235", "user5@test.com", "user5", "12345644", 45));
	}

	@Override
	protected void cleanFactoryAfterClass(RepositoryFactory factory)
	{
		//cleanup the emp table
		factory.dropRepository(Employee.class);
	}

	/**
	 * Test update method which return update count
	 */
	@Test(dataProvider = "repositoryFactories")
	public void testUpdateWithIntReturn(RepositoryFactory factory)
	{
		IEmployeeRepository repo = factory.getRepository(IEmployeeRepository.class);
		
		int count = repo.updateAge("user2", 27);
		Assert.assertEquals(count, 2);
		
		
		List<EmpSearchResult> results = repo.findResultsByName("user2");
		Assert.assertEquals(CommonUtils.toSet(results.get(0).getAge(), results.get(1).getAge()), CommonUtils.toSet(27));
	}

	/**
	 * Tests update method which return success/failure as boolean flag
	 * @param factory
	 */
	@Test(dataProvider = "repositoryFactories")
	public void testUpdateWithBooleanReturn(RepositoryFactory factory)
	{
		IEmployeeRepository repo = factory.getRepository(IEmployeeRepository.class);
		
		boolean res = repo.updatePhone("user0@test.com", "987654");
		Assert.assertTrue(res);
		
		res = repo.updatePhone("nonexisting@test.com", "987654");
		Assert.assertFalse(res);
		
		Employee emp = repo.findEmpByEmail("user0@test.com");
		Assert.assertEquals(emp.getPhoneNo(), "987654");
	}

	/**
	 * Tests update method which return success/failure as boolean flag
	 * @param factory
	 */
	@Test(dataProvider = "repositoryFactories")
	public void testDeleteWithBooleanReturn(RepositoryFactory factory)
	{
		IEmployeeRepository repo = factory.getRepository(IEmployeeRepository.class);
		
		Assert.assertEquals(repo.getCountByMailId("user5@test.com"), 1);
		Assert.assertEquals(repo.getCount(), 6);
		
		Assert.assertTrue(repo.deleteByMailId("user5@test.com"));
		Assert.assertEquals(repo.getCountByMailId("user5@test.com"), 0);
		Assert.assertEquals(repo.getCount(), 5);
	}

	/**
	 * Test delete method when return type is int
	 * @param factory
	 */
	@Test(dataProvider = "repositoryFactories")
	public void testDeleteWithIntReturn(RepositoryFactory factory)
	{
		IEmployeeRepository repo = factory.getRepository(IEmployeeRepository.class);
		
		Assert.assertEquals(repo.getCountByMailId("user4@test.com"), 1);
		Assert.assertEquals(repo.deleteByUserName("user4"), 1);
		Assert.assertEquals(repo.getCountByMailId("user4@test.com"), 0);
	}
}
