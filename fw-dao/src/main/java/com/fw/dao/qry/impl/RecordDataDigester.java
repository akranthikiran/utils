package com.fw.dao.qry.impl;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.fw.dao.qry.DataDigester;
import com.fw.dao.qry.FunctionInstance;
import com.fw.dao.qry.QueryResultData;
import com.fw.dao.qry.QueryResultDataProvider;

public class RecordDataDigester implements DataDigester<Record>
{
	private static final String CONSTR_KEY = "RecordDataDigester$Construcotr#";
	private static final String FUNC_INST_KEY = "RecordDataDigester$funcInst#";

	public static final String QRY_PARAM_FIELDS_TO_CONVERT = "fieldsToConvert";
	public static final String QRY_PARAM_CONVERT_FUNC = "convertFunctions";

	@SuppressWarnings("unchecked")
	private Map<String, Integer> getFieldsToConvert(QueryResultData rsData)
	{
		Map<String, Integer> fieldLstMap = (Map<String, Integer>)rsData.getQueryAttribute(CONSTR_KEY);

		if(fieldLstMap != null)
		{
			return fieldLstMap;
		}

		String fieldsToConvert = rsData.getQueryParam(QRY_PARAM_FIELDS_TO_CONVERT);

		if(fieldsToConvert == null)
		{
			return null;
		}

		fieldsToConvert = fieldsToConvert.trim();

		String fieldLst[] = fieldsToConvert.split("\\s*\\,\\s*");
		fieldLstMap = new HashMap<String, Integer>();
		
		for(int i = 0; i < fieldLst.length; i++)
		{
			fieldLstMap.put(fieldLst[i], i);
		}
		
		rsData.setQueryAttribute(CONSTR_KEY, fieldLstMap);
		return fieldLstMap;
	}

	private FunctionInstance getFieldsFunctionInstance(QueryResultData rsData)
	{
		FunctionInstance inst = (FunctionInstance)rsData.getQueryAttribute(FUNC_INST_KEY);

		if(inst != null)
		{
			return inst;
		}

		String parsmStr = rsData.getQueryParam(FUNC_INST_KEY);

		if(parsmStr == null || parsmStr.trim().length() == 0)
		{
			throw new IllegalStateException("No convert functions are defined: " + QRY_PARAM_CONVERT_FUNC);
		}

		FunctionInstance funcInst = FunctionInstance.parse("<init>", parsmStr, true, true);
		rsData.setQueryAttribute(FUNC_INST_KEY, funcInst);
		return funcInst;
	}

	@Override
	public Record digest(QueryResultData rsData) throws SQLException
	{
		Map<String, Integer> fieldsToConvert = getFieldsToConvert(rsData);
		Object paramValues[] = null;
		
		if(fieldsToConvert != null)
		{
			FunctionInstance funcInst = getFieldsFunctionInstance(rsData);
			
			if(funcInst.getParamCount() != fieldsToConvert.size())
			{
				throw new IllegalStateException("Field count and convert count are not matching in query");
			}
			
			paramValues = funcInst.getParamValues(new QueryResultDataProvider(rsData));
		}
		
		String colNames[] = rsData.getColumnNames();
		int len = colNames.length;
		Record rec = new Record(len);
		Integer index = null;
		Object value = null;
		
		for(int i = 0, j = 1; i < len; i++, j++)
		{
			index = (fieldsToConvert != null) ? fieldsToConvert.get(colNames[i]) : null;
			
			if(index != null)
			{
				value = paramValues[index];
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
