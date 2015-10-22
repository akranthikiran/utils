package com.fw.test.persitence.entity;

import java.util.List;

import com.fw.persistence.ICrudRepository;
import com.fw.persistence.Operator;
import com.fw.persistence.repository.annotations.Condition;
import com.fw.persistence.repository.annotations.ConditionBean;
import com.fw.persistence.repository.annotations.Field;
import com.fw.persistence.repository.annotations.ResultMapping;
import com.fw.persistence.repository.annotations.SearchResult;
import com.fw.test.persitence.queries.EmpSearchQuery;
import com.fw.test.persitence.queries.EmpSearchResult;
import com.fw.test.persitence.queries.KeyValueBean;

public interface IEmployeeRepository extends ICrudRepository<Employee>
{
	public Employee findByEmployeeNo(String empNo);
	
	@Field("id")
	public long findIdByEmail(@Condition("emailId") String mail);
	
	@Field("age")
	public int findAge(@Condition("name") String name, @Condition("phoneNo") String phoneNo);

	@Field("emailId")
	public String findEmailByEmployeeNo(String empNo);
	
	public List<Employee> findByPhoneNo(@Condition(value = "phoneNo", op = Operator.LIKE) String phone);

	public List<Employee> find(@ConditionBean EmpSearchQuery query);
	
	@SearchResult
	public List<EmpSearchResult> findResultsByName(@Condition("name") String name);
	
	@SearchResult
	public EmpSearchResult findResultByName(@Condition("name") String name);
	
	@SearchResult(mappings = {
		@ResultMapping(entityField = "name", property = "key"),
		@ResultMapping(entityField = "age", property = "value")
	})
	public List<KeyValueBean> findKeyValues(@Condition(value = "phoneNo", op = Operator.LIKE) String phone);

	public void deleteAll();
}
