package com.fw.ccg.xml;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.fw.ccg.util.CCGUtility;

public class DefaultWriterHandler implements WriterHandler
{
	private int maxAttributeCount=10;
	private String dateFormat="MM/dd/yyyy";
	private HashMap<String,LinkedHashSet<String>> nameToPropSet=null;
	private HashMap<String,Set<String>> nameToAttr=null;
	private HashMap<String,Set<String>> nameToNodes=null;
	private HashMap<String,Set<String>> nameToCdata=null;
	private String rootName;
	
		public DefaultWriterHandler(String rootName)
		{
				if(rootName==null || rootName.trim().length()==0)
					throw new NullPointerException("Root name can not be null or empty.");
				
			this.rootName=rootName;
		}
	
		public String getRootName()
		{
			return rootName;
		}
	
		public void addPropSet(String name,String... prop)
		{
			LinkedHashSet<String> set=new LinkedHashSet<String>();
			
				for(int i=0;i<prop.length;i++)
					set.add(prop[i].toUpperCase());
				
				if(nameToPropSet==null)
					nameToPropSet=new HashMap<String,LinkedHashSet<String>>();
				
			nameToPropSet.put(name,set);
		}
		
		public void addAttributeSet(String name,String attr[])
		{
			HashSet<String> set=null;
			
				if(attr!=null && attr.length>0)
				{
					set=new HashSet<String>();
					
						for(int i=0;i<attr.length;i++)
							set.add(attr[i].toUpperCase());
				}
				
				if(nameToAttr==null)
					nameToAttr=new HashMap<String,Set<String>>();
				
			nameToAttr.put(name,set);
		}
		
		public void addNodeSet(String name,String nodes[])
		{
			HashSet<String> set=null;
			
				if(nodes!=null && nodes.length>0)
				{
					set=new HashSet<String>();
					
						for(int i=0;i<nodes.length;i++)
							set.add(nodes[i].toUpperCase());
				}
				
				if(nameToNodes==null)
					nameToNodes=new HashMap<String,Set<String>>();
				
			nameToNodes.put(name,set);
		}
	
	
		public void setDateFormat(String format)
		{
			new SimpleDateFormat(format);
			
			dateFormat=format;
		}
	
		public String getDateFormat()
		{
			return dateFormat;
		}
	
		public boolean isAttribute(WriteableBeanNode node,BeanProperty prop,Object attrValue)
		{
			String propName=prop.getName();
			propName=propName.toUpperCase();
			
				if(nameToAttr!=null || nameToNodes!=null)
				{
					String nodeName=node.getName();
					
						if(nameToAttr!=null && nameToAttr.containsKey(nodeName))
						{
							HashSet<String> set=(HashSet<String>)nameToAttr.get(nodeName);
							
								if(set==null || set.contains(propName))
									return true;
						}
						
						if(nameToNodes!=null && nameToNodes.containsKey(nodeName))
						{
							HashSet<String> set=(HashSet<String>)nameToNodes.get(nodeName);
							
								if(set==null || set.contains(propName))
									return false;
						}
					
				}
			
			int curAttrCount=node.getAttributeCount();
			
				if(curAttrCount>maxAttributeCount)
					return false;
				
				if(XMLUtil.isSupportedAttributeClass(attrValue.getClass()))
				{
						if(attrValue instanceof String)
							return ((String)attrValue).indexOf("\n")<0;
							
						if(attrValue instanceof StringBuffer)
							return ((StringBuffer)attrValue).indexOf("\n")<0;
						
					return true;
				}
				
			return false;
		}
		
		public String getAttributedString(WriteableBeanNode parent,String name,Object value,boolean forAttribute)
		{
				if(value==null)
					return "null";
				
			return XMLUtil.formatAttributeObject(value,dateFormat);
		}
		
		public Set<String> getPrimaryProperties(WriteableBeanNode node)
		{
				if(nameToPropSet!=null)
				{
					String nodeName=node.getName();
					
						if(nameToPropSet.containsKey(nodeName))
							return nameToPropSet.get(nodeName);
				}
			return null;
		}
		
		public boolean isWriteableProperty(WriteableBeanNode node,BeanProperty prop)
		{
			Object bean=node.getBean();
				if(bean instanceof WriteablePropertyBean)
				{
					int res=((WriteablePropertyBean)bean).isWriteableProperty(node,prop);
					
						if(res==WriteablePropertyBean.WRITEABLE_PROPERTY)
							return true;
						
						if(res==WriteablePropertyBean.NOT_WRITEABLE_PROPERTY)
							return false;
				}
			return true;
		}

		public void setMaxAttributeCount(int maxAttributeCount)
		{
			this.maxAttributeCount=maxAttributeCount;
		}
		
		public int getMaxAttributeCount()
		{
			return maxAttributeCount;
		}

		public void customize(WriteableBeanNode node,BeanProperty property,Map<String,String> nsMap)
		{
				if(node.isRoot())
					return;
				
			String ccgPrefix=nsMap.get(CCG_URI);
			Class<?> expectedType=property.getSetterType();
			Class<?> beanType=node.getBean().getClass();
			
			Object bean=node.getBean();
			String name=node.getName();
			
				if(bean instanceof NamedNodeBean)
				{
					name=((NamedNodeBean)bean).getNodeName();
					
						if(name!=null && name.trim().length()>0)
							node.setName(name);
				}
				
				if(bean instanceof TypeControlledBean)
				{
					Class<?> type=((TypeControlledBean)bean).getExpectedType();
					
						if(!type.isAssignableFrom(beanType))
						{
							throw new XMLWriteException(node,"Incompatible type is returned by  TypeControlledBean: "+type.getName()+" \n" +
									"Expected Type: "+expectedType.getName()+"\n" +
									"Bean Type: "+beanType.getName());
						}
					
						if(type!=null)
							expectedType=type;
				}
			
			
			String parentType=node.getParent().getBean().getClass().getName();
			
				if(expectedType.isPrimitive())
					expectedType=CCGUtility.getWrapperClass(expectedType);
			
				if(!expectedType.equals(beanType))
				{
						if(!expectedType.isAssignableFrom(beanType))
							throw new XMLWriteException(node,"Setter and Getter mismatch occured for property \""+name+"\" in type \""+parentType+"\" \n" +
										"Expected Type: "+expectedType.getName()+"\n" +
										"Bean Type: "+beanType.getName());
						
					node.setAttribute(ccgPrefix+":beanType",beanType.getName());
				}
			
		}

		public String getIDName(WriteableBeanNode parentNode,BeanProperty prop,Object bean,Map<String,String> nsMap)
		{
				if(bean instanceof WriteablePropertyBean)
				{
					String res=((WriteablePropertyBean)bean).getIDName(parentNode,prop,bean,nsMap);
					return res;
				}
				
			return null;
		}

		public String getIDString(WriteableBeanNode node,Object idValue)
		{
				if(idValue==null)
					return "null";
				
			return XMLUtil.formatAttributeObject(idValue,dateFormat);
		}
		
		public WriteableBeanNode toBeanNode(BeanProperty prop,Object value)
		{
			return null;
		}
		
		public void addCdataNode(String parentName,String name)
		{
				if(nameToCdata==null)
					nameToCdata=new HashMap<String,Set<String>>();
				
			Set<String> names=nameToCdata.get(parentName);
			
				if(names==null)
				{
					names=new HashSet<String>();
					nameToCdata.put(parentName,names);
				}
	
			names.add(name);
		}

		public boolean isCdataNode(WriteableBeanNode parent,String name,Object value)
		{
				if(nameToCdata==null)
					return false;
				
			String parentName=parent.getName();
			Set<String> names=nameToCdata.get(parentName);
			
				if(names==null)
					return false;
				
			return names.contains(name);
		}
	
}
