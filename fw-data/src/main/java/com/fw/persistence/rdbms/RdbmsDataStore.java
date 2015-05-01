package com.fw.persistence.rdbms;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.InputStream;
import java.io.Reader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fw.ccg.xml.XMLBeanParser;
import com.fw.persistence.AuditSearchQuery;
import com.fw.persistence.EntityDetails;
import com.fw.persistence.IDataStore;
import com.fw.persistence.ITransaction;
import com.fw.persistence.ITransactionManager;
import com.fw.persistence.PersistenceException;
import com.fw.persistence.Record;
import com.fw.persistence.TransactionWrapper;
import com.fw.persistence.UnsupportedOperationException;
import com.fw.persistence.conversion.ConversionService;
import com.fw.persistence.query.AuditEntryQuery;
import com.fw.persistence.query.ChildrenExistenceQuery;
import com.fw.persistence.query.ColumnParam;
import com.fw.persistence.query.ConditionParam;
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

public class RdbmsDataStore implements IDataStore
{
	public static final String TEMPLATE_NAME_MYSQL = "mysql";
	public static final String TEMPLATE_NAME_DERBY = "derby";
	
	private static Logger logger = LogManager.getLogger(RdbmsDataStore.class);
	
	private RdbmsTemplates templates;
	private ConversionService conversionService = new ConversionService();
	private RdbmsTransactionManager transactionManager = new RdbmsTransactionManager();
	
	private String templatesName;
	
	public RdbmsDataStore(String templatesName)
	{
		templates = new RdbmsTemplates();
		this.templatesName = templatesName;
		
		try
		{
			logger.debug("Using RDBMS template type: " + templatesName);
			
			XMLBeanParser.parse(RdbmsDataStore.class.getResourceAsStream("/" + templatesName + ".xml"), templates);
		}catch(RuntimeException ex)
		{
			logger.error("An error occurred while loading template: " + templatesName, ex);
			throw ex;
		}
	}
	
	public void setConversionService(ConversionService conversionService)
	{
		this.conversionService = conversionService;
	}

	@Override
	public ConversionService getConversionService()
	{
		return conversionService;
	}
	
	public void setDataSource(DataSource dataSource)
	{
		transactionManager.setDataSource(dataSource);
	}
	
	@Override
	public ITransactionManager<? extends ITransaction> getTransactionManager()
	{
		return transactionManager;
	}
	
	private void closeResources(ResultSet rs, Statement statement)
	{
		try
		{
			if(rs != null)
			{
				rs.close();
			}
			
			if(statement != null)
			{
				statement.close();
			}
		}catch(Exception ex)
		{
			logger.error("An error occurred while closing DB resources", ex);
		}
	}

	@Override
	public Set<String> getColumnNames(String tableName)
	{
		logger.trace("Started method: getColumnNames");
		logger.trace("Fetching columns for table {}", tableName);
		
		try(TransactionWrapper<RdbmsTransaction> transaction = transactionManager.newOrExistingTransaction())
		{
			Connection connection = transaction.getTransaction().getConnection();
			
			ResultSet rs = connection.getMetaData().getColumns(null, null, tableName, null);
			Set<String> columns = new HashSet<>();
			
			while(rs.next())
			{
				columns.add(rs.getString("COLUMN_NAME"));
			}
			
			rs.close();
		
			logger.debug("For table '{}' found columns as - {}", tableName, columns);
			
			if(columns.isEmpty())
			{
				throw new IllegalStateException("No table found with name '" + tableName + "' or found with zero columns");
			}
			
			transaction.commit();
			return columns;
		}catch(Exception ex)
		{
			logger.error("An error occurred while fetching column names of table: " + tableName, ex);
			throw new PersistenceException("An error occurred while fetching column names of table: " + tableName, ex);
		}
	}

	@Override
	public void checkAndCreateSequence(String name)
	{
		logger.trace("Started method: checkAndCreateSequence");
		
		if(!templates.hasQuery(RdbmsTemplates.CREATE_SEQUENCE_QUERY) || !templates.hasQuery(RdbmsTemplates.CHECK_SEQUENCE_QUERY))
		{
			throw new UnsupportedOperationException("Create sequence is not supported by this data-store: " + templatesName);
		}
		
		Statement statement = null;
		ResultSet rs = null;
		
		try(TransactionWrapper<RdbmsTransaction> transaction = transactionManager.newOrExistingTransaction())
		{
			Connection connection = transaction.getTransaction().getConnection();

			statement = connection.createStatement();
			
			String query = templates.buildQuery(RdbmsTemplates.CHECK_SEQUENCE_QUERY, "name", name);
			logger.debug("Built check sequence query as:\n\t {}", query);
			
			rs = statement.executeQuery(query);

			//if any row is returned by CHECK_SEQUENCE_QUERY, assume sequence already exists
			if(rs.next())
			{
				logger.debug("Found sequence '" + name + "' to be already existing one.");
				return;
			}
			
			logger.debug("Found sequence '" + name + "' does not exits. Creating new sequence");
			
			query = templates.buildQuery(RdbmsTemplates.CREATE_SEQUENCE_QUERY, "name", name);
			logger.debug("Built create sequence query as:\n\t {}", query);			
			
			statement.execute(query);
			
			transaction.commit();
		}catch(Exception ex)
		{
			logger.error("An error occurred while executing create-sequence-query", ex);
			throw new PersistenceException("An error occurred while executing create-sequence-query", ex);
		}finally
		{
			closeResources(rs, statement);
		}
	}

	@Override
	public void createTable(CreateTableQuery createQuery)
	{
		logger.trace("Started method: createTable");
		
		Statement statement = null;
		
		try(TransactionWrapper<RdbmsTransaction> transaction = transactionManager.newOrExistingTransaction())
		{
			Connection connection = transaction.getTransaction().getConnection();

			statement = connection.createStatement();
			
			String query = templates.buildQuery(RdbmsTemplates.CREATE_QUERY, "query", createQuery);
			
			logger.debug("Built create-table query as: \n\t{}", query);
			
			statement.execute(query);
			transaction.commit();
		}catch(Exception ex)
		{
			logger.error("An error occurred while executing create-table-query", ex);
			throw new PersistenceException("An error occurred while executing create-table-query", ex);
		}finally
		{
			closeResources(null, statement);
		}
	}

	@Override
	public void createIndex(CreateIndexQuery creatIndexQuery)
	{
		logger.trace("Started method: createIndex");
		
		Statement statement = null;
		
		try(TransactionWrapper<RdbmsTransaction> transaction = transactionManager.newOrExistingTransaction())
		{
			Connection connection = transaction.getTransaction().getConnection();

			statement = connection.createStatement();
			
			String query = templates.buildQuery(RdbmsTemplates.CREATE_INDEX, "query", creatIndexQuery);
			
			logger.debug("Built create-index query as: \n\t{}", query);
			
			statement.execute(query);
			transaction.commit();
		}catch(Exception ex)
		{
			logger.error("An error occurred while executing create-index-query", ex);
			throw new PersistenceException("An error occurred while executing create-index-query", ex);
		}finally
		{
			closeResources(null, statement);
		}
	}

	@Override
	public int checkForExistenence(ExistenceQuery existenceQuery, EntityDetails entityDetails)
	{
		logger.trace("Started method: checkForExistenence");
		
		List<ConditionParam> conditions = existenceQuery.getConditions();
		
		/*
		if(conditions == null || conditions.isEmpty())
		{
			throw new IllegalStateException("Existence query is requested without conditions: " + existenceQuery);
		}
		*/
		
		logger.debug("Checking for existence of records from table '{}' using query: {}", existenceQuery.getTableName(), existenceQuery);
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try(TransactionWrapper<RdbmsTransaction> transaction = transactionManager.newOrExistingTransaction())
		{
			String query = templates.buildQuery(RdbmsTemplates.EXISTENCE_QUERY, "query", existenceQuery);
			
			logger.debug("Built existence query as: \n\t{}", query);
			
			Connection connection = transaction.getTransaction().getConnection();
			pstmt = connection.prepareStatement(query);
			int index = 1;
			List<Object> params = new ArrayList<>();
			
			if(conditions != null)
			{
				for(ConditionParam condition: conditions)
				{
					pstmt.setObject(index, condition.getValue());
					params.add(condition.getValue());
					
					index++;
				}
			}
			
			logger.debug("Executing using params: {}", params);
			
			rs = pstmt.executeQuery();
			
			if(!rs.next())
			{
				transaction.commit();
				return 0;
			}
			
			int count = rs.getInt(1);
			
			logger.debug("Existence of {} records found from table: {}", count, existenceQuery.getTableName());
			
			transaction.commit();
			return count;
		}catch(Exception ex)
		{
			logger.error("An error occurred while checking rows existence from table '" 
					+ existenceQuery.getTableName() + "' using query: " + existenceQuery, ex);
			throw new PersistenceException("An error occurred while checking rows existence from table '" 
						+ existenceQuery.getTableName() + "' using query: " + existenceQuery, ex);
		}finally
		{
			closeResources(rs, pstmt);
		}
	}
	
	@Override
	public int deleteChildren(DeleteChildrenQuery deleteChildrenQuery)
	{
		logger.trace("Started method: deleteChildren");
		logger.debug("Deleting children records from table '{}' using query: {}", deleteChildrenQuery.getChildTableName(), deleteChildrenQuery);
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try(TransactionWrapper<RdbmsTransaction> transaction = transactionManager.newOrExistingTransaction())
		{
			String query = templates.buildQuery(RdbmsTemplates.DELETE_CHILDREN_QUERY, "query", deleteChildrenQuery);
			
			logger.debug("Built children-delete query as: \n\t{}", query);
			
			Connection connection = transaction.getTransaction().getConnection();
			pstmt = connection.prepareStatement(query);
			int index = 1;
			List<Object> params = new ArrayList<>();
			
			if(deleteChildrenQuery.getParentConditions() != null)
			{
				for(ConditionParam condition: deleteChildrenQuery.getParentConditions())
				{
					pstmt.setObject(index, condition.getValue());
					params.add(condition.getValue());
					
					index++;
				}
			}

			if(deleteChildrenQuery.getChildConditions() != null)
			{
				for(ConditionParam condition: deleteChildrenQuery.getChildConditions())
				{
					pstmt.setObject(index, condition.getValue());
					params.add(condition.getValue());
					
					index++;
				}
			}

			logger.debug("Executing using params: " + params);
			
			int res = pstmt.executeUpdate();
			
			logger.debug("Deleted {} child record(s)", res);
			
			transaction.commit();
			return res;
		}catch(Exception ex)
		{
			logger.error("An error occurred while deleting children from table '" 
					+ deleteChildrenQuery.getTableName() + "' using query: " + deleteChildrenQuery, ex);
			throw new PersistenceException("An error occurred while deleting children from table '" 
						+ deleteChildrenQuery.getTableName() + "' using query: " + deleteChildrenQuery, ex);
		}finally
		{
			closeResources(rs, pstmt);
		}
	}
	
	@Override
	public int checkChildrenExistence(ChildrenExistenceQuery childrenExistenceQuery)
	{
		logger.trace("Started method: checkChildrenExistence");
		logger.debug("Checking children records from table '{}' using query: {}", childrenExistenceQuery.getChildTableName(), childrenExistenceQuery);
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try(TransactionWrapper<RdbmsTransaction> transaction = transactionManager.newOrExistingTransaction())
		{
			String query = templates.buildQuery(RdbmsTemplates.CHILDREN_EXISTENCE_QUERY, "query", childrenExistenceQuery);
			
			logger.debug("Built children-existence query as: \n\t{}", query);
			
			Connection connection = transaction.getTransaction().getConnection();
			pstmt = connection.prepareStatement(query);
			int index = 1;
			List<Object> params = new ArrayList<>();
			
			if(childrenExistenceQuery.getParentConditions() != null)
			{
				for(ConditionParam condition: childrenExistenceQuery.getParentConditions())
				{
					pstmt.setObject(index, condition.getValue());
					params.add(condition.getValue());
					
					index++;
				}
			}

			if(childrenExistenceQuery.getChildConditions() != null)
			{
				for(ConditionParam condition: childrenExistenceQuery.getChildConditions())
				{
					pstmt.setObject(index, condition.getValue());
					params.add(condition.getValue());
					
					index++;
				}
			}

			logger.debug("Executing using params: " + params);
			
			rs = pstmt.executeQuery();
			
			if(!rs.next())
			{
				return 0;
			}
			
			int res = rs.getInt(1);
			
			logger.debug("Found {} child record(s)", res);
			
			transaction.commit();
			return res;
		}catch(Exception ex)
		{
			logger.error("An error occurred while checking child rows existence from table '" 
					+ childrenExistenceQuery.getTableName() + "' using query: " + childrenExistenceQuery, ex);
			throw new PersistenceException("An error occurred while checking child rows existence from table '" 
						+ childrenExistenceQuery.getTableName() + "' using query: " + childrenExistenceQuery, ex);
		}finally
		{
			closeResources(rs, pstmt);
		}
	}
	
	@Override
	public List<Object> fetchChildrenIds(FetchChildrenIdsQuery fetchChildrenIdsQuery)
	{
		logger.trace("Started method: fetchChildrenIds");
		logger.debug("Fetching children records from table '{}' using query: {}", fetchChildrenIdsQuery.getChildTableName(), fetchChildrenIdsQuery);
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try(TransactionWrapper<RdbmsTransaction> transaction = transactionManager.newOrExistingTransaction())
		{
			String query = templates.buildQuery(RdbmsTemplates.FETCH_CHILDREN_IDS_QUERY, "query", fetchChildrenIdsQuery);
			
			logger.debug("Built children-fetch query as: \n\t{}", query);
			
			Connection connection = transaction.getTransaction().getConnection();
			pstmt = connection.prepareStatement(query);
			int index = 1;
			List<Object> params = new ArrayList<>();
			
			if(fetchChildrenIdsQuery.getParentConditions() != null)
			{
				for(ConditionParam condition: fetchChildrenIdsQuery.getParentConditions())
				{
					pstmt.setObject(index, condition.getValue());
					params.add(condition.getValue());
					
					index++;
				}
			}

			if(fetchChildrenIdsQuery.getChildConditions() != null)
			{
				for(ConditionParam condition: fetchChildrenIdsQuery.getChildConditions())
				{
					pstmt.setObject(index, condition.getValue());
					params.add(condition.getValue());
					
					index++;
				}
			}

			logger.debug("Executing using params: " + params);
			
			rs = pstmt.executeQuery();
			
			List<Object> ids = new LinkedList<Object>();
			
			while(rs.next())
			{
				ids.add(rs.getObject(1));
			}
			
			logger.debug("Found {} child record(s)", ids.size());
			
			transaction.commit();
			return ids;
		}catch(Exception ex)
		{
			logger.error("An error occurred while fetching child rows existence from table '" 
					+ fetchChildrenIdsQuery.getTableName() + "' using query: " + fetchChildrenIdsQuery, ex);
			throw new PersistenceException("An error occurred while fetchin child rows existence from table '" 
						+ fetchChildrenIdsQuery.getTableName() + "' using query: " + fetchChildrenIdsQuery, ex);
		}finally
		{
			closeResources(rs, pstmt);
		}
	}

	@Override
	public int save(SaveQuery saveQuery, EntityDetails entityDetails)
	{
		logger.trace("Started method: save");
		logger.debug("Trying to save entity to table '{}' using query: {}", saveQuery.getTableName(), saveQuery);
		
		PreparedStatement pstmt = null;
		
		try(TransactionWrapper<RdbmsTransaction> transaction = transactionManager.newOrExistingTransaction())
		{
			String query = templates.buildQuery(RdbmsTemplates.SAVE_QUERY, "query", saveQuery);
			
			logger.debug("Built save query as: \n\t{}", query);
			
			Connection connection = transaction.getTransaction().getConnection();
			pstmt = connection.prepareStatement(query);
			int index = 1;
			List<Object> params = new ArrayList<>();
			
			for(ColumnParam column: saveQuery.getColumns())
			{
				if(column.isSequenceGenerated())
				{
					continue;
				}
				
				pstmt.setObject(index, column.getValue());
				params.add(column.getValue());
				
				index++;
			}
			
			logger.debug("Executing using params: {}", params);
			
			int count = pstmt.executeUpdate();
			
			logger.debug("Saved " + count + " records into table: " + saveQuery.getTableName());
			
			transaction.commit();
			return count;
		}catch(Exception ex)
		{
			logger.error("An error occurred while saving entity to table '" 
					+ saveQuery.getTableName() + "' using query: " + saveQuery, ex);
			throw new PersistenceException("An error occurred while saving entity to table '" 
						+ saveQuery.getTableName() + "' using query: " + saveQuery, ex);
		}finally
		{
			closeResources(null, pstmt);
		}
	}

	@Override
	public int update(UpdateQuery updateQuery, EntityDetails entityDetails)
	{
		logger.trace("Started method: update");
		logger.debug("Trying to update entity in table '{}' using query: ", updateQuery.getTableName(), updateQuery);
		
		PreparedStatement pstmt = null;
		
		try(TransactionWrapper<RdbmsTransaction> transaction = transactionManager.newOrExistingTransaction())
		{
			String query = templates.buildQuery(RdbmsTemplates.UPDATE_QUERY, "query", updateQuery);
			
			logger.debug("Built update query as: \n\t{}", query);
			
			Connection connection = transaction.getTransaction().getConnection();
			pstmt = connection.prepareStatement(query);
			int index = 1;
			List<Object> params = new ArrayList<>();
			
			for(ColumnParam column: updateQuery.getColumns())
			{
				pstmt.setObject(index, column.getValue());
				params.add(column.getValue());
				
				index++;
			}
			
			for(ConditionParam condition: updateQuery.getConditions())
			{
				pstmt.setObject(index, condition.getValue());
				params.add(condition.getValue());
				
				index++;
			}
			
			logger.debug("Executing using params: {}", params);
			
			int count = pstmt.executeUpdate();
			
			logger.debug("Updated " + count + " records in table: " + updateQuery.getTableName());
			
			transaction.commit();
			return count;
		}catch(Exception ex)
		{
			logger.error("An error occurred while updating entity(s) to table '" 
					+ updateQuery.getTableName() + "' using query: " + updateQuery, ex);
			throw new PersistenceException("An error occurred while updating entity(s) to table '" 
						+ updateQuery.getTableName() + "' using query: " + updateQuery, ex);
		}finally
		{
			closeResources(null, pstmt);
		}
	}
	
	@Override
	public int saveOrUpdate(SaveOrUpdateQuery saveOrUpdateQuery, EntityDetails entityDetails)
	{
		logger.trace("Started method: saveOrUpdate");
		logger.debug("Trying to save or update entity in table '{}' using query: ", saveOrUpdateQuery.getTableName(), saveOrUpdateQuery);
		
		if(!templates.hasQuery(RdbmsTemplates.SAVE_UPDATE_QUERY))
		{
			throw new UnsupportedOperationException("Sav-update operation is not supported by this data-store");
		}
		
		PreparedStatement pstmt = null;
		
		try(TransactionWrapper<RdbmsTransaction> transaction = transactionManager.newOrExistingTransaction())
		{
			String query = templates.buildQuery(RdbmsTemplates.SAVE_UPDATE_QUERY, "query", saveOrUpdateQuery);
			
			logger.debug("Built save-update query as: \n\t{}", query);
			
			Connection connection = transaction.getTransaction().getConnection();
			pstmt = connection.prepareStatement(query);
			int index = 1;
			List<Object> params = new ArrayList<>();
			
			for(ColumnParam column: saveOrUpdateQuery.getInsertColumns())
			{
				if(column.isSequenceGenerated())
				{
					continue;
				}
				
				pstmt.setObject(index, column.getValue());
				params.add(column.getValue());
				
				index++;
			}
			
			for(ColumnParam column: saveOrUpdateQuery.getUpdateColumns())
			{
				if(column.isSequenceGenerated())
				{
					continue;
				}
				
				pstmt.setObject(index, column.getValue());
				params.add(column.getValue());
				
				index++;
			}
			
			logger.debug("Executing using params: {}", params);
			
			int count = pstmt.executeUpdate();
			
			logger.debug("Updated/created " + count + " records in table: " + saveOrUpdateQuery.getTableName());
			
			transaction.commit();
			return count;
		}catch(Exception ex)
		{
			logger.error("An error occurred while updating entity(s) to table '" 
					+ saveOrUpdateQuery.getTableName() + "' using query: " + saveOrUpdateQuery, ex);
			throw new PersistenceException("An error occurred while updating entity(s) to table '" 
						+ saveOrUpdateQuery.getTableName() + "' using query: " + saveOrUpdateQuery, ex);
		}finally
		{
			closeResources(null, pstmt);
		}
	}

	@Override
	public int delete(DeleteQuery deleteQuery, EntityDetails entityDetails)
	{
		logger.trace("Started method: delete");
		logger.debug("Deleting rows from table '{}' using query: {}", deleteQuery.getTableName(), deleteQuery);
		
		PreparedStatement pstmt = null;
		
		try(TransactionWrapper<RdbmsTransaction> transaction = transactionManager.newOrExistingTransaction())
		{
			String query = templates.buildQuery(RdbmsTemplates.DELETE_QUERY, "query", deleteQuery);
			
			logger.debug("Built delete query as: \n\t{}", query);
			
			Connection connection = transaction.getTransaction().getConnection();
			pstmt = connection.prepareStatement(query);
			int index = 1;
			List<Object> params = new ArrayList<>();
			
			for(ConditionParam condition: deleteQuery.getConditions())
			{
				pstmt.setObject(index, condition.getValue());
				params.add(condition.getValue());
				
				index++;
			}
			
			logger.debug("Executing using params: {}", params);
			
			int deleteCount = pstmt.executeUpdate();
			
			logger.debug("Deleted " + deleteCount + " records from table: " + deleteQuery.getTableName());
			
			transaction.commit();
			return deleteCount;
		}catch(Exception ex)
		{
			logger.error("An error occurred while deleting rows from table '" + deleteQuery.getTableName() + "' using query: " + deleteQuery, ex);
			throw new PersistenceException("An error occurred while deleting rows from table '" + deleteQuery.getTableName() + "' using query: " + deleteQuery, ex);
		}finally
		{
			closeResources(null, pstmt);
		}
	}

	@Override
	public void addAuditEntries(AuditEntryQuery auditQuery)
	{
		logger.trace("Started method: addAuditEntries");
		logger.debug("Adding audit entries to table '{}' using query: {}", auditQuery.getAuditTableName(), auditQuery);
		
		PreparedStatement pstmt = null;
		
		try(TransactionWrapper<RdbmsTransaction> transaction = transactionManager.newOrExistingTransaction())
		{
			String query = templates.buildQuery(RdbmsTemplates.AUDIT_ENTRY_QUERY, "query", auditQuery);
			
			logger.debug("Built audit query as: \n\t{}", query);
			
			Connection connection = transaction.getTransaction().getConnection();
			pstmt = connection.prepareStatement(query);
			int index = 1;
			List<Object> params = new ArrayList<>();
			
			for(ColumnParam column: auditQuery.getAuditColumns())
			{
				pstmt.setObject(index, column.getValue());
				params.add(column.getValue());
				
				index++;
			}
			
			for(ConditionParam condition: auditQuery.getConditions())
			{
				pstmt.setObject(index, condition.getValue());
				params.add(condition.getValue());
				
				index++;
			}
			
			logger.debug("Executing using params: {}", params);
			
			int deleteCount = pstmt.executeUpdate();
			
			logger.debug("Added " + deleteCount + " audit-entries to table: " + auditQuery.getTableName());
			
			transaction.commit();
		}catch(Exception ex)
		{
			logger.error("An error occurred while adding audit entries to table '" + auditQuery.getAuditTableName() + "' using query: " + auditQuery, ex);
			throw new PersistenceException("An error occurred while adding audit entries to table '" + auditQuery.getAuditTableName() + "' using query: " + auditQuery, ex);
		}finally
		{
			closeResources(null, pstmt);
		}
	}
	
	protected PreparedStatement buildPreparedStatement(RdbmsTransaction transaction, String queryName, Object... params) throws SQLException
	{
		List<Object> paramValues = new ArrayList<>();
		
		String query = templates.buildQuery(queryName, paramValues, params);
		
		logger.debug("Built query as: \n\t{}", query);
		
		Connection connection = transaction.getConnection();
		PreparedStatement pstmt = connection.prepareStatement(query);
		int index = 1;
		
		for(Object value: paramValues)
		{
			pstmt.setObject(index, value);
			index++;
		}
		
		logger.debug("Executing using params: {}", paramValues);
		
		return pstmt;
	}
	
	protected int executeUpdate(String queryName, String tableName, Object... params)
	{
		PreparedStatement pstmt = null;
		
		try(TransactionWrapper<RdbmsTransaction> transaction = transactionManager.newOrExistingTransaction())
		{
			pstmt = buildPreparedStatement(transaction.getTransaction(), queryName, params);
			
			int count = pstmt.executeUpdate();
			
			transaction.commit();
			return count;
		}catch(Exception ex)
		{
			logger.error("An error occurred while fetching rows from table - " + tableName, ex);
			
			throw new PersistenceException("An error occurred while fetching rows from table - " + tableName, ex);
		}finally
		{
			closeResources(null, pstmt);
		}
	}

	protected List<Record> fetchRecords(String queryName, String tableName, Object... params)
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try(TransactionWrapper<RdbmsTransaction> transaction = transactionManager.newOrExistingTransaction())
		{
			pstmt = buildPreparedStatement(transaction.getTransaction(), queryName, params);
			
			rs = pstmt.executeQuery();
			
			List<Record> records = new ArrayList<>();
			ResultSetMetaData metaData = rs.getMetaData();
			Record  rec = null;
			int colCount = metaData.getColumnCount();
			String colNames[] = null;
			Object cellValue = null;
			
			while(rs.next())
			{
				//if records are available fetch columns names, so that same name objects
				//are shared across the records
				if(colNames == null)
				{
					colNames = new String[colCount];
					
					for(int i = 0 ; i < colCount ; i++)
					{
						colNames[i] = metaData.getColumnLabel(i + 1);
					}
				}
				
				rec = new Record(colCount);
				
				//fetch column values for each record
				for(int i = 0 ; i < colCount ; i++)
				{
					cellValue = rs.getObject(i + 1);
					
					if(cellValue instanceof Clob)
					{
						cellValue = convertClob((Clob)cellValue);
					}
					else if(cellValue instanceof Blob)
					{
						cellValue = convertBlob((Blob)cellValue);
					}
					
					rec.set(i, colNames[i], cellValue);
				}
				
				records.add(rec);
			}
			
			logger.debug("Found " + records.size() + " records found from table: " + tableName);
			
			transaction.commit();
			return records;
		}catch(Exception ex)
		{
			logger.error("An error occurred while fetching rows from table - " + tableName, ex);
			
			throw new PersistenceException("An error occurred while fetching rows from table - " + tableName, ex);
		}finally
		{
			closeResources(rs, pstmt);
		}
		
	}
	
	@Override
	public void clearAudit(EntityDetails entityDetails, Date tillDate)
	{
		logger.trace("Started method: clearAudit");
		
		int count = executeUpdate(RdbmsTemplates.AUDIT_CLEAR_QUERY, 
				entityDetails.getAuditDetails().getTableName(),  
				"auditDetails", entityDetails.getAuditDetails(),
				"tillDate", tillDate);
		
		logger.debug("Deleted {} audit records from table {}", count, entityDetails.getAuditDetails().getTableName());
	}

	@Override
	public List<Record> fetchAuditEntries(EntityDetails entityDetails, AuditSearchQuery query)
	{
		logger.trace("Started method: fetchAuditEntries");
		
		return fetchRecords(RdbmsTemplates.AUDIT_FETCH_QUERY, entityDetails.getAuditDetails().getTableName(), 
				"auditDetails", entityDetails.getAuditDetails(),
				"query", query,
				"entityDetails", entityDetails);
	}

	private char[] convertClob(Clob clob)
	{
		try
		{
			Reader reader = clob.getCharacterStream();
			char buf[] = new char[1024];
			int len = 0;
			CharArrayWriter charWriter = new CharArrayWriter();
			
			while((len = reader.read(buf)) > 0)
			{
				charWriter.write(buf, 0, len);
			}
	
			reader.close();
			return charWriter.toCharArray();
		}catch(Exception ex)
		{
			logger.error("An error occurred while reading clob", ex);
			throw new IllegalStateException("An error occurred while reading clob", ex);
		}
		
	}

	private byte[] convertBlob(Blob blob)
	{
		try
		{
			InputStream is = blob.getBinaryStream();
			byte buf[] = new byte[1024];
			int len = 0;
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			
			while((len = is.read(buf)) > 0)
			{
				bos.write(buf, 0, len);
			}
	
			is.close();
			return bos.toByteArray();
		}catch(Exception ex)
		{
			logger.error("An error occurred while reading blob", ex);
			throw new IllegalStateException("An error occurred while reading blob", ex);
		}
		
	}

	@Override
	public List<Record> executeFinder(FinderQuery findQuery, EntityDetails entityDetails)
	{
		logger.trace("Started method: executeFinder");
		logger.debug("Fetching records from table '{}' using query: {}", findQuery.getTableName(), findQuery);
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try(TransactionWrapper<RdbmsTransaction> transaction = transactionManager.newOrExistingTransaction())
		{
			String query = templates.buildQuery(RdbmsTemplates.FINDER_QUERY, "query", findQuery);
			
			logger.debug("Built find query as: \n\t{}", query);
			List<Object> params = new ArrayList<>();
			
			Connection connection = transaction.getTransaction().getConnection();
			pstmt = connection.prepareStatement(query);
			int index = 1;
			
			for(ConditionParam condition: findQuery.getConditions())
			{
				pstmt.setObject(index, condition.getValue());
				params.add(condition.getValue());
				
				index++;
			}
			
			logger.debug("Executing using params: {}", params);
			
			rs = pstmt.executeQuery();
			
			List<Record> records = new ArrayList<>();
			ResultSetMetaData metaData = rs.getMetaData();
			Record  rec = null;
			int colCount = metaData.getColumnCount();
			String colNames[] = null;
			Object cellValue = null;
			
			while(rs.next())
			{
				//if records are avialable fetch columns names, so taht same name objects
				//are shared across the reocrds
				if(colNames == null)
				{
					colNames = new String[colCount];
					
					for(int i = 0 ; i < colCount ; i++)
					{
						colNames[i] = metaData.getColumnLabel(i + 1);
					}
				}
				
				rec = new Record(colCount);
				
				//fetch column values for each record
				for(int i = 0 ; i < colCount ; i++)
				{
					cellValue = rs.getObject(i + 1);
					
					if(cellValue instanceof Clob)
					{
						cellValue = convertClob((Clob)cellValue);
					}
					else if(cellValue instanceof Blob)
					{
						cellValue = convertBlob((Blob)cellValue);
					}
					
					rec.set(i, colNames[i], cellValue);
				}
				
				records.add(rec);
			}
			
			logger.debug("Found " + records.size() + " records found from table: " + findQuery.getTableName());
			
			transaction.commit();
			return records;
		}catch(Exception ex)
		{
			logger.error("An error occurred while finding rows from table '" 
					+ findQuery.getTableName() + "' using query: " + findQuery, ex);
			
			throw new PersistenceException("An error occurred while finding rows from table '" 
						+ findQuery.getTableName() + "' using query: " + findQuery, ex);
		}finally
		{
			closeResources(rs, pstmt);
		}
	}
	
	
}
