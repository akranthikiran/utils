package com.fw.ccg.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

public class BeanUtil
{
	public static Map<String,Method> getSetterMethodMap(Class<?> cls,boolean adder)
	{
		Method met[]=cls.getMethods();
		Map<String,Method> res=new TreeMap<String,Method>(new StringComparator(true));
		int mod=0;
		String name=null;
		Class<?> args[]=null;
		
			for(Method m:met)
			{
				mod=m.getModifiers();
				
					if(!Modifier.isPublic(mod))
						continue;
					
					if(Modifier.isStatic(mod))
						continue;
					
				args=m.getParameterTypes();
				
					if(args==null || args.length!=1)
						continue;
					
				name=m.getName();
					if(!name.startsWith("set"))
					{
							if(!adder || !name.startsWith("add"))
								continue;
					}
					
					if(name.length()<=3)
						continue;
				
				name=StringUtil.toStartLower(name.substring(3));
				res.put(name,m);
			}
			
		return res;
	}
	
	/**
	 * Checks equality of two beans based on the nested getter values (instead of equals).<br/>
	 * Note: Beans whose class name starts with "java." will be compared as per equals() method.
	 * 
	 * @param bean1
	 * @param bean2
	 * @return
	 */
	public static boolean isEqual(Object bean1,Object bean2)
	{
			if(bean1==bean2)
				return true;
			
			if(bean1==null || bean2==null)
				return false;
			
			if(!bean1.getClass().equals(bean2.getClass()))
				return false;
			
		Class<?> cls=bean1.getClass();
		
			if(cls.isArray())
			{
					if(cls.getComponentType().isPrimitive())
					{
						Class<?> compType=cls.getComponentType();
						
							if(byte.class.equals(compType))
								return Arrays.equals((byte[])bean1,(byte[])bean2);
							
							if(boolean.class.equals(compType))
								return Arrays.equals((boolean[])bean1,(boolean[])bean2);
							
							if(char.class.equals(compType))
								return Arrays.equals((char[])bean1,(char[])bean2);
							
							if(short.class.equals(compType))
								return Arrays.equals((short[])bean1,(short[])bean2);
							
							if(int.class.equals(compType))
								return Arrays.equals((int[])bean1,(int[])bean2);
							
							if(long.class.equals(compType))
								return Arrays.equals((long[])bean1,(long[])bean2);
							
							if(double.class.equals(compType))
								return Arrays.equals((double[])bean1,(double[])bean2);
							
							if(float.class.equals(compType))
								return Arrays.equals((float[])bean1,(float[])bean2);
						
						throw new IllegalStateException("Unknown primitive type encountered: "+cls.getName());
					}
				
				Object objArr1[]=(Object[])bean1;
				Object objArr2[]=(Object[])bean2;
			
					if(objArr1.length!=objArr2.length)
						return false;
			
					for(int i=0;i<objArr1.length;i++)
						if(!isEqual(objArr1[i],objArr2[i]))
							return false;
					
				return true;
			}
			
		String clsName=cls.getName();
		
			if(clsName.startsWith("java."))
				return bean1.equals(bean2);
			
		Map<String,Method> getterMap=getGetterMethods(bean1.getClass());
		
			for(Method met:getterMap.values())
			{
				try
				{
					if(!isEqual(met.invoke(bean1),met.invoke(bean2)))
						return false;
				}catch(Exception ex)
				{
					throw new IllegalStateException("An error occured while invoking getter: "+met.getName(),ex);
				}
			}
			
		return true;
	}
	
	public static Map<String,Method> getGetterMethods(Class<?> cls)
	{
		Method met[]=cls.getMethods();
		Map<String,Method> res=new TreeMap<String,Method>(new StringComparator(true));
		int mod=0;
		String name=null;
		Class<?> retType=null;
		
			for(Method m:met)
			{
				mod=m.getModifiers();
				
					if(!Modifier.isPublic(mod))
						continue;
					
					if(Modifier.isStatic(mod))
						continue;
					
				retType=m.getReturnType();
				
					if(retType==null || void.class.equals(retType))
						continue;
					
				name=m.getName();
					if(!name.startsWith("get"))
					{
							if(!name.startsWith("is"))
								continue;
							
							if(name.length()<=2)
								continue;
							
						name=name.substring(2);
					}
					else
					{
							if(name.length()<=3)
								continue;
							
						name=name.substring(3);
					}
					
				name=StringUtil.toStartLower(name);
				res.put(name,m);
			}
			
		return res;
	}
	
	
	public static boolean populateBeanProperties(Object bean,Class<?> cls,Map<String,? extends Object> prop,
				boolean parse,boolean exactMatch)
	{
		cls=(cls==null)?bean.getClass():cls;
		Map<String,Method> nameToMethod=getSetterMethodMap(cls,true);
		Object value=null;
		Method met=null;
		Class<?> arg[]=null;
		boolean res=true;
		
			for(String propName:prop.keySet())
			{
				value=prop.get(propName);
				met=nameToMethod.get(propName);
				arg=met.getParameterTypes();
				
					if(value==null)
						continue;
				
					if(!value.getClass().isAssignableFrom(arg[0]))
					{
							if(parse && (value instanceof String))
							{
								value=CCGUtility.toObject((String)value,arg[0],null);
							}
							else
								throw new IllegalArgumentException("Property type \""+arg[0].getName()+"\" is not matching with value: "+value);
					}
					
					try
					{
						met.invoke(bean,value);
					}catch(Exception ex)
					{
						throw new IllegalStateException("Error in invoking property: "+propName);
					}
			}
		return res;
	}
	
	public static byte[] encodeToBytes(Serializable object)
	{
		try
		{
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			
			oos.writeObject(object);
			oos.flush();
			
			return bos.toByteArray();
		}catch(Exception ex)
		{
			throw new IllegalStateException("An error occurred while encoding object to bytes", ex);
		}
	}
	
	public static Object decodeFromBytes(byte data[])
	{
		try
		{
			ByteArrayInputStream bis = new ByteArrayInputStream(data);
			ObjectInputStream ois = new ObjectInputStream(bis);
			
			return ois.readObject();
		}catch(Exception ex)
		{
			throw new IllegalStateException("An error occurred while decoding object from bytes", ex);
		}
	}
}
