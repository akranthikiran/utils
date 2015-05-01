package com.fw.ccg.beans;

import java.io.Serializable;

/**
 * <BR>
 * <P>
 * A wrapper to the string class. This class is also immutable like string with the only
 * difference, that the comparison (in equals() method) and hashCode() calculation are
 * case independent.
 * </P>
 * <BR>
 * @author A. Kranthi Kiran
 */
class FieldName implements Cloneable,Serializable
{
	private static final long serialVersionUID=1L;
	
	/**
	 * Actual value the current instance is holding.
	 */
	private String name;
	/**
	 * Upper case version of actual value. This is needed to avoid upper case conversion
	 * in equals() and hashCode(), which helps in performance improvement.
	 */
	private String upperCaseVersion;
	
		/**
		 * @param name Value this wrapper needs to hold.
		 */
		public FieldName(String name)
		{
			this.name=name;
			upperCaseVersion=name.toUpperCase();
		}
		
		/**
		 * Used in clone().
		 * @param name The value this wrapper needs to hold.
		 * @param uversion Upper case version of name
		 */
		private FieldName(String name,String uversion)
		{
			this.name=name;
			upperCaseVersion=uversion;
		}
		/**
		 * @return Value specified in the constructor.
		 */
		public String getName()
		{
			return name;
		}
		
		/**
		 * Returns true if the other is instrance of String or FieldName and is equal 
		 * (case insensitive) to the string this field is holding.
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object other)
		{
				if(this==other)
					return true;
				
				if(other instanceof String)
				{
					return upperCaseVersion.equalsIgnoreCase(((String)other).toUpperCase());
				}
				else if(other instanceof FieldName)
				{
					return upperCaseVersion.equals(((FieldName)other).upperCaseVersion);
				}
			return false;
		}
		
		/**
		 * The cloned version will share same instances of name and its upper case version
		 * (for the sake of memory efficiency).
		 * @see java.lang.Object#clone()
		 */
		public Object clone()
		{
			return new FieldName(name,upperCaseVersion);
		}
		
		/**
		 * Hash code of the upper case version of name.
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode()
		{
			return upperCaseVersion.hashCode();
		}
		
		/**
		 * Returns the name this wrapper is holding.
		 * @see java.lang.Object#toString()
		 */
		public String toString()
		{
			return name;
		}
}
