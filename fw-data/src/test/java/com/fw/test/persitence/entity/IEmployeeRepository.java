package com.fw.test.persitence.entity;

import java.util.List;

import com.fw.persistence.ICrudRepository;
import com.fw.persistence.Operator;
import com.fw.persistence.repository.annotations.Condition;
import com.fw.persistence.repository.annotations.Field;

public interface IEmployeeRepository extends ICrudRepository<Employee>
{
	public Employee findByEmployeeNo(String empNo);
	
	@Field("id")
	public long findIdByEmail(@Condition("emailId")String mail);

	@Field("emailId")
	public String findEmailByEmpno(@Condition("employeeNo")String empNo);
	
	public List<Employee> findByPhoneNo(@Condition(value = "phoneNo", op = Operator.LIKE) String phone);

	public void deleteAll();
}
