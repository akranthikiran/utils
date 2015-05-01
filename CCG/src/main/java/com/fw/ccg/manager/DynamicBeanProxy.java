package com.fw.ccg.manager;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.fw.ccg.util.CCGUtility;
import com.fw.ccg.util.MethodSequence;
import com.fw.ccg.util.StringUtil;

/**
 * <BR><BR>
 * <P>
 * The "Dynamic Manager" built by "CCG Manager Builder" will be backed by the instance of 
 * this class. This class is responsible for managing the property data needed by the 
 * dynamic manager. This also responsible for redirecting "Method Mapping" calls and 
 * "Proxy Method" calls to the appropriate "method map" or "method proxy" respectively.   
 * </P> 
 * <BR>
 * @author A. Kranthi Kiran
 */
class DynamicBeanProxy implements InvocationHandler
{
		/**
		 * This acts like a buffer class to maitain ID of the beans and thier default flag
		 * status till they get added to the main property map.<BR>
		 * Note, the ID, default flag are specified at the start of XML node and the bean
		 * is added at the end of the node.
		 * <BR><BR>
		 * @author A. Kranthi Kiran
		 */
		private static class BeanAttributes
		{
			String ID;
			boolean def;
				public BeanAttributes(String id,boolean def)
				{
					this.ID=id;
					this.def=def;
				}
		}
		
		/**
		 * This class holds information needed to invoke method mappings. And this class 
		 * constructor is responsible for parsing property map and param string.
		 * @author A. Kranthi Kiran
		 */
		private static class MethodConfig
		{
			/**
			 * Property map string. This will be used in the exception messages to make the
			 * debugging easier.
			 */
			private String propName;
			
			/**
			 * Index of the arguments to be passed at different levels when nested property 
			 * is invoked. That is, parsed data of the param string. methodArgs[0] indicates
			 * parameter indexed at the first level. 
			 */
			private byte methodArgs[][];
			
			/**
			 * Method sequence to be invoked. 
			 */
			private MethodSequence sequence=null;
			
				/**
				 * This constructor is responsible for parsing method mapping string and param
				 * string and build method sequence to be invoked. 
				 * @param manager Manager instance where root method sequence is expected to start.
				 * @param method Method for which mapping is being done.
				 * @param propName Property string.
				 * @param paramString Param string.
				 */
				public MethodConfig(Object manager,Method method,String propName,String paramString)
				{
					this.propName=propName;
					Class methodArgTypes[]=method.getParameterTypes();
					String paramLst[]=null;
					
					//Parse the param string
						/*
						 * If parameter string is null, then assign parameters of 
						 * mapped method as arguments one at each level.
						 */
						if(paramString==null)
						{
							paramLst=new String[methodArgTypes.length];
								for(int i=0;i<methodArgTypes.length;i++)
									paramLst[i]=Integer.toString(i);
						}
						else
						{
							paramLst=StringUtil.tokenize(paramString,"|",false,true);
						}
						
					methodArgs=new byte[paramLst.length][];
					
					Class argTypes[][]=new Class[paramLst.length][];
					String params[]=null;
					int j=0;
						for(int i=0;i<paramLst.length;i++)
						{
								if(paramLst[i].trim().length()<=0)
								{
									//indicates none of the parameters should
									//be passed at this method level
									methodArgs[i]=null;
									argTypes[i]=null;
									continue;
								}
								
							params=StringUtil.tokenize(paramLst[i],",",false,false);
							methodArgs[i]=new byte[params.length];
							argTypes[i]=new Class[params.length];
							
								for(j=0;j<params.length;j++)
								{
										try
										{
											methodArgs[i][j]=Byte.parseByte(params[j]);
												if(methodArgTypes.length<=methodArgs[i][j])
													throw new ConfigurationException("For method \""+CCGUtility.toString(method)+"\" specified parameter index is out of bounds: "+methodArgs[i][j]);
											//Note: Argument index <0 indicates null value to be passed	
												if(methodArgs[i][j]>=0)
													argTypes[i][j]=methodArgTypes[methodArgs[i][j]];
										}catch(NumberFormatException ex)
										{
											throw new IllegalArgumentException("Invalid number passed in param string \""+paramString+"\" at level \""+i+"\": "+params[j]);
										}
								}
						}
					
					//construct the method sequence out of property string.
						
						try
						{
							sequence=CCGUtility.buildMethodSequence(manager.getClass(),propName,argTypes);
						}catch(Exception ex)
						{
							throw new ConfigurationException("Error in building method sequence from property string: "+propName,ex);
						}

					//Validate the return type of sequnce aganist mapped method return type
						if(!CCGUtility.isAssignable(sequence.getReturnType(),method.getReturnType()))
							throw new ConfigurationException("Return type of method \""+CCGUtility.toString(method)+"\" is not assignable from return type of property string: "+propName);
				}
				
				/**
				 * Invokes the undelying method sequence.
				 * @param method Mapped method.
				 * @param manager Dynamic manager on which mapping property needs to be invoked.
				 * @param args Arguments passed while invoking mapped method.
				 * @return The return value of property map.
				 */
				public Object invoke(Method method,Object manager,Object args[])
				{
					Object res=null;
					Object fnlArgs[][]=null;
					int levelCount=sequence.getLevelCount();
					
					//build 2D argument list to be used while invoking method sequence
					fnlArgs=new Object[levelCount][];
					int j=0;
						for(int i=0;i<levelCount;i++)
						{
								if(i>=methodArgs.length || methodArgs[i]==null || methodArgs[i].length==0)
								{
									fnlArgs[i]=null;
									continue;
								}
							
							fnlArgs[i]=new Object[methodArgs[i].length];
							
								for(j=0;j<fnlArgs[i].length;j++)
								{
									//if argument index is < 0 pass null for that argument
										if(methodArgs[i][j]<0)
										{
											fnlArgs[i][j]=null;
											continue;
										}
										
									fnlArgs[i][j]=args[methodArgs[i][j]];
								}
						}
					
					//invoke the method sequence
						try
						{
							res=sequence.invoke(manager,fnlArgs);
						}catch(InvocationTargetException ex)
						{
								if(ex.getCause()!=null)
									throw new DynamicMethodException("Error in invoking property: "+propName,ex.getCause(),method);
							throw new DynamicMethodException("Error in invoking property: "+propName,ex,method);
						}catch(Exception ex)
						{
							throw new DynamicMethodException("Error in invoking property: "+propName,ex,method);
						}
						
					Class retType=method.getReturnType();
					//if return type is primitive and property map resulted in null
					//	pass default value.
						if(retType.isPrimitive() && res==null)
							res=CCGUtility.getDefaultPrimitiveValue(retType);
						else if(void.class.equals(retType) && res!=null)
							res=null;
						
					return res;
				}
		}
	
	/**
	 * Simple constant used for internal purpose, to represent the unsupported
	 * operation.
	 */
	private static final Object UNSUPPORTED_VALUE=new Object();
	
	/**
	 * Simple bean that holds the dynamic manager actual data with thier 
	 * unique keys.
	 */
	private DynamicBean bean;
	
	/**
	 * A simple map to maitain bean attributes during the node start and node end.
	 * This map hold the bean instance as key to the instance of BeanAttributes.
	 */
	private Map<Object,BeanAttributes> beanToID=new HashMap<Object,BeanAttributes>();
	
	/**
	 * Map used to hold method names to the method mappings (instances of MethodConfig). 
	 * This holds even the proxy method names and thier method proxies as values.<BR>
	 * Note: Method proxies may differ from other beans if they are loaded from 
	 * different XML resources.
	 */
	private Map<String,Object> methodToProp=new HashMap<String,Object>();
	
	/**
	 * A flag to indicate the loading of input XML data is completed.
	 * Once this flag is set to true, setters and adders invokation on dynamic manager
	 * will throw excpetions.
	 */
	private boolean loadingCompleted=false;
	
	/**
	 * Method proxy for the current XML resource being loaded.
	 */
	private ManagerMethodProxy methodProxy=null;
	
	/**
	 * Dynamic manager instance.
	 */
	private Object manager=null;
	
	/**
	 * A simple instance which works as substitute for the dynamic manager when standard
	 * methods like hashCode(), equals() are called on dynamic manager.
	 */
	private Object managerSubstitute=new Object();
	
		/**
		 * Constructs a proxy for CCG dynamic manager and uses specified map for storing property values.
		 * If map is null, a new HashMap instance will get created.
		 * @param map Map instance to be used for storing property values.
		 */
		public DynamicBeanProxy(Map<String,Object> map)
		{
				if(map==null)
					map=new HashMap<String,Object>();
			bean=new DynamicBean(map);
		}
		
		/**
		 * Inidicates loading input XML data is completed. After this method call 
		 * of adders/setters dynamic managers will throw DynamicMethodException.
		 */
		public void loadingCompleted()
		{
			loadingCompleted=true;
			beanToID.clear();
		}
		
		/**
		 * Puts specified bean on the map "beanToID" using specified ID.  This bean to 
		 * id mapping is needed to determine ID for a paticular bean before adding 
		 * to actual property map.<BR>
		 * Note: Bean id will not be avilable while adding bean to actual map.
		 * @param bean  Bean to be added.
		 * @param ID ID for the bean.
		 * @param def True, if specified bean is default value for current property.
		 */
		public void put(Object bean,String ID,boolean def)
		{
			beanToID.put(bean,new BeanAttributes(ID,def));
		}
		
		/**
		 * Sets the method proxy and manager to be used for current input XML resource.
		 * @param methodProxy 
		 * @param manager
		 */
		public void setMethodProxy(ManagerMethodProxy methodProxy,Object manager)
		{
			this.methodProxy=methodProxy;
			this.manager=manager;
		}
		
		/**
		 * @return Whether method proxy is configured for current XML.
		 */
		public boolean isMethodProxyDefined()
		{
			return (methodProxy==null);
		}
		
		/**
		 * Method for adding property mapping.
		 * 
		 * @param methodName Method name being mapped.
		 * @param property Property string.
		 * @param paramString Parameter string.
		 */
		public void addMethodMap(String methodName,String property,String paramString)
		{
			
				if(methodToProp==null)
					methodToProp=new HashMap<String,Object>();
			
				if(property==null)
				{
					Method met[]=CCGUtility.getMethodsIn(manager.getClass(),
							new String[]{methodName},-1,null,false);
				
						if(met==null)
							throw new ConfigurationException("No method exist in manager with name \""+methodName+"\".");
						
					methodToProp.put(methodName,methodProxy);
				}
				else
				{
					Method met[]=CCGUtility.getMethodsIn(manager.getClass(),
								new String[]{methodName},-1,null,false);
					
						if(met==null)
							throw new ConfigurationException("No method exist in manager with name \""+methodName+"\".");
						
						if(met.length>1)
							throw new ConfigurationException("More than one method exist in manager with name \""+methodName+"\".");
					
					MethodConfig conf=new MethodConfig(manager,met[0],property,paramString);						
					methodToProp.put(methodName,conf);
				}
		}
		
		/**
		 * Other than the CCG standard methods, when other java common methods (methods 
		 * of java.lang.Object class) are invoked on dynamic manager this method will be
		 * invoked. Note, some methods like wait(), notify() will not invoke this method.
		 * @param dynBean Dynamic bean on which standard method is invoked.
		 * @param method Method which is invoked.
		 * @param args Arguments passed to this method.
		 * @return Appropriate return value for the current standard method. If not a java
		 * standard method this will return UNSUPPORTED_VALUE. So that standard process of
		 * method invoking can be continued.
		 * @throws Exception
		 */
		public Object checkForStandardMethod(Object dynBean,Method method,Object args[]) throws Exception
		{
			String name=method.getName();
				if("toString".equals(name) && (args==null || args.length==0))
					return "<<Dynamic Manager>>";
				
				if("equals".equals(name) && 
						args!=null && args.length==1 && 
								boolean.class.equals(method.getReturnType()))
					return new Boolean(dynBean==args[0]);
				
				if("hashCode".equals(name) && (args==null || args.length==0))
					return new Integer(managerSubstitute.hashCode());
				
			return UNSUPPORTED_VALUE;
		}
		
		
		/**
		 * Invoked whenever there is a method invokation happens on the corresponding 
		 * dynamic manager.
		 * 
		 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
		 */
		public Object invoke(Object dynBean,Method method,Object args[]) throws Throwable
		{
			String methodName=method.getName();
			
			//Check if the method invoked is a java styandard method.
			Object res=checkForStandardMethod(dynBean,method,args);
				
				//if it is standard method then just return obtained result
				if(res!=UNSUPPORTED_VALUE)
					return res;
				
				//If current method is configure for method mapping or asproxy method
				//then just invoke the approp mapping or method proxy class and return
				//obtained result
				if(methodToProp.containsKey(methodName))
				{
					Object mapping=methodToProp.get(methodName);
						if(mapping instanceof ManagerMethodProxy)
						{
							ManagerMethodProxy methodProxy=(ManagerMethodProxy)mapping;
							res=methodProxy.execute(manager,method,args);
								if(res!=ManagerMethodProxy.CONTINUE_DEFAULT)
								{
									Class<?> retType=method.getReturnType();
									
										if(res==null && retType.isPrimitive())
											return CCGUtility.getDefaultPrimitiveValue(retType);
										
										if(res!=null && !retType.isAssignableFrom(res.getClass()))
											throw new DynamicMethodException("Incompatible value type \""+res.getClass().getName()+"\" returned by method proxy: "+methodProxy.getClass().getName(),method);
									return res;
								}
						}
						else
						{
							MethodConfig metInvoke=(MethodConfig)methodToProp.get(methodName);
							return metInvoke.invoke(method,dynBean,args);
						}
				}
				
				if(methodName.startsWith("set"))//setter
				{
						if(loadingCompleted)
							throw new DynamicMethodException("Setter method calls are not supported on dynamic manager.",method);
					
						if(methodName.length()==3 || args==null || args.length!=1)//check for setter structure
							throw new DynamicMethodException(method);
						
					methodName=methodName.substring(3);
					bean.set(methodName,args[0]);
				}
				else if(methodName.startsWith("add"))//adder
				{
						if(loadingCompleted)
							throw new DynamicMethodException("Adder method calls are not supported on dynamic manager.",method);
						//check for adder structure
						if(methodName.length()==3 || args==null ||args.length!=1) 
							throw new DynamicMethodException(method);
						
					methodName=methodName.substring(3);
					BeanAttributes att=beanToID.remove(args[0]);
					
						if(att.ID==null)
							throw new ConfigurationException("ID expected for node \""+methodName+"\" (Adder Property).");
						
					bean.addIDBased(att.def,methodName,att.ID,args[0]);
				}
				else if(methodName.startsWith("get") || methodName.startsWith("is"))
				{
						if(methodName.startsWith("get"))
						{
								if(methodName.length()==3)
									throw new DynamicMethodException(method);
							methodName=methodName.substring(3);
						}
						else//"is" type of getters
						{
								if(methodName.length()==2)
									throw new DynamicMethodException(method);
							methodName=methodName.substring(2);
						}
						
						if(args!=null && args.length>1)
							throw new DynamicMethodException(method);
					
						if(Void.TYPE.equals(method.getReturnType()))
							throw new DynamicMethodException("Methods of type void are not supported by dynamic manager.",method);
						
						if(args==null || args.length==0)//getter corresponding to setter
						{
							//search normal properties
							res=bean.get(methodName);
						}
						//getter corresponding to adder
						else if(args[0]==null || String.class.equals(args[0].getClass()))
						{
							res=bean.get(methodName,(String)args[0]);
						}
						else
							throw new DynamicMethodException(method);
				}
				else
					throw new DynamicMethodException(method);
				
				if(res==null)
				{
					Class retType=method.getReturnType();
						if(retType.isPrimitive())
							res=CCGUtility.getDefaultPrimitiveValue(retType);
				}
			return res;
		}

}

/**
 * <BR>
 * @author A. Kranthi Kiran
 * <BR><BR>
 * A support class for CCGDynamicBeanProxy. This class helps in maintaining propery values of the
 * actual dynamic bean.
 */
class DynamicBean
{
	/**
	 * Map which holds tha actual property data of dynamic manager.
	 */
	private Map<String,Object> methodKeyToVal;
		public DynamicBean(Map<String,Object> map)
		{
			this.methodKeyToVal=map;
		}
		
		/**
		 * Called when setter is invoked on dynamic bean.
		 * @param method Invoked setter property name  
		 * @param val Value that is passed to the setter.
		 */
		public void set(String method,Object val)
		{
			methodKeyToVal.put(method,val);
		}
		
		/**
		 * Called when getter is invoked on dynamic bean.
		 * @param method Invoked getter property name
		 * @return Correponding property value.
		 */
		public Object get(String method)
		{
			return methodKeyToVal.get(method);
		}
		
		/**
		 * Invoked when adder is invoked on dynamic bean.
		 * @param def Indicate whether current value represents default value for this proeprty.
		 * @param method Property name of the invoked ID-based adder. 
		 * @param id ID passed to adder.
		 * @param val Value passed to the adder.
		 */
		public void addIDBased(boolean def,String method,String id,Object val)
		{
				if(id==null)
					throw null;
				
				if(methodKeyToVal.containsKey("#"+method+"#"+id))
					throw new ConfigurationException("Duplicate ID encountered for node \""+method+"\": "+id);
			/*
			 * # symbol is added at the start of ID in order to differentiate
			 * property names of setter and adder.
			*/ 
			methodKeyToVal.put("#"+method+"#"+id,val);
				if(def)
					methodKeyToVal.put("#"+method+"#",val);
		}
		
		/**
		 * Invoked when getter correpsonding to adder is invoked.
		 * @param method Property name of the invoked getter.
		 * @param id ID used in getter method.
		 * @return Returns mapping property of the specified ID.
		 */
		public Object get(String method,String id)
		{
				if(id==null)
					return methodKeyToVal.get("#"+method+"#");
			return methodKeyToVal.get("#"+method+"#"+id);
		}
}
