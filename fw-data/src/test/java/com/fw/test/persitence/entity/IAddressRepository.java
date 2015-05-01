package com.fw.test.persitence.entity;

import java.util.List;

import com.fw.persistence.ICrudRepository;

public interface IAddressRepository extends ICrudRepository<Address>
{
	public List<Address> findByParentId(String parentId);
	
	public void deleteByParentId(String parentId);
	
	public void deleteAll();
}
