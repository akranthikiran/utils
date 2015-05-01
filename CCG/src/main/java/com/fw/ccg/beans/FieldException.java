package com.fw.ccg.beans;

import com.fw.ccg.core.CCGException;

/**
 * <BR><BR>
 * This exception is thrown when an unsupported field, or a unsupported value 
 * for a field is used.
 * <BR>
 * @author A. Kranthi Kiran
 */
public class FieldException extends CCGException
{
	private static final long serialVersionUID=1L;
		public FieldException(String field,Class expectedType,Class type)
		{
			super(buildMessage(field,expectedType,type));
		}
		
		public FieldException(String mssg,Throwable rootCause)
		{
			super(mssg,rootCause);
		}

		public FieldException(String mssg)
		{
			super(mssg);
		}

		public FieldException(Throwable rootCause)
		{
			super(rootCause);
		}

		private static String buildMessage(String field,Class expectedType,Class type)
		{
			StringBuffer sb=new StringBuffer();
			sb.append("Invalid value type encountered for field \""+field+"\" ");
			sb.append("\nExpected type: "+ expectedType.getName());
			sb.append("\nFound: "+ type.getName());
			return sb.toString();
		}
}
