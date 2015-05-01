package com.fw.ccg.xml;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.xml.sax.Attributes;

import com.fw.ccg.util.StringComparator;

/**
 * © Copyright 2006 IBM Corporation
 * <BR><BR>
 * Read only map.
 * A read-only Map used to represent attributes of XML nodes. Additional getters are 
 * provided to convert the attributed data into primitives.
 * <BR>
 * This map contains different methods to access reserve attributes and normal attributes
 * separately. If reservce attributes can not be accessed with methods meant for normal
 * attributes and vice versa.
 * <BR>
 * @author A. Kranthi Kiran
 */
public class XMLAttributeMap extends TreeMap<String,XMLAttributeMap.Attribute>
{
	private static final long serialVersionUID=1L;
			static class Attribute
			{
				String value;
				boolean isReserved;
					public Attribute(String value,boolean isRes)
					{
						this.value=value;
						isReserved=isRes;
					}
			}
			
	private boolean hasReserveAttributes=false;
	private int normalAttCount=0;
	
		private XMLAttributeMap()
		{
			super(new StringComparator(true));
		}
		
		/**
		 * Builds map using attributes present in att.
		 * @param att SAX attributes object.
		 */
		public XMLAttributeMap(final Attributes att)
		{
			this();
			
			int len=att.getLength();
			String name=null;
			String value=null;
			boolean isReserved=false;
				for(int i=0;i<len;i++)
				{
					name=att.getLocalName(i);
					value=att.getValue(i);
					isReserved=XMLConstants.CCG_URI.equals(att.getURI(i));
					super.put(name,new Attribute(value,isReserved));
						if(!hasReserveAttributes && isReserved)
							hasReserveAttributes=true;
						
						if(!isReserved)
							normalAttCount++;
				}
		}
		
		public XMLAttributeMap getReservedMap()
		{
			XMLAttributeMap resMap=new XMLAttributeMap();
			Attribute attr=null;
			
				for(String name:super.keySet())
				{
					attr=super.get(name);
					
						if(!attr.isReserved)
							continue;
						
					resMap.putAttr(name,attr);
				}
				
			resMap.hasReserveAttributes=!resMap.isEmpty();
			return resMap;
		}
		
		private void putAttr(String name,Attribute attr)
		{
			super.put(name,attr);
		}
		
		public void put(String name, String value, boolean reserved)
		{
			super.put(name, new Attribute(value, reserved));
		}
		
		
		/**
		 * This method is simply ignored.
		 */
		public void remove() 
		{}
		
		/**
		 * This method is simply ignored.
		 * @see java.util.Map#clear()
		 */
		public void clear()
		{}

		/**
		 * This method is simply ignored.
		 * @param key
		 * @param value
		 * @return null.
		 */
		public Attribute put(String key,Attribute value)
		{
			return null;
		}

		/**
		 * This method is simply ignored.
		 * @param arg0
		 */
		public void putAll(Map<? extends String,? extends Attribute> arg0)
		{}

		/**
		 * This method is simply ignored.
		 * @see java.util.Map#remove(java.lang.Object)
		 */
		public Attribute remove(Object arg0)
		{
			return null;
		}

		/**
		 * Returns attribute value whose name matches with key.
		 * @param key Attribute name
		 * @param def Default value, if attribute is not present or mismatching with expected type .
		 * @return Value represented by attribute, if present. If not present returns def.
		 */
		public String get(String key,String def)
		{
			String res=(String)get(key);
				if(res==null)
					return def;
			return res;
		}
		
		/**
		 * Returns attribute value whose name matches with key. If specified attribute is not present 
		 *  or mismatching with expected type exception is thrown.
		 * @param key Attribute name
		 * @return Value represented by attribute, if present. 
		 */
		public byte getByte(String key)
		{
			String val=(String)get(key);
			return new Byte(val).byteValue();
		}
		
		/**
		 * Returns attribute value whose name matches with key.
		 * @param key Attribute name
		 * @param def Default value, if attribute is not present or mismatching with expected type .
		 * @return Value represented by attribute, if present. If not present returns def.
		 */
		public byte getByte(String key,byte def)
		{
			String val=(String)get(key);
				try
				{
					return new Byte(val).byteValue();
				}catch(Exception ex)
				{
					return def;
				}
		}
		
		/**
		 * Returns attribute value whose name matches with key. If specified attribute is not present 
		 *  or mismatching with expected type exception is thrown.
		 * @param key Attribute name
		 * @return Value represented by attribute, if present. 
		 */
		public boolean getBoolean(String key)
		{
			String val=(String)get(key);
			return val.equalsIgnoreCase("true");
		}
		
		/**
		 * Returns attribute value whose name matches with key.
		 * @param key Attribute name
		 * @param def Default value, if attribute is not present or mismatching with expected type .
		 * @return Value represented by attribute, if present. If not present returns def.
		 */
		public boolean getBoolean(String key,boolean def)
		{
			String val=(String)get(key);
				try
				{
					return val.equalsIgnoreCase("true");
				}catch(Exception ex)
				{
					return def;
				}
		}
		
		/**
		 * Returns attribute value whose name matches with key. If specified attribute is not present 
		 *  or mismatching with expected type exception is thrown.
		 * @param key Attribute name
		 * @return Value represented by attribute, if present. 
		 */
		public char getChar(String key)
		{
			String val=(String)get(key);
			return val.charAt(0);
		}
		
		/**
		 * Returns attribute value whose name matches with key.
		 * @param key Attribute name
		 * @param def Default value, if attribute is not present or mismatching with expected type .
		 * @return Value represented by attribute, if present. If not present returns def.
		 */
		public char getChar(String key,char def)
		{
			String val=(String)get(key);
				try
				{
					return val.charAt(0);
				}catch(Exception ex)
				{
					return def;
				}
		}
		
		/**
		 * Returns attribute value whose name matches with key. If specified attribute is not present 
		 *  or mismatching with expected type exception is thrown.
		 * @param key Attribute name
		 * @return Value represented by attribute, if present. 
		 */
		public short getShort(String key)
		{
			String val=(String)get(key);
			return Short.parseShort(val);
		}
		
		/**
		 * Returns attribute value whose name matches with key.
		 * @param key Attribute name
		 * @param def Default value, if attribute is not present or mismatching with expected type .
		 * @return Value represented by attribute, if present. If not present returns def.
		 */
		public short getShort(String key,short def)
		{
			String val=(String)get(key);
				try
				{
					return Short.parseShort(val);
				}catch(Exception ex)
				{
					return def;
				}
		}
		
		/**
		 * Returns attribute value whose name matches with key. If specified attribute is not present 
		 *  or mismatching with expected type exception is thrown.
		 * @param key Attribute name
		 * @return Value represented by attribute, if present. 
		 */
		public int getInt(String key)
		{
			String val=(String)get(key);
			return Integer.parseInt(val);
		}
		
		/**
		 * Returns attribute value whose name matches with key.
		 * @param key Attribute name
		 * @param def Default value, if attribute is not present or mismatching with expected type .
		 * @return Value represented by attribute, if present. If not present returns def.
		 */
		public int getInt(String key,int def)
		{
			String val=(String)get(key);
				try
				{
					return Integer.parseInt(val);
				}catch(Exception ex)
				{
					return def;
				}
		}
		
		/**
		 * Returns attribute value whose name matches with key. If specified attribute is not present 
		 *  or mismatching with expected type exception is thrown.
		 * @param key Attribute name
		 * @return Value represented by attribute, if present. 
		 */
		public long getLong(String key)
		{
			String val=(String)get(key);
			return Long.parseLong(val);
		}
		
		/**
		 * Returns attribute value whose name matches with key.
		 * @param key Attribute name
		 * @param def Default value, if attribute is not present or mismatching with expected type .
		 * @return Value represented by attribute, if present. If not present returns def.
		 */
		public long getLong(String key,long def)
		{
			String val=(String)get(key);
				try
				{
					return Long.parseLong(val);
				}catch(Exception ex)
				{
					return def;
				}
		}
		
		/**
		 * Returns attribute value whose name matches with key. If specified attribute is not present 
		 *  or mismatching with expected type exception is thrown.
		 * @param key Attribute name
		 * @return Value represented by attribute, if present. 
		 */
		public float getFloat(String key)
		{
			String val=(String)get(key);
			return Float.parseFloat(val);
		}
		
		/**
		 * Returns attribute value whose name matches with key.
		 * @param key Attribute name
		 * @param def Default value, if attribute is not present or mismatching with expected type .
		 * @return Value represented by attribute, if present. If not present returns def.
		 */
		public float getFloat(String key,float def)
		{
			String val=(String)get(key);
				try
				{
					return Float.parseFloat(val);
				}catch(Exception ex)
				{
					return def;
				}
		}
		
		/**
		 * Returns attribute value whose name matches with key. If specified attribute is not present 
		 *  or mismatching with expected type exception is thrown.
		 * @param key Attribute name
		 * @return Value represented by attribute, if present. 
		 */
		public double getDouble(String key)
		{
			String val=(String)get(key);
			return Double.parseDouble(val);
		}
		
		/**
		 * Returns attribute value whose name matches with key.
		 * @param key Attribute name
		 * @param def Default value, if attribute is not present or mismatching with expected type .
		 * @return Value represented by attribute, if present. If not present returns def.
		 */
		public double getDouble(String key,double def)
		{
			String val=(String)get(key);
				try
				{
					return Double.parseDouble(val);
				}catch(Exception ex)
				{
					return def;
				}
		}
		
		/**
		 * Returns reserve attribute value whose name matches with key.
		 * @param key Attribute name
		 * @param def Default value, if attribute is not present or mismatching with expected type .
		 * @return Value represented by attribute, if present. If not present returns def.
		 */
		public String getReserved(String key,String def)
		{
			String res=(String)getReserved(key);
				if(res==null)
					return def;
			return res;
		}
		
		/**
		 * Returns reserve attribute value whose name matches with key. If specified attribute is not present 
		 *  or mismatching with expected type exception is thrown.
		 * @param key Attribute name
		 * @return Value represented by attribute, if present. 
		 */
		public byte getReservedByte(String key)
		{
			String val=(String)getReserved(key);
			return new Byte(val).byteValue();
		}
		
		/**
		 * Returns reserve attribute value whose name matches with key.
		 * @param key Attribute name
		 * @param def Default value, if attribute is not present or mismatching with expected type .
		 * @return Value represented by attribute, if present. If not present returns def.
		 */
		public byte getReservedByte(String key,byte def)
		{
			String val=(String)getReserved(key);
				try
				{
					return new Byte(val).byteValue();
				}catch(Exception ex)
				{
					return def;
				}
		}
		
		/**
		 * Returns reserve attribute value whose name matches with key. If specified attribute is not present 
		 *  or mismatching with expected type exception is thrown.
		 * @param key Attribute name
		 * @return Value represented by attribute, if present. 
		 */
		public boolean getReservedBoolean(String key)
		{
			String val=(String)getReserved(key);
			return val.equalsIgnoreCase("true");
		}
		
		/**
		 * Returns reserve attribute value whose name matches with key.
		 * @param key Attribute name
		 * @param def Default value, if attribute is not present or mismatching with expected type .
		 * @return Value represented by attribute, if present. If not present returns def.
		 */
		public boolean getReservedBoolean(String key,boolean def)
		{
			String val=(String)getReserved(key);
				try
				{
					return val.equalsIgnoreCase("true");
				}catch(Exception ex)
				{
					return def;
				}
		}
		
		/**
		 * Returns reserve attribute value whose name matches with key. If specified attribute is not present 
		 *  or mismatching with expected type exception is thrown.
		 * @param key Attribute name
		 * @return Value represented by attribute, if present. 
		 */
		public char getReservedChar(String key)
		{
			String val=(String)getReserved(key);
			return val.charAt(0);
		}
		
		/**
		 * Returns reserve attribute value whose name matches with key.
		 * @param key Attribute name
		 * @param def Default value, if attribute is not present or mismatching with expected type .
		 * @return Value represented by attribute, if present. If not present returns def.
		 */
		public char getReservedChar(String key,char def)
		{
			String val=(String)getReserved(key);
				try
				{
					return val.charAt(0);
				}catch(Exception ex)
				{
					return def;
				}
		}
		
		/**
		 * Returns reserve attribute value whose name matches with key. If specified attribute is not present 
		 *  or mismatching with expected type exception is thrown.
		 * @param key Attribute name
		 * @return Value represented by attribute, if present. 
		 */
		public short getReservedShort(String key)
		{
			String val=(String)getReserved(key);
			return Short.parseShort(val);
		}
		
		/**
		 * Returns reserve attribute value whose name matches with key.
		 * @param key Attribute name
		 * @param def Default value, if attribute is not present or mismatching with expected type .
		 * @return Value represented by attribute, if present. If not present returns def.
		 */
		public short getReservedShort(String key,short def)
		{
			String val=(String)getReserved(key);
				try
				{
					return Short.parseShort(val);
				}catch(Exception ex)
				{
					return def;
				}
		}
		
		/**
		 * Returns reserve attribute value whose name matches with key. If specified attribute is not present 
		 *  or mismatching with expected type exception is thrown.
		 * @param key Attribute name
		 * @return Value represented by attribute, if present. 
		 */
		public int getReservedInt(String key)
		{
			String val=(String)getReserved(key);
			return Integer.parseInt(val);
		}
		
		/**
		 * Returns reserve attribute value whose name matches with key or mismatching with expected type .
		 * @param key Attribute name
		 * @param def Default value, if attribute is not present.
		 * @return Value represented by attribute, if present. If not present returns def.
		 */
		public int getReservedInt(String key,int def)
		{
			String val=(String)getReserved(key);
				try
				{
					return Integer.parseInt(val);
				}catch(Exception ex)
				{
					return def;
				}
		}
		
		/**
		 * Returns reserve attribute value whose name matches with key. If specified attribute is not present 
		 *  or mismatching with expected type exception is thrown.
		 * @param key Attribute name
		 * @return Value represented by attribute, if present. 
		 */
		public long getReservedLong(String key)
		{
			String val=(String)getReserved(key);
			return Long.parseLong(val);
		}
		
		/**
		 * Returns reserve attribute value whose name matches with key.
		 * @param key Attribute name
		 * @param def Default value, if attribute is not present or mismatching with expected type .
		 * @return Value represented by attribute, if present. If not present returns def.
		 */
		public long getReservedLong(String key,long def)
		{
			String val=(String)getReserved(key);
				try
				{
					return Long.parseLong(val);
				}catch(Exception ex)
				{
					return def;
				}
		}
		
		/**
		 * Returns reserve attribute value whose name matches with key. If specified attribute is not present 
		 *  or mismatching with expected type exception is thrown.
		 * @param key Attribute name
		 * @return Value represented by attribute, if present. 
		 */
		public float getReservedFloat(String key)
		{
			String val=(String)getReserved(key);
			return Float.parseFloat(val);
		}
		
		/**
		 * Returns reserve attribute value whose name matches with key.
		 * @param key Attribute name
		 * @param def Default value, if attribute is not present or mismatching with expected type .
		 * @return Value represented by attribute, if present. If not present returns def.
		 */
		public float getReservedFloat(String key,float def)
		{
			String val=(String)getReserved(key);
				try
				{
					return Float.parseFloat(val);
				}catch(Exception ex)
				{
					return def;
				}
		}
		
		/**
		 * Returns reserve attribute value whose name matches with key. If specified attribute is not present 
		 *  or mismatching with expected type exception is thrown.
		 * @param key Attribute name
		 * @return Value represented by attribute, if present. 
		 */
		public double getReservedDouble(String key)
		{
			String val=(String)getReserved(key);
			return Double.parseDouble(val);
		}
		
		/**
		 * Returns reserve attribute value whose name matches with key.
		 * @param key Attribute name
		 * @param def Default value, if attribute is not present or mismatching with expected type.
		 * @return Value represented by attribute, if present. If not present returns def.
		 */
		public double getReservedDouble(String key,double def)
		{
			String val=(String)getReserved(key);
				try
				{
					return Double.parseDouble(val);
				}catch(Exception ex)
				{
					return def;
				}
		}
		
		public Object get(String key)
		{
			return get(key,false);
		}
		
		public Object getReserved(Object key)
		{
			return get(key,true);
		}
		
		private Object get(Object key,boolean reserve)
		{
				if(!(key instanceof String))
					return null;
				
			String skey=(String)key;
			Attribute att=super.get(skey);
				if(att==null)
					return null;
				
				if(reserve!=att.isReserved)
					return null;
				
			return att.value;
		}
		
		public String getFirstKey(boolean reserved)
		{
			Set<String> keySet=super.keySet();
			
				if(keySet.isEmpty())
					return null;
			
			Attribute attr=null;
			
				for(String key:keySet)
				{
					attr=super.get(key);
					
						if(attr.isReserved==reserved)
							return key;
				}
				
			return null;
		}
		
		/**
		 * Returns an independent key set. Altering the returned key set will not effect this map.
		 * @param reserved
		 * @return
		 */
		public Set<String> getKeySet(boolean reserved)
		{
			HashSet<String> keys=new HashSet<String>();
			Iterator<String> it=keySet().iterator();
			String key=null;
			Attribute att=null;
			
				while(it.hasNext())
				{
					key=it.next();
					att=super.get(key);
					
						if(att.isReserved==reserved)
							keys.add(key);
				}
				
			return keys;
		}
		
		public boolean containsReservedKey(Object key)
		{
				if(!(key instanceof String))
					return false;
				
			String skey=((String)key).toUpperCase();
			Attribute att=super.get(skey);//this.keySet()
				if(att==null)
					return false;
			return att.isReserved;
		}
		
		public boolean containsKey(Object key)
		{
				if(!(key instanceof String))
					return false;
				
			String skey=((String)key).toUpperCase();
			Attribute att=(Attribute)get(skey,false);
				if(att==null)
					return false;
			return att.isReserved;
		}
		
		public boolean hasReserveAttributes()
		{
			return hasReserveAttributes;
		}
		
		public int getNormalAttributeCount()
		{
			return normalAttCount;
		}
		
		public String toString()
		{
			StringBuffer buff=new StringBuffer("{");
			Iterator<String> it=keySet().iterator();
			Object key=null;
			Attribute value=null;
				while(it.hasNext())
				{
					key=it.next();
					value=super.get(key);
					
					buff.append(key);
						if(value.isReserved)
							buff.append("{R}");
					buff.append("=");
					buff.append(value.value);
					
						if(it.hasNext())
							buff.append(", ");
				}
			
			buff.append("}");
			return buff.toString();
		}
}
