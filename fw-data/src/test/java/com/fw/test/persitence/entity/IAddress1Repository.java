package com.fw.test.persitence.entity;

import java.util.List;

import com.fw.persistence.ICrudRepository;

public interface IAddress1Repository extends ICrudRepository<Address1>
{
	public List<Address1> findByParentId(String parentId);
	
	public void deleteAll();
}
