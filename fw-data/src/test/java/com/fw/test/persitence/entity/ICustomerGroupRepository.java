package com.fw.test.persitence.entity;

import java.util.List;

import com.fw.persistence.ICrudRepository;
import com.fw.persistence.repository.annotations.Condition;

public interface ICustomerGroupRepository extends ICrudRepository<CustomerGroup>
{
	public List<CustomerGroup> findGroupsOfCusomer(@Condition("customers.name") String customerName);
}
