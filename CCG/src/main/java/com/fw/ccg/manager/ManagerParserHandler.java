package com.fw.ccg.manager;

import java.io.File;
import java.lang.reflect.Proxy;
import java.util.Map;

import com.fw.ccg.core.Identifiable;
import com.fw.ccg.core.ValidateException;
import com.fw.ccg.core.Validateable;
import com.fw.ccg.ds.CacheMapListener;
import com.fw.ccg.ds.TimeBasedCacheMap;
import com.fw.ccg.util.CCGUtility;
import com.fw.ccg.util.Cache;
import com.fw.ccg.util.StringUtil;
import com.fw.ccg.xml.BeanNode;
import com.fw.ccg.xml.DefaultParserHandler;
import com.fw.ccg.xml.XMLAttributeMap;

/**
 * © Copyright 2006 IBM Corporation
 * <BR><BR>
 * 
 * CCG Parser Handler implementation used by "Manager Builder" (com.ccg.manager.ManagerBuilder). 
 * 
 * <BR><BR>
 * @author A. Kranthi Kiran
 */

class ManagerParserHandler extends DefaultParserHandler
{
		/**
		 * @author A. Kranthi Kiran
		 * This main purpose of this class to set back the manager to the "manager beans" which are
		 * reloaded from the cache.
		 */
		private class BeanCacheListener implements CacheMapListener
		{
				public void objectCached(Object key,Object value)
				{
				}
	
				public void loadedObject(Object key,Object value)
				{
					if(value instanceof ManagerBean)
						((ManagerBean)value).setManager(rootBean);
				}
	
				public void removedRemovable(Object key,Object value)
				{
				}
		}
	
	//reserved attributes supported by root node
	private static final String MGR_HANDLER="managerHandler";
	private static final String DATE_FORMAT="dateFormat";

	//Cache related attributes
	private static final String CACHE_SUPPORT="useCache";
	private static final String CACHE_TYPE="cacheType";
	private static final String FORCE_CACHE="forceCache";
	private static final String CACHE_INTERVAL="cacheInterval";
	private static final String CACHE_PATH="cachePath";
	private static final String CACHE_FILE="cacheFile";
		
	//reserve attributes supported by other nodes
	//bean related attributes
	private static final String BEAN_TYPE="beanType";//this is also used in root node
	private static final String BEAN_ID="id";
	private static final String DEFAULT_FLAG="default";
	
	//reserve nodes and its attributes
	//dynamic method mapping related attributes
	private static final String METHOD_PROXY="methodProxy";
	
	private static final String RN_PROXY_METHOD="proxyMethod";
	private static final String ATT_METHOD_NAME="method";
	
	private static final String RN_METHOD_MAP="methodMap";
	private static final String ATT_PROPERTY="property";
	private static final String ATT_PARAM_STRING="paramString";
	
	private ManagerHandler beanFactory=null;
	private Object rootBean=null;
	private DynamicBeanProxy proxy=null;
	private Map<String,Object> beanMap=null;
	private boolean forceCache=false;

		
		/**
		 * If bean factory is present, processBean() method will be called once the bean is created.
		 * Note, processBean() return value, if not null, will replace the created bean.
		 * <BR>
		 * If the bean created/replaced is "Manager Bean" (com.ccg.manager.ManagerBean), setManager() will be 
		 * called on it.
		 * <BR>
		 * If the bean is "Identifiable" (com.ccg.core.Identifiable), setID() will be called on it.
		 * <BR>
		 * @see com.fw.ccg.xml.ParserHandler#createBean(java.lang.String, java.lang.Object, java.lang.Class, com.fw.ccg.xml.XMLAttributeMap)
		 */
		public Object createBean(BeanNode node,XMLAttributeMap att)
		{
			Object bean=super.createBean(node,att);
				
			boolean def=att.getReservedBoolean(DEFAULT_FLAG,false);
			String id=att.getReserved(BEAN_ID,null);//ID may be null even root-node childs setter properties.
			
				if(beanFactory!=null)
				{
					Object tmpBean=beanFactory.processBean(new ManagerNode(node),att);
						if(tmpBean!=null)
						{
							Class<?> type=node.getType();
								if(!type.isAssignableFrom(tmpBean.getClass()))
									throw new ManagerException("The bean type \""+tmpBean.getClass().getName()
												+"\" returned by processBean() of ManagerHandler is not compatible " +
														"with required type: "+type.getName());
							bean=tmpBean;
						}
				}
			
				if(bean instanceof ManagerBean)
					((ManagerBean)bean).setManager(rootBean);
			
				if(bean instanceof Identifiable)
					((Identifiable)bean).setId(id);
				
			proxy.put(bean,id,def);
			return bean;
		}
		
		/**
		 * XML resource specific root attributes will be loaded by this method. 
		 * @param att Attributes map of the root node in current XML resource.
		 */
		private void loadHandlerConfigurationData(XMLAttributeMap att)
		{
			/*
			 * Check for attributes thats going to be used in loading other node data
			 */
			String dateFormat=att.getReserved(DATE_FORMAT,null);
				if(dateFormat!=null)
					super.setDateFormat(dateFormat);
				else
					super.setDateFormat(STD_DATE_FORMAT);
				
			beanFactory=(ManagerHandler)createObject(att.getReserved(MGR_HANDLER,null));
			
				if(beanFactory!=null)
					beanFactory.init(rootBean,new ManagerContext(this));
			
			/*
			 * Check if method proxy class is defined 
			 */
			String proxyCls=att.getReserved(METHOD_PROXY,null);
				if(proxyCls!=null)
				{
					ManagerMethodProxy methodProxy=null;
					Class cls=null;
						try
						{
							cls=Class.forName(proxyCls);
						}catch(Exception ex)
						{
							throw new ConfigurationException("Invalid method proxy type encountered: "+proxyCls,ex);
						}
						
						if(!ManagerMethodProxy.class.isAssignableFrom(cls))
							throw new ConfigurationException("Specified method proxy class is not compatible with \""+ManagerMethodProxy.class.getName()+"\": "+proxyCls);
						
						try
						{
							methodProxy=(ManagerMethodProxy)cls.newInstance();
						}catch(Exception ex)
						{
							throw new ConfigurationException("Error while creating instance of method proxy of type: "+proxyCls,ex);
						}
						
					proxy.setMethodProxy(methodProxy,rootBean);
				}
				else
					proxy.setMethodProxy(null,rootBean);
			
		}
	
		/**
		 * Sets the specified bean as root bean and loads the XML resource specific configuration data like 
		 * datFormat, beanFactory and methodProxy.
		 *   
		 * @see com.fw.ccg.xml.ParserHandler#setRootBean(java.lang.Object, com.fw.ccg.xml.XMLAttributeMap)
		 */
		public void setRootBean(BeanNode node,XMLAttributeMap att)
		{
			this.rootBean=node.getActualBean();
			//load configuration data need by handler to load current XML file
			loadHandlerConfigurationData(att);
		}
		
		/**
		 * A manager bean will be created with the specified types. This method is responsible for 
		 * creating cache mechanism to be used by the dynamic manager.
		 * <BR><BR>
		 * @see com.fw.ccg.xml.ParserHandler#createRootBean(java.lang.String, com.fw.ccg.xml.XMLAttributeMap)
		 */
		public Object createRootBean(BeanNode node,XMLAttributeMap att)
		{
			/*
			 * Find the interface types that needs to used to create root bean
			 */
			String typeArg=(String)att.getReserved(BEAN_TYPE);
				if(typeArg==null)
					throw new ConfigurationException("Root bean type not specified.");
				
			String strTypes[]=StringUtil.tokenize(typeArg,",",false,true);
				if(strTypes==null || strTypes.length==0)
					throw new ConfigurationException("Root bean type not specified.");
				
			Class types[]=new Class[strTypes.length];
			int i=0;
				try
				{
					for(i=0;i<types.length;i++)
					{
						types[i]=Class.forName(strTypes[i]);
							if(!types[i].isInterface())
								throw  new ConfigurationException("Non-interface type found in the root bean type(s): "+types[i].getName());
					}
				}catch(ClassNotFoundException ex)
				{
					throw new ConfigurationException("Invalid class name encountered in root bean types: "+strTypes[i],ex);
				}
			
			/*
			 * Determine the map that needs to be used with the root bean. 
			 */
			boolean cacheMap=att.getReservedBoolean(CACHE_SUPPORT,false);
				if(cacheMap)//if caching is needed, then check if other attributes are specified
				{
					String cacheType=att.getReserved(CACHE_TYPE,null);
					int duration=att.getReservedInt(CACHE_INTERVAL,TimeBasedCacheMap.DEFAULT_TIME_GAP);
					String cachePath=att.getReserved(CACHE_PATH,null);
					String cacheFile=att.getReserved(CACHE_FILE,null);
					forceCache=att.getReservedBoolean(FORCE_CACHE,false);
					
						if(cacheType!=null)
						{
							Class cacheClass=null;
							Cache cache=null;
								try
								{
									cacheClass=Class.forName(cacheType);
									cache=(Cache)cacheClass.newInstance();
								}catch(Exception ex)
								{
									throw new ConfigurationException("Error in creating cache object. Invalid cache type specified: "+cacheType,ex);
								}
								
							beanMap=new TimeBasedCacheMap(cache,duration);
						}
						else
						{
								if(cachePath!=null && cacheFile!=null)
								{
									File pathFile=new File(cachePath);
										if(!pathFile.exists() || !pathFile.isDirectory())
											throw new ConfigurationException("Invalid cache directory path specified: "+cachePath);
										
										if(cacheFile==null || cacheFile.trim().length()==0)
											throw new ConfigurationException("Cache file name is not specified or specified as empty string.");
								}
								else
								{
									cachePath=null;
									cacheFile=null;
								}
								
								try
								{
										if(cachePath!=null)
											beanMap=new TimeBasedCacheMap(cachePath,cacheFile,duration);
										else
											beanMap=new TimeBasedCacheMap(duration);
								}catch(Exception ex)
								{
									throw new ConfigurationException("IO error occured while creating cache file.",ex);
								}
						}
						
					((TimeBasedCacheMap)beanMap).setCacheEnabled(false);
					((TimeBasedCacheMap)beanMap).setCacheMapListener(new BeanCacheListener());
				}
			//create the root bean based on specified types and map
			proxy=new DynamicBeanProxy(beanMap);
			rootBean=Proxy.newProxyInstance(this.getClass().getClassLoader(),types,proxy);
			
			//load configuration data need by handler to load current XML file
			loadHandlerConfigurationData(att);

			return rootBean;
		}
	
		/**
		 * This method is responsible for loading method mapping and configuring proxy methods. If "Bean Factory"
		 * is configured, then processReserveNode() will be called on it.
		 * <BR><BR>
		 * Note: processReserveNode() will not be called for standard reserve nodes supported by "CCG Manager Builder"
		 * <BR><BR>
		 * @see com.fw.ccg.xml.ParserHandler#processReservedNode(java.lang.String, java.lang.Object, com.fw.ccg.xml.XMLAttributeMap)
		 */
		public Object processReservedNode(BeanNode node,XMLAttributeMap att)
		{
			String nodeName=node.getName();
				if(RN_METHOD_MAP.equals(nodeName))
				{
					String method=att.get(ATT_METHOD_NAME,null);
						if(method==null)
							throw new ConfigurationException ("Mandatory attribute \""+ATT_METHOD_NAME+"\" is missing for node "+RN_METHOD_MAP);
					String prop=att.get(ATT_PROPERTY,null);
						if(prop==null)
							throw new ConfigurationException("Mandatory attribute \""+ATT_PROPERTY+"\" is missing for node "+RN_METHOD_MAP);
					String paramString=att.get(ATT_PARAM_STRING,null);
					proxy.addMethodMap(method,prop,paramString);
					return null;
				}
				
				if(RN_PROXY_METHOD.equals(nodeName))
				{
						if(proxy.isMethodProxyDefined())
							throw new ConfigurationException("In order to configure proxy methods \""+METHOD_PROXY+"\" should be defined.");
						
					String method=att.get(ATT_METHOD_NAME,null);
					
						if(method==null)
							throw new ConfigurationException("Mandatory attribute \""+ATT_METHOD_NAME+"\" is missing for node "+RN_METHOD_MAP);
						
					proxy.addMethodMap(method,null,null);
					return null;
				}
				
				if(beanFactory!=null)
					return beanFactory.processReserveNode(new ManagerNode(node),att);
				
			throw new ManagerException("Unsupported reserve node encountered: "+nodeName);
		}
	
		/**
		 * In this method following steps will be performed,
		 * 	<OL>
		 * 		<LI>If bean factory is specified, validateBean() is called on the factory
		 * 		passing this bean as an argument.
		 * 		<LI>If the bean is of type com.ccg.core.Validateable, then validate() will be called on the bean.
		 * 	</OL>
		 * @see com.fw.ccg.xml.ParserHandler#validateBean(java.lang.String, java.lang.Object, java.lang.Object)
		 * @throws ValidateException When validation on current bean fails.
		 */
		public void validateBean(BeanNode node) throws ValidateException
		{
			Object bean=node.getActualBean();
				if(bean instanceof Validateable)
					((Validateable)bean).validate();
				
				if(beanFactory!=null)
					beanFactory.validateBean(new ManagerNode(node));
		}
		
		/**
		 * This method will be called by "Manager Builder" once all the XML resource loading using this 
		 * handler is completed. If the underlying map is of type "TimeBasedCacheMap" and if
		 * forceCache flag is set to true then forceCache() will be called on it.  
		 */
		public void finalizeLoading()
		{
				if(beanMap instanceof TimeBasedCacheMap)
				{
					((TimeBasedCacheMap)beanMap).setCacheEnabled(true);
						if(forceCache)
							((TimeBasedCacheMap)beanMap).forceCache();
				}
			proxy.loadingCompleted();
		}
	
		/**
		 * If current bean is root bean then text "<Root Bean>" will be returned,
		 * otherwise it will be the bean's class name.
		 * <BR><BR>
		 * @see com.fw.ccg.xml.ParserHandler#getBeanDescription(java.lang.Object)
		 */
		public String getBeanDescription(Object bean)
		{
			return (rootBean==bean)?"<Root Bean>":bean.getClass().getName();
		}
	
		/**
		 * Returns the node name. If "id" reserve argument is specified in the corresponding 
		 * node, then text-  nodeName(<id value>) will be returned.<BR>
		 * 		eg., query(select)
		 * @see com.fw.ccg.xml.ParserHandler#getNodeDescription(java.lang.String, com.fw.ccg.xml.XMLAttributeMap)
		 */
		public String getNodeDescription(BeanNode node,XMLAttributeMap att)
		{
			String id=att.getReserved(BEAN_ID,null);
				if(id==null)
					return node.getName();
			return node.getName()+"("+id+")";
		}
	
		/**
		 * Creates instance of specified class using newInstance() of java.lang.Class.
		 * @param cls
		 * @return instance of cls.
		 */
		private Object createObject(String cls)
		{
				if(cls==null)
					return null;

				try
				{
					Class objCls=Class.forName(cls);
					return objCls.newInstance();
				}catch(Exception ex)
				{
					throw new ConfigurationException("Error in creating instance of type \""+cls+"\"",ex);
				}
		}

		/**
		 * If "Bean Factory" is present, processReserveNodeEnd() will be called on it.
		 * <BR><BR>
		 * Note: processReserveNodeEnd() will not be called for standard reserve nodes 
		 * supported by "CCG Manager Builder"
		 * <BR><BR>
		 * @see com.fw.ccg.xml.ParserHandler#processReserveNodeEnd(java.lang.String, java.lang.Object, java.lang.Object, com.fw.ccg.xml.XMLAttributeMap)
		 */
		public void processReserveNodeEnd(BeanNode node,XMLAttributeMap att)
		{
			String nodeName=node.getName();
				if(RN_METHOD_MAP.equals(nodeName) || RN_PROXY_METHOD.equals(nodeName))
					return;
				
				if(beanFactory!=null)
					beanFactory.processReserveNodeEnd(new ManagerNode(node),att);
		}
}
