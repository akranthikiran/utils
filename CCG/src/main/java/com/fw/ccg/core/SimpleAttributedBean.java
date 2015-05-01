package com.fw.ccg.core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

/**
 * <BR><BR>
 * Simple implementation of AttributedBean interface. This implementation is backed by 
 * a HashMap.
 * <BR>
 * @author A. Kranthi Kiran
 */
public class SimpleAttributedBean implements Attributes,Serializable
{
	private static final long serialVersionUID=1L;
	
	private HashMap attributes;
	private Integer hashCode=null;
		/* (non-Javadoc)
		 * @see com.ccg.core.AttributedBean#setAttribute(java.lang.Object, java.lang.Object)
		 */
		public void setAttribute(Object id,Object value)
		{
				if(attributes==null)
				{
						if(value==null)
							return;
					attributes=new HashMap();
				}
				
				if(id==null)
					throw new IllegalArgumentException("ID cannot be null.");
				
				if(value==null)
				{
					attributes.remove(id);
					hashCode=null;
					return;
				}
			attributes.put(id,value);
			hashCode=null;
		}
		
		public void setAttribute(Object id,byte value)
		{
			setAttribute(id,new Byte(value));
		}

		public void setAttribute(Object id,boolean value)
		{
			setAttribute(id,new Boolean(value));
		}
		
		public void setAttribute(Object id,char value)
		{
			setAttribute(id,new Character(value));
		}
		
		public void setAttribute(Object id,short value)
		{
			setAttribute(id,new Short(value));	
		}
		
		public void setAttribute(Object id,int value)
		{
			setAttribute(id,new Integer(value));
		}
		
		public void setAttribute(Object id,long value)
		{
			setAttribute(id,new Long(value));
		}
		
		public void setAttribute(Object id,float value)
		{
			setAttribute(id,new Float(value));
		}
		
		public void setAttribute(Object id,double value)
		{
			setAttribute(id,new Double(value));
		}
		
		public byte getByteAttribute(Object id)
		{
			return ((Number)getAttribute(id)).byteValue();
		}
		
		public boolean getBooleanAttribute(Object id)
		{
			return ((Boolean)getAttribute(id)).booleanValue();
		}
		
		public char getCharacterAttribute(Object id)
		{
			return ((Character)getAttribute(id)).charValue();
		}
		
		public short getShortAttribute(Object id)
		{
			return ((Number)getAttribute(id)).shortValue();
		}
		
		public int getIntAttribute(Object id)
		{
			return ((Number)getAttribute(id)).intValue();	
		}
		
		public long getLongAttribute(Object id)
		{
			return ((Number)getAttribute(id)).longValue();
		}
		
		public float getFloatAttribute(Object id)
		{
			return ((Number)getAttribute(id)).floatValue();
		}
		
		public double getDoubleAttribute(Object id)
		{
			return ((Number)getAttribute(id)).doubleValue();
		}
		
		public Object removeAttribute(Object id)
		{
			hashCode=null;
			return attributes.remove(id);
		}
	
		/* (non-Javadoc)
		 * @see com.ccg.core.AttributedBean#getAttribute(java.lang.Object)
		 */
		public Object getAttribute(Object id)
		{
				if(attributes==null)
					return null;
				
				if(id==null)
					return null;
			return attributes.get(id);
		}

		/**
		 * @param id
		 * @return Whether specified attribute is present or not.
		 */
		public boolean hasAttribute(Object id)
		{
				if(attributes==null)
					return false;
			
				if(id==null)
					return false;
			return attributes.containsKey(id);
		}
		
		/**
		 * Clears the attributes.
		 */
		public void clear()
		{
				if(attributes==null)
					return;
			attributes.clear();
			attributes=null;
			hashCode=null;
		}
		
		public Iterator getAttributes()
		{
			return attributes.keySet().iterator();
		}

		/**
		 * The specified object "other" is equal if and only if it is of type SimpleAttributedBean
		 * and have same attributes.  
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object other)
		{
				if(this==other)
					return true;
				
				if(!(other instanceof SimpleAttributedBean))
					return false;
				
				if(attributes==null)
					return (((SimpleAttributedBean)other).attributes==null);

			return attributes.equals(((SimpleAttributedBean)other).attributes);
		}

		/**
		 * Hash code returned by this method is equal to hascode returned by the 
		 * hashmap holding attributes and thier values.
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode()
		{
				if(attributes==null)
					return 0;
				
				if(hashCode!=null)
					return hashCode.intValue();
			int hcode=attributes.hashCode();
			hashCode=new Integer(hcode);
			return hcode; 
		}
		
		public Object clone()
		{
			SimpleAttributedBean cloned=new SimpleAttributedBean();
			makeCopy(cloned);
			return cloned;
		}
			
		public void makeCopy(SimpleAttributedBean bean)
		{
				if(bean==null)
					return;
				
				if(attributes==null)
					bean.attributes=null;
				else
					bean.attributes=(HashMap)attributes.clone();
				
			bean.hashCode=hashCode;
		}
}
