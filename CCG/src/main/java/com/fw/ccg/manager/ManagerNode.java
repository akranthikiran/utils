package com.fw.ccg.manager;

import com.fw.ccg.xml.BeanNode;

public class ManagerNode
{
	private String nodeName;
	private Object bean;
	private Object parent;
	private Class type;
	
		public ManagerNode(BeanNode node)
		{
			this.nodeName=node.getName();
			this.bean=node.getActualBean();
			this.parent=node.getParent();
			this.type=node.getType();
		}
		
		public Object getBean()
		{
			return bean;
		}
		
		public String getNodeName()
		{
			return nodeName;
		}
		
		public Object getParent()
		{
			return parent;
		}
		
		public Class getType()
		{
			return type;
		}
}
