package com.fw.dao.qry;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.TreeMap;

import com.fw.ccg.core.UnsupportedDataTypeException;
import com.fw.ccg.util.CCGUtility;

public class QueryUtil
{
		@SuppressWarnings({"rawtypes", "unchecked"})
		public static Object convert(Object obj,Class<?> toType)
		{
				if(toType==null)
					throw new NullPointerException("To type cannot be null");
				
				if(obj==null)
				{
						if(toType.isPrimitive())
							return CCGUtility.getDefaultPrimitiveValue(toType);
						
					return null;
				}
				
				if(toType.isAssignableFrom(obj.getClass()))
					return obj;
				
				if("null".equals(obj))
					return null;
				
				if(obj.getClass().isArray())
					return convertArray(obj,toType);
				
				if(Collection.class.isAssignableFrom(toType) && (obj instanceof Collection))
				{
					if(toType.isInterface())
					{
						if(toType.isAssignableFrom(ArrayList.class))
						{
							toType = ArrayList.class;
						}
						else if(toType.isAssignableFrom(HashSet.class))
						{
							toType = HashSet.class;
						}
						else if(toType.isAssignableFrom(TreeMap.class))
						{
							toType = TreeMap.class;
						}
						else
						{
							throw new IllegalStateException("Unknown abstact collection type encountered: " + toType.getName());
						}
					}
					
					try
					{
						Collection newCollection = (Collection)toType.newInstance();
						newCollection.addAll((Collection)obj);
						return newCollection;
					}catch(Exception ex)
					{
						throw new IllegalStateException("An exception occurred while building collection object", ex);
					}
				}
				
				try
				{
					return CCGUtility.toObject(obj.toString(),toType,null);
				}catch(Exception ex)
				{}
				
			throw new UnsupportedDataTypeException("Failed to convert\nFrom: "+obj+" ("+obj.getClass().getName()+")\nTo type: "+toType.getName());
		}

		public static Object convertArray(Object obj,Class<?> toType)
		{
				if(obj==null)
					return null;
				
				if(toType==null)
					throw new NullPointerException("To type cannot be null");
				
				if(!obj.getClass().isArray())
					throw new IllegalArgumentException("Specified object is not an array");
				
				if(!toType.isArray())
					throw new IllegalArgumentException("Specified type is not an array");

			int len=Array.getLength(obj);
			Class<?> compType=toType.getComponentType();
			Object res=Array.newInstance(compType,len);
			
			
				for(int i=0;i<len;i++)
					Array.set(res,i,convert(Array.get(obj,i),compType));
				
			return res;
		}
		
		public static Object[] convert(Object obj[],Class<?> toType[])
		{
				if(obj==null)
					return null;
				
				if(toType==null)
					throw new NullPointerException("To type cannot be null");
				
				if(obj.length!=toType.length)
					throw new IllegalArgumentException("Object count and type count are mismatching.");

			Object res[]=new Object[obj.length];
			
				for(int i=0;i<res.length;i++)
					res[i]=convert(obj[i],toType[i]);
				
			return res;
		}
}
