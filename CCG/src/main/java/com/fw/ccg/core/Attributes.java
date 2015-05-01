package com.fw.ccg.core;

/**
 * <BR><BR>
 * Classes implementing this interface will have capability of having attachments
 * (attributes) to thier instances.
 * <BR>
 * @author A. Kranthi Kiran
 */
public interface Attributes
{
	/**
	 * Id should not be accepted as null.
	 * If value is null, the specified id entry will be removed.
	 * @param id
	 * @param value
	 */
	public void setAttribute(Object id,Object value);
	
	/**
	 * @param id
	 * @return the value mapped with id. null if no attribute exists with 
	 * specified id. 
	 */
	public Object getAttribute(Object id);
	
	public Object removeAttribute(Object id);
}
