package com.fw.ccg.manager;

/**
 * © Copyright 2006 IBM Corporation
 * <BR><BR>
 * Property beans (properties of dynamic manager) implementing this interface will be given 
 * access to the manager instance holding this bean and hence access to the other property 
 * beans and properties the manager is holding.
 * <BR>
 * @author A. Kranthi Kiran
 */
public interface ManagerBean
{
	/**
	 * <P>
	 * Called by ManagerBuilder immediately after creation of the bean. Note, at this time,
	 * when this method is invoked, none of the configured properties (if any) are not loaded
	 * to the bean. And dynamic manager also is not yet completely built. <BR>
	 * </P>
	 * <P>
	 * The beans are expected to maintain this manager instance at the instance level variable
	 * with transient specifier (Especially Serializable beans, otherwise exceptions will
	 * be thrown during serialization).
	 * </P>
	 * <P>
	 * If the current bean is serializable and the respective dynamic manager supports 
	 * caching mechanism, then this method will be called again every time this bean gets
	 * deserialized.
	 * </P> 
	 * @param manager Dynamic manager holding this bean.
	 */
	public void setManager(Object manager);
}
