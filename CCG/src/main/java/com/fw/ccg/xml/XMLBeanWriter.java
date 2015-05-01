package com.fw.ccg.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.fw.ccg.util.ArrayIterator;
import com.fw.ccg.util.CCGUtility;

public class XMLBeanWriter
{
	public static final int SKIP_CCG_NS=1;
	public static final int SKIP_ROOT_BEAN_TYPE=2;
	
	private WriterHandler handler;
	private HashMap<Class<?>,Map<String,BeanProperty>> typeToProp=new HashMap<Class<?>,Map<String,BeanProperty>>();
	private Document doc;
	private HashMap<String,String> nsMap=new HashMap<String,String>();
	
		private XMLBeanWriter()
		{
			nsMap.put(XMLConstants.CCG_URI,"ccg");
		}
	
		protected XMLBeanWriter(WriterHandler handler)
		{
			this();
			
				if(handler==null)
					throw new NullPointerException("Handler can not be null.");
				
			this.handler=handler;
			
				try
				{
					this.doc=DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
				}catch(Exception ex)
				{
					throw new IllegalStateException("Error in creating new document object.");
				}
		}
		
		protected Map<String,String> getNsMap()
		{
			return nsMap;
		}
		
		protected Document getDocument()
		{
			return doc;
		}
		public static void writeXML(Object rootBean,OutputStream out,String rootName)
		{
			writeXML(rootBean,out,new DefaultWriterHandler(rootName),null,0);
		}
		
		public static void writeXML(Object rootBean,OutputStream out,String rootName,int flags)
		{
			writeXML(rootBean,out,new DefaultWriterHandler(rootName),null,flags);
		}
		
		public static void writeXML(Object rootBean,OutputStream out,WriterHandler handler)
		{
			writeXML(rootBean,out,handler,null,0);
		}
		
		public static void writeXML(Object rootBean,OutputStream out,WriterHandler handler,int flags)
		{
			writeXML(rootBean,out,handler,null,flags);
		}
		
		public static void writeXML(Object rootBean,OutputStream out,String rootName,XMLFormatter formatter)
		{
			writeXML(rootBean,out,new DefaultWriterHandler(rootName),formatter,0);
		}
		
		public static void writeXML(Object rootBean,OutputStream out,String rootName,XMLFormatter formatter,int flags)
		{
			writeXML(rootBean,out,new DefaultWriterHandler(rootName),formatter,flags);
		}
		
		public static void writeXML(Object rootBean,OutputStream out,WriterHandler handler,XMLFormatter formatter)
		{
			writeXML(rootBean,out,handler,formatter,0);
		}
		
		public static void writeXML(Object rootBean,OutputStream out,WriterHandler handler,XMLFormatter formatter,int flags)
		{
				if(out==null)
					throw new NullPointerException("Output stream can not be null.");
				
			Document doc=createDocument(rootBean,handler,flags);
				
				if(formatter==null)
					formatter=new DOMFormatter();

				try
				{
					formatter.writeTo(doc.getDocumentElement(),out);
				}catch(IOException ex)
				{
					throw new XMLWriteException(null,"Error in writing XML to output stream.",ex);
				}
		}
		
		public static Document createDocument(Object rootBean,String rootName)
		{
				if(rootName==null || rootName.trim().length()==0)
					throw new NullPointerException("Root node name can not be null or empty string.");
				
			return createDocument(rootBean,new DefaultWriterHandler(rootName),0);
		}
		
		public static Document createDocument(Object rootBean,String rootName,int flags)
		{
				if(rootName==null || rootName.trim().length()==0)
					throw new NullPointerException("Root node name can not be null or empty string.");
				
			return createDocument(rootBean,new DefaultWriterHandler(rootName),flags);
		}
		
		public static Document createDocument(Object rootBean,WriterHandler handler,int flags)
		{
				if(rootBean==null)
					throw new NullPointerException("Root bean can not be null.");
				
				if(handler==null)
					throw new NullPointerException("Handler can not be null.");
				
			XMLBeanWriter beanWriter=new XMLBeanWriter();
			Document doc=beanWriter.buildDocument(rootBean,handler,flags);
			return doc;
		}
		
		private Document buildDocument(Object rootBean,WriterHandler handler,int flags)
		{
			boolean skipCcgNs=((flags & SKIP_CCG_NS)==SKIP_CCG_NS);
			boolean skipBeanType=((flags & SKIP_ROOT_BEAN_TYPE)==SKIP_ROOT_BEAN_TYPE);
			
			this.handler=handler;
			String rootName=handler.getRootName();
			
				try
				{
					doc=DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
					
					WriteableBeanNode root=new WriteableBeanNode(rootName,rootBean);
					populateElement(root);
					handler.customize(root,null,nsMap);
					
					Element rootElement=root.toXMLNode(doc,handler,nsMap);
					
						if(!skipCcgNs)
							rootElement.setAttribute("xmlns:ccg",XMLConstants.CCG_URI);
						
						if(!skipBeanType)
							rootElement.setAttribute("ccg:beanType",rootBean.getClass().getName());
						
					doc.appendChild(rootElement);
					
					return doc;
				}catch(XMLWriteException ex)
				{
					throw ex;
				}catch(Exception ex)
				{
					throw new XMLWriteException(null,"Error in writing XML Document",ex);
				}
		}
		
		protected WriterHandler getHandler()
		{
			return handler;
		}
		
		private Map<String,BeanProperty> getPropertyMap(Class<?> type)
		{
			Map<String,BeanProperty> propMap=typeToProp.get(type);
			
				if(propMap==null)
				{
					propMap=BeanProperty.loadProperties(type);
					typeToProp.put(type,propMap);
				}
				
			return propMap;
		}
		
		/**
		 * Consider following property
		 * 		void setValue(String name,Object value)
		 * 		Map<String,Object> getValueMap()
		 * 
		 * Let say one of the map entry is like size=20, while writing XML we will get this value
		 * as Integer. And If this value is writtern as attribute, while loading XML, we cant determine
		 * the actual type of the Object was during writing. NOTE: while loading the attribute type is
		 * determioned based on setter type (which is Object here whereas for exact reload it should be
		 * Integer in this case).
		 * 
		 * The only way to overcome such situations is to make such values as text nodes and attach beanType
		 * to it. So that while loading back the data will be interpreter based on beanType and not on 
		 * setter type.
		 * 
		 * This function isFeasibleAttribute() will return true only if there will be no problem in loading
		 * the current attribute (setter type and "value" type should be same). Even though the handler says 
		 * a particular property yo be attribute, if feasibility fails, its made into text node. 
		 * 
		 * @param prop
		 * @param value
		 * @return
		 */
		private boolean isFeasibleAttribute(BeanProperty prop,Object value)
		{
			Class<?> expectedType=prop.getSetterType();
			Class<?> valueType=value.getClass();
			
				if(expectedType.isPrimitive())
					expectedType=CCGUtility.getWrapperClass(expectedType);
				
			//NOTE: here since value is not primitive, valueType can 
				//not be of primitive type
			
			return expectedType.equals(valueType);
		}
		
		@SuppressWarnings("unchecked")
		private void processProperty(BeanProperty prop,WriteableBeanNode parentNode,Map<String,BeanProperty> propMap,boolean removeProp)
		{
			Object resValue=prop.invokeGetter(parentNode.getBean());
			
				if(resValue==null || resValue.equals(parentNode.getBean()))
					return;
				
			WriteableBeanNode childNode=handler.toBeanNode(prop,resValue);
			
				if(childNode!=null)
				{
					parentNode.addChild(childNode);
					return;
				}
				
			String propName=prop.getName();
			
				if(!prop.isIdBased() && 
							handler.isAttribute(parentNode,prop,resValue) && 
							isFeasibleAttribute(prop,resValue))
				{
						if(!parentNode.containsAttribute(propName) && !handler.isCdataNode(parentNode,propName,resValue))
						{
							parentNode.setAttribute(propName,resValue);
						}
						else
						{
							parentNode.addTextNode(propName,resValue);
						}
						
						if(removeProp)
							propMap.remove(propName.toUpperCase());
						
					return;
				}
			
			Object propBean=resValue;
			Class<?> propType=propBean.getClass();
			
			Iterator<?> propBeanIt=null;
			Map<String,BeanProperty> propBeanMap=null;
			
				if(propType.isArray())
				{
					propBeanIt=new ArrayIterator((Object[])propBean);
				}
				else if(propBean instanceof Iterator)
				{
					propBeanIt=(Iterator<?>)propBean;
				}
				else if(propBean instanceof Collection)
				{
					propBeanIt=((Collection<?>)propBean).iterator();
				}
				else if(propBean instanceof Map)
				{
					propBeanMap=(Map<String,BeanProperty>)propBean;
					propBeanIt=propBeanMap.keySet().iterator();
				}
				else
				{
					propBeanIt=new ArrayIterator(new Object[]{propBean});
				}
				
			Object childBean=null;
			Object key=null;
				
				while(propBeanIt.hasNext())
				{
					childBean=propBeanIt.next();
					
						if(childBean==null)
							continue;
						
						if(propBeanMap==null)
						{
							childNode=createNode(prop,null,childBean,parentNode);
						}
						else
						{
							key=childBean;
							childBean=propBeanMap.get(key);
							
							childNode=createNode(prop,key,childBean,parentNode);
						}
							
							
					handler.customize(childNode,prop,nsMap);
				}
				
				if(removeProp)
					propMap.remove(propName.toUpperCase());
		}
		
		private WriteableBeanNode createNode(BeanProperty prop,Object id,Object bean,WriteableBeanNode parentNode)
		{
			Class<?> beanType=bean.getClass();
			String idName=handler.getIDName(parentNode,prop,bean,nsMap);
			
			WriteableBeanNode node=null;
			
				if(XMLUtil.isSupportedAttributeClass(beanType))
				{
					node=WriteableBeanNode.createTextNode(prop.getName(),idName,id,bean);
					
						if(parentNode!=null)
							parentNode.addChild(node);
				}
				else
				{
					node=new WriteableBeanNode(prop.getName(),bean);
					
						if(id!=null)
							node.setId(idName,id);
						
						if(parentNode!=null)
							parentNode.addChild(node);
						
					populateElement(node);
				}
			
			
			return node;
		}
		
		protected void populateElement(WriteableBeanNode node)
		{
			Object bean=node.getBean();
			Class<?> beanType=bean.getClass();
			TreeMap<String,BeanProperty> propMap=new TreeMap<String,BeanProperty>(getPropertyMap(beanType));
			Set<String> primPropSet=handler.getPrimaryProperties(node);
			BeanProperty prop=null;
			
				if(primPropSet!=null && primPropSet.size()>0)
				{
						for(String propName:primPropSet)
						{
							prop=propMap.get(propName.toUpperCase());
								
								if(prop==null)
									throw new XMLWriteException(node,"No property exist in type \""+beanType.getName()+
													"\" with specified prime property: "+propName);	
							
								if(!handler.isWriteableProperty(node,prop))
									continue;
									
							processProperty(prop,node,propMap,true);
						}
				}
				
				for(String propName:propMap.keySet())
				{
					prop=propMap.get(propName);
					
						if(!prop.isCCGXMLSupported())
							continue;
						
						if(!handler.isWriteableProperty(node,prop))
							continue;
							
					processProperty(prop,node,propMap,false);
				}
				
		}
}
