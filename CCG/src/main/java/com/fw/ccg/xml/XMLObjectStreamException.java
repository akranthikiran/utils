package com.fw.ccg.xml;

public class XMLObjectStreamException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

		public XMLObjectStreamException()
		{
		}
	
		public XMLObjectStreamException(String message)
		{
			super(message);
		}
	
		public XMLObjectStreamException(Throwable cause)
		{
			super(cause);
		}
	
		public XMLObjectStreamException(String message, Throwable cause)
		{
			super(message, cause);
		}
}
