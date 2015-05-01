package com.fw.ccg.core;


/**
 * <BR><BR>
  * <P>
 * This interface is meant for those engines/builders which loads beans  
 * from different sources. 
 * </P>
 * This objects will be used to validate all/selective beans by the bean engines/builders.
 * <BR><BR>
 * @author A. Kranthi Kiran
 */
public interface BeanValidator
{
	/**
	 * Validates specified value. This method s
	 * @param bean
	 * @throws ValidateException When validation fails.
	 */
	public void validate(Object bean) throws ValidateException;
}
