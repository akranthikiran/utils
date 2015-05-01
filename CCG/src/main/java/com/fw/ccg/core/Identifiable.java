package com.fw.ccg.core;

/**
 * <BR><BR>
 * <P>
 * This interface is meant for those beans which are expected to be loaded by engines/builders 
 * from different sources.
 * </P>
 * <P>
 * Beans implementing this interface will be identifiable with a string id. Bean loading 
 * engines are expected to set id for this identifiable beans (if id mechanism exists).
 * </P>
 * <BR>
 * @author A. Kranthi Kiran
 */
public interface Identifiable
{
	/**
	 * Sets identification for this bean.
	 * @param id
	 */
	public void setId(String id);
	
	/**
	 * Returns identity string of this bean.
	 * @return
	 */
	public String getId();
}
