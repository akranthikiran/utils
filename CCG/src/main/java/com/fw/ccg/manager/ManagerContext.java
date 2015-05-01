package com.fw.ccg.manager;

import com.fw.ccg.xml.BeanFactory;

public class ManagerContext
{
	private ManagerParserHandler handler;

		ManagerContext(ManagerParserHandler handler)
		{
			this.handler=handler;
		}
		
		public void addBeanFactory(Class type,Class factoryCls)
		{
			handler.addBeanFactory(type,factoryCls);
		}
		
		public BeanFactory getBeanFactory(Class type)
		{
			return handler.getBeanFactory(type);
		}
		
		public void setDateFormat(String format)
		{
			handler.setDateFormat(format);
		}

}
