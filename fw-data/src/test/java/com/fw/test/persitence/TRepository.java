package com.fw.test.persitence;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.fw.persistence.ChildConstraintViolationException;
import com.fw.persistence.ForeignConstraintViolationException;
import com.fw.persistence.UniqueConstraintViolationException;
import com.fw.persistence.UnsupportedOperationException;
import com.fw.persistence.rdbms.RdbmsDataStore;
import com.fw.persistence.repository.RepositoryFactory;
import com.fw.test.persitence.config.TestConfiguration;
import com.fw.test.persitence.entity.Address;
import com.fw.test.persitence.entity.Address1;
import com.fw.test.persitence.entity.Employee;
import com.fw.test.persitence.entity.IAddress1Repository;
import com.fw.test.persitence.entity.IAddressRepository;
import com.fw.test.persitence.entity.IEmployeeRepository;

public class TRepository
{
	private static Logger logger = LogManager.getLogger(TRepository.class);
	
	private RepositoryFactory factory = new RepositoryFactory();
	private RdbmsDataStore rdbmsDataStore = new RdbmsDataStore(RdbmsDataStore.TEMPLATE_NAME_DERBY);
	
	@BeforeClass
	public void init()
	{
		rdbmsDataStore.setDataSource(TestConfiguration.getTestConfiguration().getDataSource());
		factory.setDataStore(rdbmsDataStore);
		
		factory.setCreateTables(true);
	}
	
	@AfterMethod
	public void clean()
	{
		IAddressRepository addressRepository = factory.getRepository(IAddressRepository.class);
		addressRepository.deleteAll();
		
		IEmployeeRepository empRepository = factory.getRepository(IEmployeeRepository.class);
		empRepository.deleteAll();
	}
	
	@Test
	public void testGetRepository()
	{
		try
		{
			IEmployeeRepository empRepository = factory.getRepository(IEmployeeRepository.class);
			Assert.assertNotNull(empRepository);
		}catch(Exception ex)
		{
			ex.printStackTrace();
			throw ex;
		}
	}

	@Test
	public void testCreate()
	{
		IEmployeeRepository empRepository = factory.getRepository(IEmployeeRepository.class);
		
		Employee emp = new Employee("12345", "kranthi@kk.com", "kranthi", "90232333");
		boolean res = empRepository.save(emp);
		
		Assert.assertTrue(res, "Entity was not saved");
		Assert.assertNotNull("Id is not set for saved entity", emp.getId());
	}

	@Test
	public void testUniqunessInCreate()
	{
		IEmployeeRepository empRepository = factory.getRepository(IEmployeeRepository.class);
		
		Employee emp = new Employee("12345", "kranthi@kk.com", "kranthi", "90232333");
		empRepository.save(emp);
			
		//create employee with same emp-id
		Employee emp1 = new Employee("12345", "kiran@kk.com", "kiran", "90231223");
		
		try
		{
			empRepository.save(emp1);
			Assert.fail("Employee got saved with duplicate employee id");
		}catch(UniqueConstraintViolationException ex)
		{
			Assert.assertEquals("EmpNo", ex.getConstraintName());
		}
		
		//create employee with same email-id
		Employee emp2 = new Employee("12346", "kranthi@kk.com", "kiran", "90231223");
		
		try
		{
			empRepository.save(emp2);
			Assert.fail("Employee got saved with duplicate email id");
		}catch(UniqueConstraintViolationException ex)
		{
			Assert.assertEquals("EmailId", ex.getConstraintName());
			Assert.assertTrue(ex.getMessage().contains("kranthi@kk.com"));
		}
	}

	@Test
	public void testForUpdate()
	{
		IEmployeeRepository empRepository = factory.getRepository(IEmployeeRepository.class);
		
		Employee emp = new Employee("12345", "kranthi@kk.com", "kranthi", "90232333");
		empRepository.save(emp);
		
		Employee emp1 = new Employee("123452", "kiran@kk.com", "kiran", "90232333");
		empRepository.save(emp1);
		
		//update the emp with different emp id and email id
		Employee empForUpdate = new Employee("12345678", "kranthi123@kk.com", "kranthi12", "12390232333");
		empForUpdate.setId(emp.getId());
		Assert.assertTrue(empRepository.update(empForUpdate));
		
		Employee updatedEmp = empRepository.findById(emp.getId());
		Assert.assertEquals("12345", updatedEmp.getEmployeeNo()); //check emp no is not changed
		Assert.assertEquals("kranthi123@kk.com", updatedEmp.getEmailId());
		Assert.assertEquals("kranthi12", updatedEmp.getName());
		Assert.assertEquals("12390232333", updatedEmp.getPhoneNo());
	}

	@Test
	public void testUniquenessDuringUpdate()
	{
		IEmployeeRepository empRepository = factory.getRepository(IEmployeeRepository.class);
		
		Employee emp = new Employee("12345", "kranthi@kk.com", "kranthi", "90232333");
		empRepository.save(emp);
		
		Employee emp1 = new Employee("123452", "kiran@kk.com", "kiran", "90232333");
		empRepository.save(emp1);

		try
		{
			Employee empForUpdate = new Employee("1234523", "kiran@kk.com", "kranthi12", "12390232333");
			empForUpdate.setId(emp.getId());
			
			empRepository.update(empForUpdate);
			Assert.fail("Employee got updated with duplicate mail");
		}catch(UniqueConstraintViolationException ex)
		{
			Assert.assertEquals("EmailId", ex.getConstraintName());
			Assert.assertTrue(ex.getMessage().contains("kiran@kk.com"));
		}
	}

	@Test
	public void testFinders()
	{
		IEmployeeRepository empRepository = factory.getRepository(IEmployeeRepository.class);
		
		Employee emp = new Employee("12345", "kranthi@kk.com", "kranthi", "90232333");
		empRepository.save(emp);
		String empId = emp.getId();
		
		Employee emp1 = new Employee("123452", "kiran@kk.com", "kiran", "90232333");
		empRepository.save(emp1);
		String empId1 = emp1.getId();

		Employee emp2 = new Employee("123455", "abc@kk.com", "abc", "887788778");
		empRepository.save(emp2);

		Employee foundEmployee = empRepository.findById(empId1);
		Assert.assertEquals(emp1.getEmailId(), foundEmployee.getEmailId());
		
		foundEmployee = empRepository.findByEmployeeNo("12345");
		Assert.assertEquals(empId, foundEmployee.getId());
		
		Assert.assertEquals(empId, empRepository.findIdByEmail("kranthi@kk.com"));
		Assert.assertEquals("kiran@kk.com", empRepository.findEmailByEmpno("123452"));
		
		Assert.assertEquals(2, empRepository.findByPhoneNo("%90%").size());
	}
	
	@Test
	public void testSaveOrUpdate()
	{
		try
		{
			IEmployeeRepository empRepository = factory.getRepository(IEmployeeRepository.class);
			
			Employee emp = new Employee("12345", "kranthi@kk.com", "kranthi", "90232333");
			boolean res = empRepository.saveOrUpdate(emp);
			
			Assert.assertTrue(res, "Entity was not saved");
			Assert.assertNotNull("Id is not set for saved entity", emp.getId());
			
			Employee empForUpdate = new Employee("12345", "kranthi1@kk.com", "kranthi", "902909090");
			res = empRepository.saveOrUpdate(empForUpdate);
			
			Assert.assertTrue(res, "Entity was not saved");
			Assert.assertEquals(emp.getId(), empForUpdate.getId());
		}catch(UnsupportedOperationException ex)
		{
			logger.info("Save-update operation is unssupported by current data-store");
		}
	}
	
	@Test
	public void testDelete()
	{
		IEmployeeRepository empRepository = factory.getRepository(IEmployeeRepository.class);
		IAddressRepository addressRepository = factory.getRepository(IAddressRepository.class);
		
		Employee emp = new Employee("12345", "kranthi@kk.com", "kranthi", "90232333");
		empRepository.save(emp);
		
		Employee emp1 = new Employee("123452", "kiran@kk.com", "kiran", "90232333");
		empRepository.save(emp1);

		addressRepository.save(new Address(null, "Address1", emp.getId(), Address.PARENT_TYPE_EMPLOYEE));
		addressRepository.save(new Address(null, "Address2", emp.getId(), Address.PARENT_TYPE_EMPLOYEE));
		
		addressRepository.save(new Address(null, "Address3", emp1.getId(), Address.PARENT_TYPE_EMPLOYEE));
		addressRepository.save(new Address(null, "Address4", emp1.getId(), Address.PARENT_TYPE_EMPLOYEE));
		
		Assert.assertEquals(2, addressRepository.findByParentId(emp.getId()).size(), "Addresses are not added properly");
		
		String invalidId = "3455566";
		
		try
		{
			//provide invalid id
			addressRepository.save(new Address(null, "Address3", invalidId, Address.PARENT_TYPE_EMPLOYEE));
			Assert.fail("Address got saved with invalid id");
		}catch(ForeignConstraintViolationException ex)
		{
			Assert.assertEquals("PARENT_ID", ex.getConstraintName());
			Assert.assertTrue(ex.getMessage().contains(invalidId));
		}
		
		addressRepository.save(new Address(null, "Address5", "2345678", Address.PARENT_TYPE_OTHER));
		addressRepository.save(new Address(null, "Address5", "2345556", Address.PARENT_TYPE_OTHER));
		
		//Delete parent employee without deleting child addresses, that should throw error
		try
		{
			empRepository.deleteById(emp.getId());
			Assert.fail("Employee got deleted when child items (addresses) are still present");
		}catch(ChildConstraintViolationException ex)
		{
			Assert.assertEquals("PARENT_ID", ex.getConstraintName());
			Assert.assertTrue(ex.getMessage().contains(Address.class.getName()));
		}

		//delete child address items and then try to delete parent employee, which should succeed
		addressRepository.deleteByParentId(emp.getId());
		
		Assert.assertEquals(0, addressRepository.findByParentId(emp.getId()).size(), "Addresses are not delete properly");
		
		empRepository.deleteById(emp.getId());
	}

	@Test
	public void testAutoChildDelete()
	{
		IEmployeeRepository empRepository = factory.getRepository(IEmployeeRepository.class);
		IAddress1Repository address1Repository = factory.getRepository(IAddress1Repository.class);
		
		Employee emp = new Employee("12345", "kranthi@kk.com", "kranthi", "90232333");
		empRepository.save(emp);
		
		Employee emp1 = new Employee("123452", "kiran@kk.com", "kiran", "90232333");
		empRepository.save(emp1);

		address1Repository.save(new Address1(null, "Address1", emp.getId(), Address.PARENT_TYPE_EMPLOYEE));
		address1Repository.save(new Address1(null, "Address2", emp.getId(), Address.PARENT_TYPE_EMPLOYEE));
		
		address1Repository.save(new Address1(null, "Address3", emp1.getId(), Address.PARENT_TYPE_EMPLOYEE));
		address1Repository.save(new Address1(null, "Address4", emp1.getId(), Address.PARENT_TYPE_EMPLOYEE));
		
		Assert.assertEquals(2, address1Repository.findByParentId(emp.getId()).size(), "Addresses are not added properly");
		Assert.assertEquals(2, address1Repository.findByParentId(emp1.getId()).size(), "Addresses are not added properly");
		
		//Delete parent employee without deleting child addresses, which should delete employee along with child addresses
		empRepository.deleteById(emp.getId());
		
		Assert.assertEquals(0, address1Repository.findByParentId(emp.getId()).size(), "Child addresses are not deleted properly");
		Assert.assertEquals(2, address1Repository.findByParentId(emp1.getId()).size(), "Other addresses got deleted by bug");
	}
}
