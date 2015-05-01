package com.fw.ccg.core;

import java.io.Serializable;


/**
 * <BR><BR>
 * A Simple implementation of the Identifiable and Validateable interfaces.
 * <BR>
 * @author A. Kranthi Kiran
 */
public class IdentifiableBean implements Identifiable,Validateable,Serializable
{
	private static final long serialVersionUID=1L;
	private String id;
	
		public IdentifiableBean()
		{}
		
		public IdentifiableBean(IdentifiableBean other)
		{
			this.id=other.id;
		}
		
		/* (non-Javadoc)
		 * @see com.ccg.core.Identifiable#setID(java.lang.String)
		 */
		public void setId(String id)
		{
			this.id=id;
		}
	
		/* (non-Javadoc)
		 * @see com.ccg.core.Identifiable#getID()
		 */
		public String getId()
		{
			return id;
		}
		
		/**
		 * Throws ValidateException if id is not specified.
		 * @see com.fw.ccg.core.Validateable#validate()
		 */
		public void validate() throws ValidateException
		{
			if(id==null || id.length()==0)
				throw new ValidateException("Mandatory attribute \"ID\" is missing.");
		}

		/**
		 * Specified bean "other" is considered equal if the "other" bean is also identifiable and id of this bean 
		 * is equal to the id of the bean.
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object other)
		{
				if(this==other)
					return true;
				
				if(!(other instanceof IdentifiableBean))
					return false;
				
				if(id==null)
					return (((IdentifiableBean)other).id==null);
			return id.equals(((IdentifiableBean)other).id);
		}

		/**
		 * If id is null, then zero (0) is returned, otherwise hashcode of id string is
		 * returned as hascode of this bean.
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode()
		{
				if(id==null)
					return 0;
			return id.hashCode();
		}
		
		public String toString()
		{
			return id;
		}
}
