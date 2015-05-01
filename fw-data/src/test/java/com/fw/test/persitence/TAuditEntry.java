package com.fw.test.persitence;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.fw.persistence.AuditSearchQuery;
import com.fw.persistence.AuditType;
import com.fw.persistence.Record;
import com.fw.persistence.rdbms.RdbmsDataStore;
import com.fw.persistence.repository.RepositoryFactory;
import com.fw.test.persitence.config.TestConfiguration;
import com.fw.test.persitence.entity.IStoryRespository;
import com.fw.test.persitence.entity.ITaskRespository;
import com.fw.test.persitence.entity.Story;
import com.fw.test.persitence.entity.Task;

public class TAuditEntry
{
	private RepositoryFactory factory = new RepositoryFactory();
	private RdbmsDataStore rdbmsDataStore = new RdbmsDataStore(RdbmsDataStore.TEMPLATE_NAME_DERBY);

	private Date tomorrow;
	
	@BeforeClass
	public void init()
	{
		rdbmsDataStore.setDataSource(TestConfiguration.getTestConfiguration().getDataSource());
		factory.setDataStore(rdbmsDataStore);
		factory.setPersistenceContext(new TestPersistenceContext());
		
		factory.setCreateTables(true);
		
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(new Date());
		calendar.add(Calendar.DATE, 1);
		
		this.tomorrow = calendar.getTime();
	}
	
	@AfterClass
	public void clean()
	{
		ITaskRespository taskRepository = factory.getRepository(ITaskRespository.class);
		taskRepository.deleteAll();
		taskRepository.clearAuditEntries(tomorrow);
		
		IStoryRespository storyRepository = factory.getRepository(IStoryRespository.class);
		storyRepository.deleteAll();
		storyRepository.clearAuditEntries(tomorrow);
	}
	
	private void addStories()
	{
		IStoryRespository storyRepository = factory.getRepository(IStoryRespository.class);
		
		storyRepository.save(new Story("story1", "story1"));
		storyRepository.save(new Story("story2", "story2"));
	}
	
	private void addStoriesAndTasks()
	{
		addStories();
		
		ITaskRespository taskRespository = factory.getRepository(ITaskRespository.class);
		IStoryRespository storyRepository = factory.getRepository(IStoryRespository.class);
		
		String id1 = storyRepository.fetchIdByName("story1");
		String id2 = storyRepository.fetchIdByName("story2");
		
		taskRespository.save(new Task(id1, "task1"));
		taskRespository.save(new Task(id1, "task2"));
		
		taskRespository.save(new Task(id2, "task3"));
	}

	@Test
	public void testAuditEntryAdditions()
	{
		System.out.println("Testing audit entries for create");
		IStoryRespository storyRepository = factory.getRepository(IStoryRespository.class);

		addStories();
		
		AuditSearchQuery auditSearchQuery = new AuditSearchQuery();
		List<Record> records = storyRepository.fetchAuditRecords(auditSearchQuery);
		
		Assert.assertNotNull(records);
		Assert.assertEquals(2, records.size());
		Assert.assertEquals("story1", records.get(0).getString("NAME"));
		Assert.assertEquals("story2", records.get(1).getString("NAME"));
		Assert.assertEquals(AuditType.INSERT.toString(), records.get(0).getString("AUDIT_CHANGE_TYPE"));
		Assert.assertEquals(AuditType.INSERT.toString(), records.get(1).getString("AUDIT_CHANGE_TYPE"));
		Assert.assertEquals(TestPersistenceContext.USER, records.get(1).getString("AUDIT_CHANGED_BY"));
	}
	
	@Test
	public void testAuditEntryUpdates()
	{
		System.out.println("Testing audit entries for update");
		IStoryRespository storyRepository = factory.getRepository(IStoryRespository.class);

		addStories();
		
		String id1 = storyRepository.fetchIdByName("story1");
		String id2 = storyRepository.fetchIdByName("story2");
		
		Assert.assertTrue(storyRepository.updateDescriptionByName("story1", "story1-1"));
		Assert.assertTrue(storyRepository.updateDescriptionByName("story1", "story1-2"));
		
		Assert.assertTrue(storyRepository.updateDescriptionByName("story2", "story2-1"));

		//check for record 1, where there are 2 updates
		AuditSearchQuery auditSearchQuery = new AuditSearchQuery();
		auditSearchQuery.setAuditType(AuditType.UPDATE);
		auditSearchQuery.setEntityId(id1);
		
		List<Record> records = storyRepository.fetchAuditRecords(auditSearchQuery);
		Assert.assertNotNull(records);
		Assert.assertEquals(2, records.size());
		Assert.assertEquals("story1-1", records.get(0).getString("DESCRIPTION"));
		Assert.assertEquals("story1-2", records.get(1).getString("DESCRIPTION"));
		
		
		//check for record 2, where there is 1 update
		auditSearchQuery = new AuditSearchQuery();
		auditSearchQuery.setAuditType(AuditType.UPDATE);
		auditSearchQuery.setEntityId(id2);
		
		records = storyRepository.fetchAuditRecords(auditSearchQuery);
		Assert.assertNotNull(records);
		Assert.assertEquals(1, records.size());
		Assert.assertEquals("story2-1", records.get(0).getString("DESCRIPTION"));
		Assert.assertEquals(AuditType.UPDATE.name(), records.get(0).getString("AUDIT_CHANGE_TYPE"));
	}
	
	@Test
	public void testAuditEntryDeletes()
	{
		System.out.println("Testing audit entries for delete");
		IStoryRespository storyRepository = factory.getRepository(IStoryRespository.class);
		ITaskRespository taskRepository = factory.getRepository(ITaskRespository.class);

		addStoriesAndTasks();
		
		String id1 = storyRepository.fetchIdByName("story1");

		//child audit search query
		AuditSearchQuery childAuditSearchQuery = new AuditSearchQuery();
		childAuditSearchQuery.setAuditType(AuditType.DELETE);

		//make sure there are no delete audit entries in child audit table
		List<Record> taskAuditRecords = storyRepository.fetchAuditRecords(childAuditSearchQuery);
		Assert.assertTrue(taskAuditRecords == null || taskAuditRecords.isEmpty());

		//delete the parent entity
		Assert.assertTrue(storyRepository.deleteById(id1));

		//check for record deletion audit entry
		AuditSearchQuery auditSearchQuery = new AuditSearchQuery();
		auditSearchQuery.setAuditType(AuditType.DELETE);
		auditSearchQuery.setEntityId(id1);
		
		List<Record> storyAuditRecords = storyRepository.fetchAuditRecords(auditSearchQuery);
		Assert.assertNotNull(storyAuditRecords);
		Assert.assertEquals(1, storyAuditRecords.size());
		Assert.assertEquals("story1", storyAuditRecords.get(0).getString("NAME"));
		Assert.assertEquals(AuditType.DELETE.name(), storyAuditRecords.get(0).getString("AUDIT_CHANGE_TYPE"));

		//check for child record deletion audit entry
		
		taskAuditRecords = taskRepository.fetchAuditRecords(childAuditSearchQuery);
		Assert.assertNotNull(childAuditSearchQuery);
		Assert.assertEquals(2, taskAuditRecords.size());
		Assert.assertEquals("task1", taskAuditRecords.get(0).getString("NAME"));
		Assert.assertEquals(AuditType.DELETE.name(), taskAuditRecords.get(0).getString("AUDIT_CHANGE_TYPE"));

		Assert.assertEquals("task2", taskAuditRecords.get(1).getString("NAME"));
		Assert.assertEquals(AuditType.DELETE.name(), taskAuditRecords.get(1).getString("AUDIT_CHANGE_TYPE"));
	}
}
