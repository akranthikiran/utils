package com.fw.dao.qry.impl;

import java.io.InputStream;
import java.sql.PreparedStatement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.lang.model.util.Types;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fw.ccg.xml.XMLBeanParser;
import com.fw.dao.qry.ConnectionSource;
import com.fw.dao.qry.DataDigester;
import com.fw.dao.qry.FunctionManager;
import com.fw.dao.qry.Query;
import com.fw.dao.qry.QuerySource;
import com.fw.dao.qry.SQLTypeMapping;

public class XMLQuerySource implements QuerySource
{
	private static Logger logger = LogManager.getLogger(XMLQuerySource.class);

	public static final String QRY_PARAM_DIGESTER = "#digester";
	public static final String QRY_PARAM_CONNECTION = "#connection";
	public static final String QRY_PARAM_FETCH_COUNT = "#fetchCount";

	public static final String BEAN_DATA_DIGESTER = "beanDigester";
	public static final String REC_DATA_DIGESTER = "recordDigester";
	public static final String PROP_DATA_DIGESTER = "propBeanDigester";

	public static final String DEF_CONNECTION_SOURCE_NAME = "#default";

	private ConnectionSource connectionSource;
	private String transactionManagerName = "DEFAULT_TRANSACTION_MANAGER";

	private Map<String, Query> nameToQuery = new HashMap<String, Query>();
	private Map<String, DataDigester<? extends Object>> nameToDigester = new HashMap<String, DataDigester<? extends Object>>();
	private String defaultDigester = REC_DATA_DIGESTER;

	private Map<String, Object> nameToGprop = new HashMap<>();
	
	public XMLQuerySource()
	{
		addDigester(BEAN_DATA_DIGESTER, new BeanDataDigester());
		addDigester(REC_DATA_DIGESTER, new RecordDataDigester());
		addDigester(PROP_DATA_DIGESTER, new PropertyBeanDataDigester());
	}

	public XMLQuerySource(InputStream is, ConnectionSource connectionSource)
	{
		this();

		try
		{
			this.connectionSource = connectionSource;
			XMLBeanParser.parse(is, this, new QueryXMLBeanHandler(this));
		}catch(Exception ex)
		{
			throw new IllegalStateException("An error occured while loading from stream", ex);
		}
	}
	
	public void processLinkFrom(XMLQuerySource xmlQuerySource)
	{
		if(this.connectionSource == null)
		{
			this.connectionSource = xmlQuerySource.connectionSource;
		}
		
		this.nameToQuery.putAll(xmlQuerySource.nameToQuery);
		this.nameToDigester.putAll(xmlQuerySource.nameToDigester);
		this.nameToDigester.putAll(xmlQuerySource.nameToDigester);
		this.nameToGprop.putAll(xmlQuerySource.nameToGprop);
	}
	
	public String getTransactionManagerName()
	{
		return transactionManagerName;
	}

	public void setTransactionManagerName(String transactionManagerName)
	{
		if(transactionManagerName == null || transactionManagerName.trim().length() == 0)
		{
			throw new NullPointerException("Transaction manager name can not be empty or null");
		}
		
		this.transactionManagerName = transactionManagerName;
	}

	public void addDigester(String name, DataDigester<? extends Object> digester)
	{
		if(name == null || name.trim().length() == 0)
			throw new IllegalArgumentException("Null/empty digester name specified");

		if(digester == null)
			throw new NullPointerException("Digester cannot be null.");

		nameToDigester.put(name, digester);
	}

	public void addFunctionClass(String className)
	{
		try
		{
			Class<?> cls = Class.forName(className);
			FunctionManager.register(cls);
		}catch(Exception ex)
		{
			throw new IllegalStateException("An error occured while loading function class: " + className, ex);
		}
	}

	@Override
	public ConnectionSource getConnectionSource()
	{
		return connectionSource;
	}

	public void setConnectionSource(ConnectionSource connectionSource)
	{
		this.connectionSource = connectionSource;
	}

	@Override
	public Query getQuery(String name)
	{
		return nameToQuery.get(name);
	}
	
	@Override
	public Set<String> getQueryNames()
	{
		return Collections.unmodifiableSet(this.nameToQuery.keySet());
	}

	@Override
	public boolean hasQuery(String name)
	{
		return nameToQuery.containsKey(name);
	}

	public void addQuery(String name, Query query)
	{
		nameToQuery.put(name, query);
	}

	public void addGlobalProperty(String name, Object bean)
	{
		nameToGprop.put(name, bean);
	}

	public Object getGlobalProperty(String name)
	{
		return nameToGprop.get(name);
	}

	public void setDefaultDigester(String defaultDigester)
	{
		this.defaultDigester = defaultDigester;
	}

	@Override
	public DataDigester<?> getDataDigester(Query query)
	{
		String digesterName = query.getParam(QRY_PARAM_DIGESTER);

		if(digesterName == null)
			digesterName = defaultDigester;

		return nameToDigester.get(digesterName);
	}

	@Override
	public void customize(String name, PreparedStatement pstmt)
	{
		Query qry = nameToQuery.get(name);

		if(qry == null)
			return;

		String fetchStr = qry.getParam(QRY_PARAM_FETCH_COUNT);

		if(fetchStr == null || fetchStr.trim().length() == 0)
			return;

		int fetchCount = 0;

		try
		{
			fetchCount = Integer.parseInt(fetchStr);
		}catch(Exception ex)
		{
			logger.debug("Invalid fetch count specified for query (" + name + "): " + fetchStr);
			return;
		}

		try
		{
			pstmt.setFetchSize(fetchCount);
			logger.debug("Setting fetch count as: " + fetchCount);
		}catch(Exception ex)
		{
			logger.debug("An error occured while setting fetch count(" + fetchCount + ") for query: " + name + "\nError: " + ex);

		}
	}

	public void addSQLMapping(String javaType, String sqlType)
	{
		Class<?> jtype = null;
		int sqlTypeCode = 0;

		try
		{
			jtype = Class.forName(javaType);
		}catch(Exception ex)
		{
			throw new IllegalArgumentException("Invalid java type specified: " + javaType, ex);
		}

		try
		{
			sqlTypeCode = Types.class.getField(sqlType.toUpperCase()).getInt(null);
		}catch(Exception ex)
		{
			throw new IllegalArgumentException("Invalid SQL type specified: " + sqlType, ex);
		}

		SQLTypeMapping.registerMapping(sqlTypeCode, jtype);
	}
}
