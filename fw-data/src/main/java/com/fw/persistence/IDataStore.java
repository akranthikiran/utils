package com.fw.persistence;

import java.util.List;
import java.util.Set;

import com.fw.persistence.conversion.ConversionService;
import com.fw.persistence.query.ChildrenExistenceQuery;
import com.fw.persistence.query.CountQuery;
import com.fw.persistence.query.CreateIndexQuery;
import com.fw.persistence.query.CreateTableQuery;
import com.fw.persistence.query.DeleteQuery;
import com.fw.persistence.query.DropTableQuery;
import com.fw.persistence.query.FetchChildrenIdsQuery;
import com.fw.persistence.query.FinderQuery;
import com.fw.persistence.query.SaveQuery;
import com.fw.persistence.query.UpdateQuery;
import com.fw.utils.ObjectWrapper;

public interface IDataStore
{
	public ConversionService getConversionService();
	
	public ITransactionManager<? extends ITransaction> getTransactionManager();
	
	public Set<String> getColumnNames(String tableName);
	
	public void checkAndCreateSequence(String name);
	
	public void createTable(CreateTableQuery query);
	
	public void createIndex(CreateIndexQuery query);
	
	public long getCount(CountQuery existenceQuery, EntityDetails entityDetails);
	
	/**
	 * Executes the specified save-query using structure details from specified entity-details. And stores
	 * generated id if any, into idGenerated.
	 * 
	 * @param saveQuery
	 * @param entityDetails
	 * @param idGenerated
	 * @return Number of rows effected (1 or zero in general)
	 */
	public int save(SaveQuery saveQuery, EntityDetails entityDetails, ObjectWrapper<Object> idGenerated);

	public int update(UpdateQuery updateQuery, EntityDetails entityDetails);
	
	public int delete(DeleteQuery deleteQuery, EntityDetails entityDetails);
	
	public int checkChildrenExistence(ChildrenExistenceQuery childrenExistenceQuery);
	
	public List<Object> fetchChildrenIds(FetchChildrenIdsQuery fetchChildrenIdsQuery);

	public List<Record> executeFinder(FinderQuery findQuery, EntityDetails entityDetails);
	
	/**
	 * Drops the underlying entity table
	 * @param query
	 */
	public void dropTable(DropTableQuery query);
	
	/**
	 * Indicates whether check for foreign key relation should be done explicitly. Needed by NOSQL DB, if integrity needs
	 * to be maintained.
	 * This is used 
	 * 		During delete, to check/delete child entities with parent entity
	 * 		During insert/update, to check if parent entity exists or not
	 * 
	 * DataStore which needs this explicit support, should have setter to accept whether this explicit check is required by
	 * the application. By this developer can choose whether it should be enabled or not.
	 * @return
	 */
	public boolean isExplicitForeignCheckRequired();
}


