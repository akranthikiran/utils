package com.fw.ccg.core;

public class UnsupportedDataTypeException extends CCGException
{
	private static final long serialVersionUID=1L;

		public UnsupportedDataTypeException()
		{
			super();
		}
	
		public UnsupportedDataTypeException(String mssg,Throwable rootCause)
		{
			super(mssg,rootCause);
		}
	
		public UnsupportedDataTypeException(String mssg)
		{
			super(mssg);
		}
	
		public UnsupportedDataTypeException(Throwable rootCause)
		{
			super(rootCause);
		}
}
