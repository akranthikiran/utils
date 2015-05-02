package com.fw.dao.qry.impl;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;

import com.fw.dao.qry.BulkQueryFilter;

public class BulkBeanQueryFilter extends BeanQueryFilter implements BulkQueryFilter
{
	private Iterator<? extends Object> it;
	private Object curBean;
	private int commitCount;
	
		public BulkBeanQueryFilter()
		{}
	
		public BulkBeanQueryFilter(Iterable<? extends Object> beans)
		{
			this(beans.iterator(),-1);
		}
	
		public BulkBeanQueryFilter(Iterator<? extends Object> beans)
		{
			this(beans,-1);
		}
		
		public BulkBeanQueryFilter(Iterator<? extends Object> it,int commitCount)
	    {
			setBeans(it);
			this.commitCount=commitCount;
	    }
		
		public void setBeans(Collection<? extends Object> beans)
		{
			setBeans(beans.iterator());
		}
		
		public void setBeans(Iterator<? extends Object> it)
		{
			if(it == null || !it.hasNext())
			{
				throw new NullPointerException("Beans cannot be null or empty");
			}
		
			super.clear();
			this.it = it;
			curBean = it.next();
			super.loadProperties(curBean);
		}
	
		@Override
	    public Class<?> getParamType(String paramName)
	    {
			Method getter=super.getGetter(paramName);
			
				if(getter==null)
					throw new IllegalArgumentException("No getter found for param-type \""+paramName+"\" in type: "+beanType.getName());
				
		    return getter.getReturnType();
	    }
	
		@Override
	    public boolean next()
	    {
				if(this.curBean==null)
				{
					//super.setBean(null);
					return false;
				}
				
			Object bean=this.curBean;
			curBean=(it.hasNext())?it.next():null;
			
			super.setBean(bean);
			return true;
	    }

		@Override
        public int getCommitCount()
        {
	        return commitCount;
        }
}
