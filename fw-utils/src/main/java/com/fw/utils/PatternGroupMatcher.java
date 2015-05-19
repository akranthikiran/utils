package com.fw.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternGroupMatcher
{
	private Matcher matchers[];
	private int matchFoundAt=-1;
	private int nextStart=0;
	private int appendPos=0;
	private String actualString;
	
		public PatternGroupMatcher(String str,Pattern... patterns )
		{
			matchers=new Matcher[patterns.length];
			
				for(int i=0;i<patterns.length;i++)
					matchers[i]=patterns[i].matcher(str);
				
			this.actualString=str;
		}
		
		public boolean find()
		{
			int minIdx=Integer.MAX_VALUE;
			int idx=0;
			int foundIdx=-1;
			
				for(int i=0;i<matchers.length;i++)
				{
						if(!matchers[i].find(nextStart))
							continue;
						
					idx=matchers[i].start();
					
						if(idx<minIdx)
						{
							minIdx=idx;
							foundIdx=i;
						}
				}
				
				if(foundIdx<0)
				{
					matchFoundAt=-1;
					return false;
				}
				
			this.nextStart=matchers[foundIdx].end();
			this.matchFoundAt=foundIdx;
			return true;
		}
		
		public int getGroupCount()
		{
				if(matchFoundAt<0)
					throw new IllegalStateException("No match found.");
				
			return matchers[matchFoundAt].groupCount();
		}
		
		public int getMatchIndex()
		{
			return matchFoundAt;
		}
		
		public String group()
		{
				if(matchFoundAt<0)
					throw new IllegalStateException("No match found.");
				
			return matchers[matchFoundAt].group();
		}
		
		public String group(int grp)
		{
				if(matchFoundAt<0)
					throw new IllegalStateException("No match found.");
				
			return matchers[matchFoundAt].group(grp);
		}
		
		public PatternGroupMatcher appendReplacement(StringBuffer sb,String replacement)
		{
				if(matchFoundAt<0)
					throw new IllegalStateException("No match found.");
				
			int st=matchers[matchFoundAt].start();
				
				if(appendPos>st)
					throw new IllegalStateException("No match found after previous replacement.");

			int ed=matchers[matchFoundAt].end();
			ed=matchers[matchFoundAt].end();
			
				if(appendPos==st)
				{
					sb.append(replacement);
					return this;
				}
				
			String appStr=actualString.substring(appendPos, st);
			
			sb.append(appStr).append(replacement);
			this.appendPos=ed;
			return this;
		}
		
		public StringBuffer appendTail(StringBuffer sb)
		{
			int len=actualString.length();
			
				if(appendPos>=len)
					return sb;
			
			String appStr=actualString.substring(appendPos, len);
			
			sb.append(appStr);
			this.appendPos=len;
			return sb;
		}
		
}
