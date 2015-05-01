package com.fw.ccg.xml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class WriteableBeanNode
{
		public static WriteableBeanNode createTextNode(String name,Object value)
		{
			WriteableBeanNode node=new WriteableBeanNode(name,value,true);
			return node;
		}
		
		public static WriteableBeanNode createTextNode(String name,String idName,Object id,Object value)
		{
			WriteableBeanNode node=new WriteableBeanNode(name,value,true);
			
				if(id!=null)
					node.setId(idName,id);
				
			return node;
		}
	
	private String name;
	private ArrayList<WriteableBeanNode> childNodes;
	private LinkedHashMap<String,Object> attributes;
	private WriteableBeanNode parent;
	private Object bean;
	private boolean textNode=false;
	private Object id=null;
	private String idName;
		
		public WriteableBeanNode(String name,Object bean)
		{
			this(name,bean,false);
		}
		
		private WriteableBeanNode(String name,Object bean,boolean txtNode)
		{
				if(name==null || name.trim().length()==0)
					throw new IllegalArgumentException("Name can not be null or empty String.");
				
				if(bean==null)
					throw new IllegalArgumentException("Bean can not be null.");
				
			this.name=name;
			this.bean=bean;
			textNode=txtNode;
		}
		
		public void addChild(WriteableBeanNode node)
		{
				if(childNodes==null)
					childNodes=new ArrayList<WriteableBeanNode>();
				
			childNodes.add(node);
			node.setParent(this);
		}
		
		public WriteableBeanNode addTextNode(String name,Object value)
		{
			WriteableBeanNode node=new WriteableBeanNode(name,value,true);
			addChild(node);
			return node;
		}
		
		public boolean containsAttribute(String name)
		{
				if(attributes==null)
					return false;
				
			return attributes.containsKey(name);
		}
		
		public Object getAttribute(String name)
		{
				if(attributes==null)
					return null;
			return attributes.get(name);
		}
		
		public int getAttributeCount()
		{
				if(attributes==null)
					return 0;
			return attributes.size();
		}
		
		public LinkedHashMap<String,Object> getAttributeMap()
		{
				if(attributes==null)
					return null;
				
			return new LinkedHashMap<String,Object>(attributes);
		}
		
		public Object getBean()
		{
			return bean;
		}
		
		public int getChildCount()
		{
				if(childNodes==null)
					return 0;
				
			return childNodes.size();
		}
		
		public List<WriteableBeanNode> getChildNodes()
		{
				if(childNodes==null)
					return null;
				
			return new ArrayList<WriteableBeanNode>(childNodes);
		}
		
		public Object getId()
		{
			return id;
		}
		
		public String getName()
		{
			return name;
		}
		
		public WriteableBeanNode getParent()
		{
			return parent;
		}

		public boolean isRoot()
		{
			return (parent==null);
		}
		
		public boolean isTextNode()
		{
			return textNode;
		}

		public void removeAttribute(String name)
		{
				if(attributes==null)
					return;
				
			attributes.remove(name);
		}
		
		public void removeChild(WriteableBeanNode node)
		{
				if(childNodes==null)
					return;
				
			childNodes.remove(node);
			
				if(node.parent==this)
					node.setParent(null);
		}

		public void setAttribute(String name,Object value)
		{
				if(attributes==null)
					attributes=new LinkedHashMap<String,Object>();
				
			attributes.put(name,value);
		}

		public void setBean(Object bean)
		{
			this.bean=bean;
		}
		
		public void setId(String idName,Object id)
		{
			this.idName=(idName==null)?"ID":idName;
			this.id=id;
		}

		public void setName(String name)
		{
				if(name==null || name.length()==0)
					throw new NullPointerException("Name can not be null or empty String.");
				
			this.name=name;
		}
		
		private void setParent(WriteableBeanNode node)
		{
			parent=node;
		}

		public Element toXMLNode(Document doc,WriterHandler handler,Map<String,String> nsMap)
		{
			Element node=doc.createElement(name);
			
				if(id!=null)
				{
					String idValue=""+handler.getIDString(this,id);
					
					node.setAttribute(idName,idValue);
				}
				
				if(textNode)
				{
					String nodeValue=handler.getAttributedString(parent,name,bean,false);
					
						if(handler.isCdataNode(parent,name,bean))
						{
							node.appendChild(doc.createCDATASection(nodeValue));
							return node;
						}
					
					node.appendChild(doc.createTextNode(nodeValue));
					return node;
				}
				
				if(attributes!=null && attributes.size()>0)
				{
					Iterator<String> names=attributes.keySet().iterator();
					String name=null;
					Object value=null;
					String valueStr=null;
					
						while(names.hasNext())
						{
							name=names.next();
							value=attributes.get(name);
							valueStr=handler.getAttributedString(this,name,value,true);
							
							node.setAttribute(name,valueStr);
						}
				}
				
				if(childNodes!=null && childNodes.size()>0)
				{
					Iterator<WriteableBeanNode> it=childNodes.iterator();
					WriteableBeanNode childNode=null;
						while(it.hasNext())
						{
							childNode=it.next();
							node.appendChild(childNode.toXMLNode(doc,handler,nsMap));
						}
				}
			
			return node;
		}
		
		public void toXMLStream(XMLStreamWriter xout,WriterHandler handler,Map<String,String> nsMap,boolean endNode) throws XMLStreamException
		{
			xout.writeStartElement(name);
			 
				if(id!=null)
				{
					String idValue=""+handler.getIDString(this,id);
					
					xout.writeAttribute(idName,idValue);
				}
				
				if(textNode)
				{
					String nodeValue=handler.getAttributedString(parent,name,bean,false);
					
						if(handler.isCdataNode(parent,name,bean))
							xout.writeCData(nodeValue);
						else
							xout.writeCharacters(nodeValue);
					
						if(endNode)
							xout.writeEndElement();
						
					return;
				}
				
				if(attributes!=null && attributes.size()>0)
				{
					Iterator<String> names=attributes.keySet().iterator();
					String name=null;
					Object value=null;
					String valueStr=null;
					
						while(names.hasNext())
						{
							name=names.next();
							value=attributes.get(name);
							valueStr=handler.getAttributedString(this,name,value,true);
							
							xout.writeAttribute(name,valueStr);
						}
				}
				
				if(childNodes!=null && childNodes.size()>0)
				{
					Iterator<WriteableBeanNode> it=childNodes.iterator();
					WriteableBeanNode childNode=null;
						while(it.hasNext())
						{
							childNode=it.next();
							childNode.toXMLStream(xout,handler,nsMap,true);
						}
				}
				
				if(endNode)
					xout.writeEndElement();
		}
		
		public String getNodePath()
		{
			WriteableBeanNode node=this;
			StringBuffer buff=new StringBuffer();
			
				while(node!=null)
				{
					buff.insert(0,"/");
					buff.insert(0,node);
					node=node.getParent();
				}
			return buff.toString();
		}
		
		public String toString()
		{
			Object idObj=id;
			
				if(attributes!=null)
				{
					if(idObj==null)
						idObj=attributes.get("id");
					
					if(idObj==null)
						idObj=attributes.get("name");
				}
					
			String id=(idObj!=null)?"("+idObj+")":"";
				
			return name+id;
		}
}
