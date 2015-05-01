package com.fw.dao.qry;

import com.fw.dao.qry.FunctionInstance.DataProvider;

public class QueryResultDataProvider implements DataProvider
{
	private QueryResultData rsData;
	
		public QueryResultDataProvider(QueryResultData rsData)
	    {
	        this.rsData=rsData;
	    }
	
		@Override
	    public Object getColumn(String funcName,String name)
	    {
				try
				{
					return rsData.getObject(name);
				}catch(Exception ex)
				{
					throw new IllegalStateException("An error occured while fetching column value: "+name,ex);
				}
	    }
	
		@Override
	    public Object getProperty(String funcName,String name)
	    {
	        return rsData.getProperty(funcName,name);
	    }
}
