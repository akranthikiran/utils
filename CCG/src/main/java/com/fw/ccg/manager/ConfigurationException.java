package com.fw.ccg.manager;


/**
 * <BR><BR>
 * <P>
 * This exception will be thrown when there is exception in the input XML configuration
 * data.
 * </P>
 * <BR><BR>
 * @author A. Kranthi Kiran
 */
public class ConfigurationException extends ManagerException
{
	private static final long serialVersionUID=1L;

		public ConfigurationException()
		{
			super();
		}
	
		public ConfigurationException(String mssg,Throwable rootCause)
		{
			super(mssg,rootCause);
		}
	
		public ConfigurationException(String mssg)
		{
			super(mssg);
		}
	
		public ConfigurationException(Throwable rootCause)
		{
			super(rootCause);
		}
}
