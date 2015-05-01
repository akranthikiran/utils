package com.fw.ccg.xml;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

public class BeanProperty
{
	private Method setter;
	private Method getter;
	private Class<?> setterType;
	private Class<?> getterType;
	private boolean idBased;
	
		public BeanProperty(Method setter,Method getter)
		{
			this.setter=setter;
			this.getter=getter;
			
			getterType=getter.getReturnType();
			
			Class<?> args[]=setter.getParameterTypes();
			idBased=args.length>1;
			setterType=(idBased)?args[1]:args[0];
		}
		
		public String getName()
		{
			String name=setter.getName().substring(3);
			String ch=name.substring(0,1).toLowerCase();
			
			return ch+name.substring(1);
		}
		
		private static Map<String,LinkedList<Method>> loadMethods(Class<?> cls)
		{
			Method methods[]=cls.getMethods();
			String name=null;
			Class<?> args[]=null;
			Class<?> retType=null;
			int mod=0;
			boolean isSetMethod=false;
			HashMap<String,LinkedList<Method>> nameToMet=new HashMap<String,LinkedList<Method>>();
			
				for(int i=0;i<methods.length;i++)
				{
					mod=methods[i].getModifiers();
					
						if(!Modifier.isPublic(mod) || Modifier.isStatic(mod))
							continue;
							
					name=methods[i].getName();
					
						if(name.startsWith("set") || name.startsWith("add"))
						{
							isSetMethod=true;
								if(name.length()<=3)
									continue;
							name="@"+name.substring(3);
						}
						else if(name.startsWith("get"))
						{
							isSetMethod=false;
								if(name.length()<=3)
									continue;
							name="_"+name.substring(3);
						}
						else if(name.startsWith("is"))
						{
							isSetMethod=false;
							
								if(name.length()<=2)
									continue;
							name="_"+name.substring(2);
						}
						else
							continue;
							
					args=methods[i].getParameterTypes();
					retType=methods[i].getReturnType();
					args=(args==null || args.length==0)?null:args;
					
						if(isSetMethod && (args==null || args.length>2))
							continue;
						
						if(!isSetMethod && (void.class.equals(retType) || args!=null))
							continue;
						
						if(nameToMet.containsKey(name.toUpperCase()))
						{
							LinkedList<Method> lst=nameToMet.get(name.toUpperCase());
							lst.add(methods[i]);
						}
						else
						{
							LinkedList<Method> lst=new LinkedList<Method>();
							lst.add(methods[i]);
							nameToMet.put(name.toUpperCase(),lst);
						}
				}
				
			return nameToMet;
		}
		
		private static BeanProperty buildProperty(String setterName,Method setter,
											Map<String,LinkedList<Method>> nameToMet)
		{
			Class<?> paramTypes[]=setter.getParameterTypes();
			boolean isIDBased=(paramTypes.length>1);
			Method getMethod=null;
			String getterName="_"+setterName.substring(1);
			
				if(isIDBased)
				{
						if(!nameToMet.containsKey(getterName+"MAP"))
							return null;
						
					getterName=getterName+"MAP";
					getMethod=nameToMet.get(getterName).get(0);
				}
				else if(nameToMet.containsKey(getterName+"S"))
				{
					getterName=getterName+"S";
					getMethod=nameToMet.get(getterName).get(0);
				}
				else if(nameToMet.containsKey(getterName.substring(0,getterName.length()-1)+"IES"))
				{
					getterName=getterName.substring(0,getterName.length()-1)+"IES";
					getMethod=nameToMet.get(getterName).get(0);
				}
				else if(nameToMet.containsKey(getterName+"LIST"))
				{
					getterName=getterName+"LIST";
					getMethod=nameToMet.get(getterName).get(0);
				}
				else if(nameToMet.containsKey(getterName))
				{
					getMethod=nameToMet.get(getterName).get(0);
				}
				else
					return null;
				
			
			Class<?> getType=getMethod.getReturnType();
			boolean dynamic=(Collection.class.isAssignableFrom(getType) ||
								Map.class.isAssignableFrom(getType));
			
				if(!dynamic)
				{
						if(getType.isArray())
							getType=getType.getComponentType();
						
					Class<?> setType=(isIDBased)?paramTypes[1]:paramTypes[0];
					
						if(!setType.isAssignableFrom(getType))
							return null;
				}
			
			nameToMet.remove(setterName);
			nameToMet.remove(getterName);
			return new BeanProperty(setter,getMethod);			
		}
		
		public static Map<String,BeanProperty> loadProperties(Class<?> cls)
		{
			Map<String,LinkedList<Method>> nameToMet=loadMethods(cls);
			BeanProperty prop=null;
			LinkedList<Method> methodLst=null;
			Iterator<String> it=nameToMet.keySet().iterator();
			
			HashMap<String,BeanProperty> nameToProp=new HashMap<String,BeanProperty> ();
			String name=null;
				while(it.hasNext())
				{
					name=it.next();
					
						if(!name.startsWith("@"))
							continue;
					
					methodLst=nameToMet.get(name);
						for(Method method:methodLst)
						{
							prop=buildProperty(name,method,nameToMet);
							
								if(prop==null)
									continue;
									
							nameToProp.put(prop.getName().toUpperCase(),prop);
							break;
						}
			
					nameToMet.remove(name);
					it=nameToMet.keySet().iterator();
				}
				
			
			return nameToProp;
		}
	
		public Method getGetter()
		{
			return getter;
		}
		
		public Method getSetter()
		{
			return setter;
		}
		
		public void invokeSetter(Object bean,Object id,Object value)
		{
			try
			{
					if(setter.getParameterTypes().length==1)
						setter.invoke(bean,new Object[]{value});
					else
						setter.invoke(bean,new Object[]{id,value});
			}catch(Exception ex)
			{
				throw new BeanPropertyException("Error in invoking Setter Method.",ex);
			}
		}
		
		public Object invokeGetter(Object bean)
		{
			try
			{
				return getter.invoke(bean,(Object[])null);
			}catch(Exception ex)
			{
				throw new BeanPropertyException("Error in invoking Setter Method.",ex);
			}
		}

		public Class<?> getGetterType()
		{
			return getterType;
		}

		public boolean isIdBased()
		{
			return idBased;
		}

		public Class<?> getSetterType()
		{
			return setterType;
		}
		
		public boolean isCCGXMLSupported()
		{
				if(setterType.isArray())
					return false;
				
				if(Collection.class.isAssignableFrom(setterType))
					return false;
				
				if(Map.class.isAssignableFrom(setterType))
					return false;
			
			return true;
		}
}
