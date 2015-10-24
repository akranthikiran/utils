package com.fw.test.persitence;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.fw.persistence.Operator;
import com.fw.persistence.RecordCountMistmatchException;
import com.fw.persistence.repository.RepositoryFactory;
import com.fw.persistence.repository.annotations.SearchResult;
import com.fw.persistence.repository.search.SearchCondition;
import com.fw.persistence.repository.search.SearchQuery;
import com.fw.test.persitence.entity.Employee;
import com.fw.test.persitence.entity.IEmployeeRepository;
import com.fw.test.persitence.queries.EmpSearchQuery;
import com.fw.test.persitence.queries.EmpSearchResult;
import com.fw.test.persitence.queries.KeyValueBean;
import com.fw.utils.CommonUtils;


public class TFinders extends TestSuiteBase
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
	 * Tests finder based on finder method name
	 */
	@Test(dataProvider = "repositoryFactories")
	public void testFindByMethodName(RepositoryFactory factory)
	{
		IEmployeeRepository repo = factory.getRepository(IEmployeeRepository.class);
		Assert.assertEquals(repo.findEmailByEmployeeNo("1234"), "user4@test.com");
	}
	
	/**
	 * Tests finder whose conditions specified
	 * using annotation
	 * @param factory
	 */
	@Test(dataProvider = "repositoryFactories")
	public void testFindWithCondition(RepositoryFactory factory)
	{
		IEmployeeRepository repo = factory.getRepository(IEmployeeRepository.class);
		Assert.assertEquals(repo.findAge("user2", "1234562"), 30);
	}
	
	/**
	 * Tests entity fetching
	 * @param factory
	 */
	@Test(dataProvider = "repositoryFactories")
	public void testFindForEntity(RepositoryFactory factory)
	{
		IEmployeeRepository repo = factory.getRepository(IEmployeeRepository.class);
		
		Employee e = repo.findByEmployeeNo("1234");
		Assert.assertEquals(e.getAge(), 40);
		Assert.assertEquals(e.getEmailId(), "user4@test.com");
		Assert.assertEquals(e.getEmployeeNo(), "1234");
		Assert.assertEquals(e.getName(), "user4");
		Assert.assertEquals(e.getPhoneNo(), "1234564");
		Assert.assertTrue(e.getId() > 0);
	}

	/**
	 * Test finder to fetch collection of employees
	 * @param factory
	 */
	@Test(dataProvider = "repositoryFactories")
	public void testFindForEntityCollection(RepositoryFactory factory)
	{
		IEmployeeRepository repo = factory.getRepository(IEmployeeRepository.class);
		
		List<Employee> empLst = repo.findByPhoneNo("%64%");
		Assert.assertEquals(empLst.size(), 2);

		Assert.assertEquals(CommonUtils.toSet(empLst.get(0).getAge(), empLst.get(1).getAge()) , CommonUtils.toSet(40, 45));
		Assert.assertEquals(CommonUtils.toSet(empLst.get(0).getEmailId(), empLst.get(1).getEmailId()) , CommonUtils.toSet("user4@test.com", "user5@test.com"));
		Assert.assertEquals(CommonUtils.toSet(empLst.get(0).getEmployeeNo(), empLst.get(1).getEmployeeNo()) , CommonUtils.toSet("1234", "1235"));
		Assert.assertEquals(CommonUtils.toSet(empLst.get(0).getName(), empLst.get(1).getName()) , CommonUtils.toSet("user4", "user5"));
		Assert.assertEquals(CommonUtils.toSet(empLst.get(0).getPhoneNo(), empLst.get(1).getPhoneNo()) , CommonUtils.toSet("1234564", "12345644"));
	}

	/**
	 * Tests entity fetch based on query object
	 * @param factory
	 */
	@Test(dataProvider = "repositoryFactories")
	public void testQueryByConditionBean(RepositoryFactory factory)
	{
		IEmployeeRepository repo = factory.getRepository(IEmployeeRepository.class);
		
		List<Employee> empLst = repo.find(new EmpSearchQuery(null, "%64%", 5, 100));
		Assert.assertEquals(empLst.size(), 2);

		Assert.assertEquals(CommonUtils.toSet(empLst.get(0).getAge(), empLst.get(1).getAge()) , CommonUtils.toSet(40, 45));
		Assert.assertEquals(CommonUtils.toSet(empLst.get(0).getEmailId(), empLst.get(1).getEmailId()) , CommonUtils.toSet("user4@test.com", "user5@test.com"));
		Assert.assertEquals(CommonUtils.toSet(empLst.get(0).getEmployeeNo(), empLst.get(1).getEmployeeNo()) , CommonUtils.toSet("1234", "1235"));
		Assert.assertEquals(CommonUtils.toSet(empLst.get(0).getName(), empLst.get(1).getName()) , CommonUtils.toSet("user4", "user5"));
		Assert.assertEquals(CommonUtils.toSet(empLst.get(0).getPhoneNo(), empLst.get(1).getPhoneNo()) , CommonUtils.toSet("1234564", "12345644"));
		
		empLst = repo.find(new EmpSearchQuery("user2", "1234561", null, null));
		Assert.assertEquals(empLst.size(), 1);

		empLst = repo.find(new EmpSearchQuery("user2", null, null, null));
		Assert.assertEquals(empLst.size(), 2);

		empLst = repo.find(new EmpSearchQuery(null, null, 35, null));
		Assert.assertEquals(empLst.size(), 3);

		empLst = repo.find(new EmpSearchQuery(null, null, 35, 40));
		Assert.assertEquals(empLst.size(), 2);
	}

	/**
	 * Tests finder method which returns different bean collection other than entity or simple field (using {@link SearchResult} annotation)
	 * @param factory
	 */
	@Test(dataProvider = "repositoryFactories")
	public void testFetchDiffBeanCollection(RepositoryFactory factory)
	{
		IEmployeeRepository repo = factory.getRepository(IEmployeeRepository.class);
		
		List<EmpSearchResult> results = repo.findResultsByName("user2");
		Assert.assertEquals(CommonUtils.toSet(results.get(0).getEmpName(), results.get(1).getEmpName()) , CommonUtils.toSet("user2"));
		Assert.assertEquals(CommonUtils.toSet(results.get(0).getAge(), results.get(1).getAge()) , CommonUtils.toSet(25, 30));
	}
	
	/**
	 * Tests finder method which returns different bean other than entity or simple field (using {@link SearchResult} annotation)
	 * @param factory
	 */
	@Test(dataProvider = "repositoryFactories", expectedExceptions = RecordCountMistmatchException.class)
	public void testFetchDiffBean(RepositoryFactory factory)
	{
		IEmployeeRepository repo = factory.getRepository(IEmployeeRepository.class);

		EmpSearchResult res = repo.findResultByName("user2");
		Assert.assertEquals(res.getEmpName(), "user2");
		Assert.assertEquals(res.getAge(), 20);

		//this results in multiple rows but expected is single row. So results in error
		repo.findResultByName("user2");
	}

	/**
	 * Tests finder with {@link SearchResult} annotation with mappings
	 * @param factory
	 */
	@Test(dataProvider = "repositoryFactories")
	public void testFinderWithMappings(RepositoryFactory factory)
	{
		IEmployeeRepository repo = factory.getRepository(IEmployeeRepository.class);
		
		List<KeyValueBean> results = repo.findKeyValues("1234564%");
		Assert.assertEquals(results.size(), 2);
		Assert.assertEquals(CommonUtils.toSet(results.get(0).getKey(), results.get(1).getKey()) , CommonUtils.toSet("user4", "user5"));
		Assert.assertEquals(CommonUtils.toSet(results.get(0).getValue(), results.get(1).getValue()) , CommonUtils.toSet("40", "45"));
	}

	@Test(dataProvider = "repositoryFactories")
	public void testSearch(RepositoryFactory factory)
	{
		IEmployeeRepository repo = factory.getRepository(IEmployeeRepository.class);
		
		List<Employee> results = repo.search(new SearchQuery(
				new SearchCondition("name", Operator.EQ, "user2")
		));
		
		Assert.assertEquals(results.size(), 2);
		Assert.assertEquals(CommonUtils.toSet(results.get(0).getEmployeeNo(), results.get(1).getEmployeeNo()) , CommonUtils.toSet("1231", "1232"));
		
		results = repo.search(new SearchQuery(
				new SearchCondition("name", Operator.EQ, "user2"),
				new SearchCondition("phoneNo", Operator.EQ, "1234561")
		));
		
		Assert.assertEquals(results.size(), 1);
		Assert.assertEquals(results.get(0).getEmployeeNo() , "1231");
	}
}
