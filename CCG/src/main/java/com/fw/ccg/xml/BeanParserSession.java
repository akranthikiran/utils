package com.fw.ccg.xml;

import java.util.HashMap;

import com.fw.ccg.core.SimpleAttributedBean;

public class BeanParserSession extends SimpleAttributedBean
{
	private static final long serialVersionUID=1L;
	private static HashMap<Thread,BeanParserSession> threadToInstance=new HashMap<Thread,BeanParserSession>();
	
	private Object rootBean;
	
		private BeanParserSession(Object rootBean)
		{
			this.rootBean=rootBean;
			threadToInstance.put(Thread.currentThread(),this);
		}
		
		public Object getRootBean()
		{
			return rootBean;
		}
		
		
		static BeanParserSession createSession(Object rootBean)
		{
			return new BeanParserSession(rootBean); 
		}
		
		static void destroyCurrentSession()
		{
			threadToInstance.remove(Thread.currentThread());
		}
		
		public static BeanParserSession getCurrentSession()
		{
			Thread th=Thread.currentThread();
			return threadToInstance.get(th);
		}

}
