package com.fw.dao.qry;

import java.sql.Types;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SQLTypeMapping
{
	private static Map<Integer,Class<?>> typeToCls=new HashMap<Integer,Class<?>>();
	private static Map<Class<?>,Integer> clsToType=new HashMap<Class<?>,Integer>();
	
		static
		{
			registerMapping(Types.INTEGER,Byte.class);
			registerMapping(Types.INTEGER,byte.class);
			
			registerMapping(Types.INTEGER,Integer.class);
			registerMapping(Types.INTEGER,int.class);
			
			registerMapping(Types.INTEGER,Long.class);
			registerMapping(Types.INTEGER,long.class);
			
			registerMapping(Types.DECIMAL,Float.class);
			registerMapping(Types.DECIMAL,float.class);
			
			registerMapping(Types.DECIMAL,Double.class);
			registerMapping(Types.DECIMAL,double.class);
			
			registerMapping(Types.BOOLEAN,Boolean.class);
			registerMapping(Types.BOOLEAN,boolean.class);
			
			registerMapping(Types.VARCHAR,String.class);
			
			registerMapping(Types.VARCHAR,Character.class);
			registerMapping(Types.VARCHAR,char.class);

			registerMapping(Types.DATE,Date.class);
			registerMapping(Types.DATE,java.sql.Date.class);
			
			registerMapping(Types.BLOB,byte[].class);
		}
	
		public static void registerMapping(int sqlType,Class<?> cls)
		{
				if(cls==null)
					throw null;
				
			typeToCls.put(sqlType,cls);
			clsToType.put(cls,sqlType);
		}
		
		public static Integer getSqlMapping(Class<?> cls)
		{
			return clsToType.get(cls);
		}
		
		public static Class<?> getJavaMapping(int type)
		{
			return typeToCls.get(type);
		}
}
