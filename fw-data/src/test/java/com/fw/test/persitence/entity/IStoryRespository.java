package com.fw.test.persitence.entity;

import com.fw.persistence.ICrudRepository;
import com.fw.persistence.repository.annotations.Condition;
import com.fw.persistence.repository.annotations.Field;

public interface IStoryRespository extends ICrudRepository<Story>
{
	public void deleteAll();
	
	public boolean updateDescriptionByName(@Condition("name") String name, @Field("description") String description);
	
	public @Field("id") String fetchIdByName(String name);
}
