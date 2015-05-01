package com.fw.persistence;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.fw.persistence.conversion.ConversionService;
import com.fw.persistence.query.AuditEntryQuery;
import com.fw.persistence.query.ChildrenExistenceQuery;
import com.fw.persistence.query.CreateIndexQuery;
import com.fw.persistence.query.CreateTableQuery;
import com.fw.persistence.query.DeleteChildrenQuery;
import com.fw.persistence.query.DeleteQuery;
import com.fw.persistence.query.ExistenceQuery;
import com.fw.persistence.query.FetchChildrenIdsQuery;
import com.fw.persistence.query.FinderQuery;
import com.fw.persistence.query.SaveOrUpdateQuery;
import com.fw.persistence.query.SaveQuery;
import com.fw.persistence.query.UpdateQuery;

public interface IDataStore
{
	public ConversionService getConversionService();
	
	public ITransactionManager<? extends ITransaction> getTransactionManager();
	
	public Set<String> getColumnNames(String tableName);
	
	public void checkAndCreateSequence(String name);
	
	public void createTable(CreateTableQuery query);
	
	public void createIndex(CreateIndexQuery query);
	
	public int checkForExistenence(ExistenceQuery existenceQuery, EntityDetails entityDetails);
	
	public int save(SaveQuery saveQuery, EntityDetails entityDetails);

	public int update(UpdateQuery updateQuery, EntityDetails entityDetails);
	
	public int saveOrUpdate(SaveOrUpdateQuery saveOrUpdateQuery, EntityDetails entityDetails);
	
	public int delete(DeleteQuery deleteQuery, EntityDetails entityDetails);
	
	public int deleteChildren(DeleteChildrenQuery deleteChildrenQuery);
	
	public int checkChildrenExistence(ChildrenExistenceQuery childrenExistenceQuery);
	
	public List<Object> fetchChildrenIds(FetchChildrenIdsQuery fetchChildrenIdsQuery);

	public List<Record> executeFinder(FinderQuery findQuery, EntityDetails entityDetails);

	//Audit realted functions
	public void addAuditEntries(AuditEntryQuery query);
	
	public void clearAudit(EntityDetails entityDetails, Date tillDate);
	public List<Record> fetchAuditEntries(EntityDetails entityDetails, AuditSearchQuery query);
}


