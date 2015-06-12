package com.fw.dao.qry.impl;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fw.dao.qry.DataDigester;
import com.fw.dao.qry.FunctionInstance;
import com.fw.dao.qry.QueryResultData;
import com.fw.dao.qry.QueryResultDataProvider;

public class RecordDataDigester implements DataDigester<Record>
{
	private static final String ATTR_FIELD_TO_CONVERT = "RecordDataDigester$FieldsToConvert#";

	public static final String QRY_PARAM_FIELDS_TO_CONVERT = "fieldsToConvert";

	private static final Pattern COL_MAPPING_PATTERN = Pattern.compile("\\s*(\\w+)\\s*\\=\\s*(.+)\\s*");

	@SuppressWarnings("unchecked")
	private Map<String, FunctionInstance> getFieldsToConvert(QueryResultData rsData)
	{
		//get map from cache
		Map<String, FunctionInstance> fieldLstMap = (Map<String, FunctionInstance>)rsData.getQueryAttribute(ATTR_FIELD_TO_CONVERT);

		if(fieldLstMap != null)
		{
			return fieldLstMap;
		}

		//get field mapping param
		String fieldsToConvert = rsData.getQueryParam(QRY_PARAM_FIELDS_TO_CONVERT);

		if(fieldsToConvert == null)
		{
			return null;
		}

		fieldsToConvert = fieldsToConvert.trim();

		fieldLstMap = new HashMap<String, FunctionInstance>();

		StringTokenizer st = new StringTokenizer(fieldsToConvert, "\n");
		String line = null, column = null, colExpr = null;
		Matcher matcher = null;

		//loop through lines
		while(st.hasMoreTokens())
		{
			line = st.nextToken();

			//if empty line ignore
			if(line.trim().length() == 0)
			{
				continue;
			}

			matcher = COL_MAPPING_PATTERN.matcher(line);

			//if line is not matching required pattern ignore
			if(!matcher.matches())
			{
				throw new IllegalStateException("Invalid column-mapping encountered: " + line);
			}

			//extract column name and func expr
			column = matcher.group(1);
			colExpr = matcher.group(2);

			fieldLstMap.put(column, FunctionInstance.parse(colExpr, true, true));
		}

		rsData.setQueryAttribute(ATTR_FIELD_TO_CONVERT, fieldLstMap);
		return fieldLstMap;
	}

	@Override
	public Record digest(QueryResultData rsData) throws SQLException
	{
		Map<String, FunctionInstance> fieldsToConvert = getFieldsToConvert(rsData);

		if(fieldsToConvert == null)
		{
			fieldsToConvert = Collections.emptyMap();
		}

		String colNames[] = rsData.getColumnNames();
		int len = colNames.length;
		Record rec = new Record(len);
		FunctionInstance funcInst = null;
		Object value = null;

		for(int i = 0, j = 1; i < len; i++, j++)
		{
			funcInst = (fieldsToConvert != null)? fieldsToConvert.get(colNames[i]): null;

			if(funcInst != null)
			{
				value = funcInst.invoke(new QueryResultDataProvider(rsData));
			}
			else
			{
				value = rsData.getObject(j);
			}

			rec.set(i, colNames[i], value);
		}

		return rec;
	}

	@Override
	public void finalizeDigester()
	{}
}
