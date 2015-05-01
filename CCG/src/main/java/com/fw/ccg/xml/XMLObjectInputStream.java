package com.fw.ccg.xml;

import java.io.InputStream;
import java.util.NoSuchElementException;

import javax.xml.stream.XMLStreamConstants;


public class XMLObjectInputStream
{
	public static final Object ROOT_BEAN=new Object();
	
		private class XMLObjectHandler extends DefaultParserHandler
		{
			private Object bean;
			private Object parent;
			private String name;
			private String parentName;
			
				public String getNodeDescription(BeanNode node, XMLAttributeMap att)
				{
					return node.getName();
				}

				public Object processReservedNode(BeanNode node,XMLAttributeMap att)
				{
					System.out.println("Yes....");
						if(!"object".equalsIgnoreCase(node.getName()))
							return super.processReservedNode(node,att);
						
					String beanType=att.getReserved("objType",null);
					
						if(beanType==null)
							throw new IllegalStateException("No object type found.");
						
						try
						{
							Class<?> cls=Class.forName(beanType);
							Object bean=cls.newInstance();
							setObject(node,bean,att);
							return bean;
						}catch(Exception ex)
						{
							throw new IllegalStateException("An error occured while creating bean of type: "+beanType,ex);
						}
				}
			
				private void setObject(BeanNode node,Object bean,XMLAttributeMap att)
				{
						if(!"object".equalsIgnoreCase(node.getName()))
							super.processReserveNodeEnd(node,att);
						
					String name=att.getReserved("name",null);
					BeanNode parentNode=node.getParentNode();
					Object parent=null;
					String parentName=null;
						if(parentNode!=null)
						{
							parent=node.getParent();
							parentName=parentNode.getAttributeMap().getReserved("name",null);
						}
						else
						{
							parent=ROOT_BEAN;
							parentName=null;
						}
						
					this.bean=bean;
					this.parent=parent;
					this.name=name;
					this.parentName=parentName;
					
					XMLObjectInputStream.this.next=true;
				}
				
				public void loadCurrent()
				{
					XMLObjectInputStream.this.bean=bean;
					XMLObjectInputStream.this.parent=parent;
					XMLObjectInputStream.this.beanName=name;
					XMLObjectInputStream.this.parentName=parentName;
					
					this.bean=null;
					this.parent=null;
					this.name=null;
					this.parentName=null;
				}
				
				public boolean hasBean()
				{
					return (bean!=null);
				}
				
		}
		
	private Object bean;
	private Object parent;
	private String beanName;
	private String parentName;
	
	private boolean next=false;
	private boolean completed=false;
	private SAXStreamBridge bridge;
	private XMLObjectHandler handler;
	
		public XMLObjectInputStream(InputStream is)
		{
			handler=new XMLObjectHandler();
			SAXEventHandler saxHandler=new SAXEventHandler(handler,new Object());
			bridge=new SAXStreamBridge(is,saxHandler);
			
			parseTillNext();
		}
		
		private void parseTillNext()
		{
			this.next=false;
				while(bridge.parseTill(XMLStreamConstants.START_ELEMENT))
				{
						if(next)
							break;
				}
		}
	
		public boolean next()
		{
				if(!handler.hasBean())
				{
					completed=true;
					return false;
				}
				
			handler.loadCurrent();
			parseTillNext();
			return true;
		}

		public Object getBean()
		{
				if(completed)
					throw new NoSuchElementException();
				
			return bean;
		}

		public Object getParent()
		{
				if(completed)
					throw new NoSuchElementException();
			
			return parent;
		}

		public String getParentName()
		{
				if(completed)
					throw new NoSuchElementException();
			
			return parentName;
		}

		public String getBeanName()
		{
				if(completed)
					throw new NoSuchElementException();
			
			return beanName;
		}

		public boolean isCompleted()
		{
			return completed;
		}
		
}
