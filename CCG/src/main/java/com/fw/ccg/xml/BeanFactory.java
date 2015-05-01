package com.fw.ccg.xml;

public abstract class BeanFactory
{
	public static final Object SKIP_TO_NORMAL=new Object();
	
		public Object createBean(BeanNode node)
		{
			return DefaultParserHandler.createBean(node,(ClassLoader)null);
		}
		
		public Object createBean(BeanNode node,ClassLoader loader)
		{
			return DefaultParserHandler.createBean(node,loader);
		}
		
		public abstract Object buildBean(Class<?> preferredType,BeanNode node);
		
		public abstract Object buildAttributeBean(Class<?> preferredType,BeanNode node,String attName);
		
		public void finalize()
		{}
}
