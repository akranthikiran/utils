package com.fw.ccg.xml;

import com.fw.ccg.core.CCGException;

public class XMLWriteException extends CCGException
{
	private static final long serialVersionUID=1L;
	
		public XMLWriteException(WriteableBeanNode node)
		{
			this(node,null,null);
		}
	
		public XMLWriteException(WriteableBeanNode node,String mssg,Throwable rootCause)
		{
			super(buildMessage(mssg,node),rootCause);
		}
	
		public XMLWriteException(WriteableBeanNode node,String mssg)
		{
			this(node,mssg,null);
		}
	
		public XMLWriteException(WriteableBeanNode node,Throwable rootCause)
		{
			this(node,null,rootCause);
		}
		
		private static String buildMessage(String mssg,WriteableBeanNode node)
		{
			StringBuffer buff=new StringBuffer();
			
			buff.append(mssg);
			
				if(node!=null)
					buff.append("\nPath: "+node.getNodePath());
				
			return buff.toString();
		}
}
