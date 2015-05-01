package com.fw.dao.qry.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import com.fw.ccg.util.AbstractObjectCacheFactory;
import com.fw.ccg.util.ICacheableBean;
import com.fw.ccg.util.ObjectCacheFactory;
import com.fw.ccg.util.StringUtil;
import com.fw.dao.qry.FilterResult;
import com.fw.dao.qry.QueryFilter;

public class MapQueryFilter implements QueryFilter, ICacheableBean<MapQueryFilter>
{
	public static final String ATTR_NULL_NODE = "ifNull";
	public static final String ATTR_VALUE = "ifValue";
	public static final String ATTR_NO_VALUE = "ifNotValue";
	public static final String ATTR_MIN_TRIM_LEN = "minTrimLen";
	public static final String ATTR_MAX_TRIM_LEN = "maxTrimLen";
	public static final String ATTR_MIN_VALUE = "minVal";
	public static final String ATTR_MAX_VALUE = "maxVal";
	public static final String ATTR_OTHER_PARAMS = "otherParams";

	public static final String NODE_TABLE = "TABLE";
	public static final String TABLE_ATTR_NAME = "name";
	public static final String ATTR_REQ_TAB = "reqTables";

	public static final String ATTR_LOOP_COLLECTION = "loopCollection";
	public static final String ATTR_LOOP_DELIMITER = "loopDelimiter";
	
	private String nullString;
	private Map<String, Object> nameToValue = new HashMap<String, Object>();
	private Set<String> requiredTables = new HashSet<String>();
	private Map<String, String[]> tabToReqTab = new HashMap<String, String[]>();
	
	private ObjectCacheFactory<FilterResult> resultFactory = AbstractObjectCacheFactory.getObjectCacheFactory(FilterResult.class);

	public MapQueryFilter(Map<String, ? extends Object> nameToValue)
	{
		this(nameToValue, "NULL");
	}

	public MapQueryFilter(Map<String, ? extends Object> nameToValue, String nullString)
	{
		if(nameToValue == null)
			throw new NullPointerException("Map cannot be null.");

		if(nullString == null)
			throw new NullPointerException("Null string cannot be null");

		this.nameToValue.putAll(nameToValue);
		this.nullString = nullString;
	}

	public MapQueryFilter(Object... keyValues)
	{
		this.nullString = "NULL";
		setValues(keyValues);
	}

	public MapQueryFilter()
	{}
	
	protected MapQueryFilter(String nullString)
	{
		if(nullString == null)
			throw new NullPointerException("Null string cannot be null");

		this.nullString = nullString;
	}
	
	public MapQueryFilter setValues(Object... keyValues)
	{
		if(keyValues == null || keyValues.length == 0)
			throw new NullPointerException("Key-Values cannot be null or empty.");

		if(keyValues.length % 2 != 0)
			throw new IllegalArgumentException("Invalid key-value pair length encountered: " + keyValues.length);

		this.nameToValue.clear();

		for(int i = 0; i < keyValues.length; i += 2)
		{
			if(!(keyValues[i] instanceof String))
				throw new IllegalArgumentException("Invalid key encountered at - " + i + " : " + keyValues[i]);

			this.nameToValue.put((String)keyValues[i], keyValues[i + 1]);
		}
		
		return this;
	}

	public MapQueryFilter setValues(Map<String, ? extends Object> keyValues)
	{
		if(keyValues == null)
			throw new NullPointerException("Key-Values cannot be null or empty.");

		this.nameToValue.clear();
		
		this.nameToValue.putAll(keyValues);
		return this;
	}
	
	protected void setParamMap(Map<String, ? extends Object> nameToValue)
	{
		if(nameToValue == null)
			throw new NullPointerException("Map cannot be null.");

		reinitalize(null);
		
		this.nameToValue.putAll(nameToValue);
	}

	protected void addRequiredTable(String table)
	{
		if(table == null || table.trim().length() == 0)
			return;

		requiredTables.add(table);

		String depTables[] = tabToReqTab.get(table);

		if(depTables != null)
		{
			for(String depTab : depTables)
				requiredTables.add(depTab);
		}
	}
	
	private FilterResult buildResult(int resVal, Object val)
	{
		FilterResult res = resultFactory.getFreeInstance();
		res.setResult(resVal);
		res.setValue(val);
		
		return res;
	}
	
	private FilterResult buildResult(int resVal)
	{
		return buildResult(resVal, null);
	}

	protected FilterResult handleTableElement(Map<String, String> nodeAttr, int phaseNo)
	{
		String tableName = nodeAttr.get(TABLE_ATTR_NAME);

		if(tableName == null || tableName.trim().length() == 0)
			throw new IllegalStateException("No name specified for <TABLE> element.");

		if(phaseNo == 0)
		{
			String reqTabLst = nodeAttr.get(ATTR_REQ_TAB);

			if(reqTabLst != null && reqTabLst.trim().length() > 0)
			{
				String reqTables[] = StringUtil.tokenize(reqTabLst, ",", false, false, true);
				tabToReqTab.put(tableName, reqTables);
			}

			return buildResult(NEXT_PHASE);
		}

		if(!requiredTables.contains(tableName))
			return buildResult(REJECT);

		return buildResult(ACCEPT);
	}

	@Override
	public FilterResult accept(String name, Map<String, String> nodeAttr, int phaseNo)
	{
		if(NODE_TABLE.equals(name))
			return handleTableElement(nodeAttr, phaseNo);

		FilterResult res = process(name, nodeAttr, phaseNo);

		if(res.getResult() == ACCEPT)
		{
			String reqTabLst = nodeAttr.get(ATTR_REQ_TAB);

			if(reqTabLst != null && reqTabLst.trim().length() > 0)
			{
				String reqTables[] = StringUtil.tokenize(reqTabLst, ",", false, false, true);

				for(String tab : reqTables)
					addRequiredTable(tab);
			}
		}

		return res;
	}

	private FilterResult process(String name, Map<String, String> nodeAttr, int phaseNo)
	{
		Object value = getValue(name);
		boolean acceptIfNull = "true".equalsIgnoreCase(nodeAttr.get(ATTR_NULL_NODE));

		if(acceptIfNull)
		{
			if(value == null)
				return buildResult(ACCEPT);

			return buildResult(REJECT);
		}

		if(value == null)
			return buildResult(REJECT);
		
		boolean loopCollection = "true".equalsIgnoreCase(nodeAttr.get(ATTR_LOOP_COLLECTION));

		if(loopCollection)
		{
			if(value instanceof Collection)
			{
				String loopDelimiter = nodeAttr.get(ATTR_LOOP_DELIMITER);
				
				FilterResult res = buildResult(PROCESS_COLLECTION, value);
				res.setDelimiter(loopDelimiter);
				
				return res;
			}
			else
			{
				throw new IllegalStateException(ATTR_LOOP_COLLECTION + " attribute is used on node \"" + name + "\" represented by non-collection object: [Type: " + value.getClass().getName() + ", Object: " + value + "]");
			}
		}

		String otherParams = nodeAttr.get(ATTR_OTHER_PARAMS);

		if(otherParams != null && otherParams.trim().length() > 0)
		{
			StringTokenizer st = new StringTokenizer(otherParams, ",");
			String nextParam = null;
			Object nextParamValue = null;
			while(st.hasMoreTokens())
			{
				nextParam = st.nextToken().trim();

				if(nextParam.length() == 0)
					continue;

				nextParamValue = getValue(nextParam);

				if(nextParamValue == null)
					return buildResult(REJECT);
			}
		}

		String nodeValue = nodeAttr.get(ATTR_VALUE);
		String nodeNoVal = nodeAttr.get(ATTR_NO_VALUE);

		if(nodeValue != null || nodeNoVal != null)
		{
			String valueStr = value.toString();

			if(nodeValue != null)
			{
				if(nodeValue.equals(valueStr))
					return buildResult(ACCEPT);
				else
					return buildResult(REJECT);
			}

			if(nodeNoVal != null)
			{
				if(nodeNoVal.equals(valueStr))
					return buildResult(REJECT);
			}
		}

		if(value instanceof String)
		{
			String strValue = (String)value;
			Integer minTrimLen = toInt(ATTR_MIN_TRIM_LEN, nodeAttr.get(ATTR_MIN_TRIM_LEN));
			Integer maxTrimLen = toInt(ATTR_MAX_TRIM_LEN, nodeAttr.get(ATTR_MAX_TRIM_LEN));

			int trimLen = strValue.trim().length();

			if(minTrimLen != null && trimLen < minTrimLen)
				return buildResult(REJECT);

			if(maxTrimLen != null && trimLen > maxTrimLen)
				return buildResult(REJECT);
		}

		Integer minVal = toInt(ATTR_MIN_VALUE, nodeAttr.get(ATTR_MIN_VALUE));
		Integer maxVal = toInt(ATTR_MAX_VALUE, nodeAttr.get(ATTR_MAX_VALUE));

		if(minVal != null || maxVal != null)
		{
			int iVal = 0;

			if(value instanceof Integer)
				iVal = (Integer)value;
			else if(value instanceof String)
			{
				try
				{
					iVal = (int)Double.parseDouble(((String)value).trim());
				}catch(Exception ex)
				{
					return buildResult(ACCEPT);
				}
			}
			else
				return buildResult(ACCEPT);

			if(minVal != null && iVal < minVal)
				return buildResult(REJECT);

			if(maxVal != null && iVal > maxVal)
				return buildResult(REJECT);
		}

		return buildResult(ACCEPT);
	}

	private Integer toInt(String name, String s)
	{
		if(s == null)
			return null;

		try
		{
			return Integer.parseInt(s);
		}catch(Exception ex)
		{
			throw new IllegalArgumentException("Invalid int value specified for \"" + name + "\": " + s);
		}
	}

	@Override
	public Object getProperty(String funcName, String paramName)
	{
		return getValue(paramName);
	}

	public void setNullString(String nullString)
	{
		if(nullString == null)
			throw new NullPointerException("Null string cannot be null");

		this.nullString = nullString;
	}

	@Override
	public String getNullString()
	{
		return nullString;
	}

	@Override
	public Object getProperty(String name)
	{
		return getValue(name);
	}

	@Override
	public String getReplaceString(String name, Map<String, String> attr, int phaseNo)
	{
		return null;
	}

	protected Object getValue(String key)
	{
		return nameToValue.get(key);
	}

	public void addValue(String name, Object value)
	{
		nameToValue.put(name, value);
	}

	@Override
	public void reinitalize(ObjectCacheFactory<MapQueryFilter> arg0)
	{
		nameToValue.clear();
		requiredTables.clear();
		tabToReqTab.clear();
		nullString = "NULL";
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder(super.toString());
		builder.append(nameToValue);
		return builder.toString();
	}
}
