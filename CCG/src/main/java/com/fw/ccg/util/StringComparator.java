package com.fw.ccg.util;

import java.util.Comparator;

public class StringComparator implements Comparator<String>
{
	private boolean ignoreCase=true;
	
		public StringComparator()
		{}
		
		public StringComparator(boolean ignoreCase)
		{
			this.ignoreCase=ignoreCase;
		}
		
		public int compare(String str1,String str2)
		{
				if(str1==str2)
					return 0;
			
				if(str1==null)
					return -1;
				
				if(str2==null)
					return 1;
				
				if(ignoreCase)
					return str1.compareToIgnoreCase(str2);
				
			return str1.compareTo(str2);
		}

}
