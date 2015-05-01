package com.fw.ccg.xml;

import com.fw.ccg.core.CCGException;

/**
 * <BR><BR>
 * Thrown when loading od XML data fails.
 * <BR>
 * @author A. Kranthi Kiran
 */
public class XMLLoadException extends CCGException
{
	private static final long serialVersionUID=1L;

	/**
	 * Build XMLLoadException with specified values.
	 * @param mssg Message
	 * @param thr Root cause. 
	 * @param node Node responsible for exception.
	 */
		public XMLLoadException(String mssg,Throwable thr,BeanNode node)
		{
			super(buildMessage(mssg,node),thr);
		}
	
		/**
		 * Build XMLLoadException with specified values.
		 * @param mssg Message
		 * @param node Node responsible for exception.
		 */
		public XMLLoadException(String mssg,BeanNode node)
		{
			this(mssg,null,node);
		}
		
		/**
		 * Build XMLLoadException with specified values.
		 * @param mssg Message
		 * @param thr Root cause. 
		 */
		public XMLLoadException(String mssg,Throwable thr)
		{
			this(mssg,thr,null);
		}
	
		/**
		 * Builds the body of this exception.
		 * @param mssg Message
		 * @param thr Root cause. 
		 * @param path Path represnting XML node repsonsible for this exception.
		 * @return String represnting the body of this exception.
		 */
		private static String buildMessage(String mssg,BeanNode node)
		{
			StringBuffer buff=new StringBuffer();
			
			buff.append(mssg);
			
				if(node!=null)
					buff.append("\nPath: "+node.getNodePath());
				
			return buff.toString();
		}
}
