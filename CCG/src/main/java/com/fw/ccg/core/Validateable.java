package com.fw.ccg.core;

/**
 * <BR><BR>
  * <P>
 * This interface is meant for those beans which are expected to be loaded by engines/builders 
 * from different sources.
 * </P>
 * <P>
 * Beans implementing this interface are capable of validating themselves. Bean loading 
 * engines are expected to call validate() method of these beans once the bean is loaded
 * completely.
 * </P>
 * <BR>
 * @author A. Kranthi Kiran
*/
public interface Validateable
{
	/**
	 * Validates this bean.
	 * @throws ValidateException When validation fails.
	 */
	public void validate() throws ValidateException;
}
