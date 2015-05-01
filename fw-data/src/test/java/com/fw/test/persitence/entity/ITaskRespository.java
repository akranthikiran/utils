package com.fw.test.persitence.entity;

import com.fw.persistence.ICrudRepository;

public interface ITaskRespository extends ICrudRepository<Task>
{
	public void deleteAll();
}
