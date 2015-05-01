package com.fw.dao.qry.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.fw.ccg.util.ICacheableBean;
import com.fw.ccg.util.ObjectCacheFactory;
import com.fw.dao.qry.BulkQueryFilter;

public class BulkValueQueryFilter<T> extends MapQueryFilter implements BulkQueryFilter, ICacheableBean<MapQueryFilter>
{
	private Iterator<T> it;
	private T curValue;
	private int commitCount;
	private Class<T> type;
	
	private Map<String, T> valueMap = new HashMap<String, T>();
	
	private Map<String,Object> extraProperties = new HashMap<String,Object>();

		public BulkValueQueryFilter(Class<T> type)
		{
			this.type = type;
		}
	
		public BulkValueQueryFilter(Iterable<T> beans, Class<T> type)
		{
			this(beans.iterator(),-1, type);
		}
	
		public BulkValueQueryFilter(Iterator<T> beans, Class<T> type)
		{
			this(beans,-1, type);
		}
		
		public BulkValueQueryFilter(Iterator<T> it,int commitCount, Class<T> type)
	    {
				if(it==null)
					throw new NullPointerException("Value iterable cannot be null or empty");
			
				if(!it.hasNext())
					throw new NullPointerException("Value iterable cannot be null or empty");
				
			this.commitCount=commitCount;
			this.type = type;
			
			setValues(it);
	    }
		
		public BulkValueQueryFilter<T> setValues(Iterator<T> it)
		{
			if(it==null)
				throw new NullPointerException("Value iterable cannot be null or empty");
		
			if(!it.hasNext())
				throw new NullPointerException("Value iterable cannot be null or empty");
			
			this.it=it;
			curValue = getNotNullValue();
			
			return this;
		}
		
		private T getNotNullValue()
		{
			T value = null;
			
			while(it.hasNext())
			{
				value = it.next();
				
				if(value != null)
				{
					return value;
				}
			}
			
			return null;
		}
	
		@Override
	    public Class<?> getParamType(String paramName)
	    {
		    return type;
	    }
	
		@Override
	    public boolean next()
	    {
			if(this.curValue == null)
			{
				return false;
			}
				
			T value = this.curValue;
			curValue = getNotNullValue();
			
			valueMap.put("value", value);
			super.setParamMap(valueMap);
			return true;
	    }
		
		@Override
        protected Object getValue(String key)
        {
			Object value = extraProperties.get(key);
			
			if(value != null)
			{
				return value;
			}
			
			return super.getValue(key);
        }

		@Override
        public int getCommitCount()
        {
	        return commitCount;
        }

		public void addExtraProperty(String name, Object value)
		{
			extraProperties.put(name, value);
		}

		@Override
		public void reinitalize(ObjectCacheFactory<MapQueryFilter> arg0)
		{
			super.reinitalize(arg0);
			extraProperties.clear();
		}
}
