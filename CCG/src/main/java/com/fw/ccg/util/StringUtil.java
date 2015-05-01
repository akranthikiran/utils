package com.fw.ccg.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil
{
	private static final Pattern pattern=Pattern.compile("\\$+\\{([.[^\\}]]+)\\}");
	
		public static String getPatternString(CharSequence template,Map<String,?> nameToValue)
		{
			return getPatternString(template, new ValueProvider.MapValueProvider(nameToValue),null, null, null);
		}
		
		public static String getPatternString(CharSequence template,ValueProvider nameToValue,Pattern pattern, String escapePrefix, String escapeReplace)
		{
				if(template == null)
				{
					return null;
				}
				
				if(pattern==null)
				{
					pattern=StringUtil.pattern;
					escapePrefix = "$$";
					escapeReplace = "$";
				}
				
				if(escapePrefix == null || escapeReplace == null)
				{
					escapePrefix = "$$";
					escapeReplace = "$";
				}
				
			String templateString=template.toString();
			String name=null;
			Object valueObj=null;
			String value=null;
			Matcher matcher=pattern.matcher(templateString);
			StringBuffer buff=new StringBuffer();
			String match = null;
			
				while(matcher.find())
				{
					match = matcher.group(0);
					name=matcher.group(1);
					
					if(!match.startsWith(escapePrefix))
					{
						valueObj=nameToValue.getValue(name);
						
						if(valueObj==null)
							value="";
						else
							value=valueObj.toString();
					}
					else
					{
						value = escapeReplace + match.substring(escapePrefix.length());
					}
					
	
					matcher.appendReplacement(buff, Matcher.quoteReplacement(value));
				}
			
			matcher.appendTail(buff);
				
			return buff.toString();
		}
	
		public static String[] tokenize(String str,String delimiter)
		{
			return tokenize(str,delimiter,false,false,false);
		}
		
		public static String[] tokenize(String str,String delimiter,boolean trim)
		{
			return tokenize(str,delimiter,false,false,trim);
		}
		
		public static String[] tokenize(String str,String delimiter,boolean includeDelimiter,boolean emptyTokens)
		{
			return tokenize(str,delimiter,includeDelimiter,emptyTokens,false);
		}
		/**
		 * Checks if the token value is present in the specified delim[] array.
		 * @param token
		 * @param delim
		 * @return
		 */
		private static boolean isDelimiter(String token,char delim[])
		{
				if(delim==null || delim.length==0)
					return false;
				
				if(token.length()!=1)
					return false;
			
				if(Arrays.binarySearch(delim,token.charAt(0))>=0)
					return true;
			return false;
		}
		
		/**
		 * Tokenizes str into string array using "delimiter" as delimiter.
		 * @param str	String to be tokenized.
		 * @param delimiter	Delimiter(s) to be used for tokenizing.
		 * @param includeDelimiter	Whether the result array should include delimiters.
		 * @param emptyTokens	Whether the result should include empty tokens.
		 * @return Tokenized array.
		 */
		public static String[] tokenize(String str,String delimiter,boolean includeDelimiter,boolean emptyTokens,boolean trim)
		{
				if(str==null)
					throw new NullPointerException("Source string can not be null.");
				
				if(delimiter==null)
					throw new NullPointerException("Delimiter string can not be null.");
				
				if(str.length()==0)
					return new String[]{""};
			ArrayList<String> lst=new ArrayList<String>();
			StringTokenizer st=new StringTokenizer(str,delimiter,true);
			String token=null;
			boolean delimFlag=true;//represents whether previous token is delimiter
							//inialized to true, to consider first empty token (if present)
			char delimChar[]=delimiter.toCharArray();
			
			Arrays.sort(delimChar);
			boolean checkDelim=(delimFlag || emptyTokens);
			
				while(st.hasMoreTokens())
				{
					token=st.nextToken();
						if(checkDelim && isDelimiter(token,delimChar))
						{
								if(delimFlag && emptyTokens)//if previous token is delimiter
									lst.add("");
							
								if(includeDelimiter)
									lst.add(token);
								
							delimFlag=true;
							continue;
						}
						
						if(trim)
							lst.add(token.trim());
						else
							lst.add(token);
						
					delimFlag=false;
				}
				
				if(delimFlag && emptyTokens)
					lst.add("");
				
				if(lst.size()==0)
					return null;
			return lst.toArray(new String[0]);
		}
		
		/**
		 * Tokenizes str into string array using "delimiter" as delimiter. But in this version
		 * an escape character can be specified. Any character preceeded by specified escape 
		 * character (including delimiter and escape character itself) that character will get
		 * simply gets appended to the previous token(if any, otherwise gets prepended to the 
		 * next token). If escape char is specified at the end of the str, it will get simply 
		 * ignored.
		 * <BR>
		 * @param str String to be tokenized.
		 * @param delimiter Delimiter to be used for tokenizing.
		 * @param includeDelimiter Specified whether delimiters should be included in result array.
		 * @param emptyTokens Specified whether result should include empty tokens.
		 * @param esc Escape character to be used.
		 * @return Tokenized elements.
		 */
		public static String[] tokenize(String str,String delimiter,boolean includeDelimiter,boolean emptyTokens,char esc)
		{
				if(str==null)
					throw new NullPointerException("Source string can not be null.");
				
				if(delimiter==null)
					throw new NullPointerException("Delimiter string can not be null.");
			
				if(str.length()==0)
					return new String[]{""};
			ArrayList<String> lst=new ArrayList<String>();
			StringTokenizer st=new StringTokenizer(str,delimiter+esc,true);
			String token=null;
			boolean delimFlag=true;//represents whether previous token is delimiter
							//inialized to true, to consider first empty token (if present)
			boolean escFlag=false;
			char delimChar[]=delimiter.toCharArray();
			String escStr=""+esc;
			String nextToken=null;
			String pretoken=null;
				while(st.hasMoreTokens())
				{
					token=st.nextToken();
						if(isDelimiter(token,delimChar))
						{
								if(pretoken!=null)//if char is escaped before delimiter
								{
									lst.add(pretoken);
									pretoken=null;
								}
								
								if(delimFlag && emptyTokens)//if previous token is delimiter
									lst.add("");
								
								if(includeDelimiter)
									lst.add(token);
								
							delimFlag=true;
							escFlag=false;
							continue;
						}
						
						/*
						 * If escape char is encountered, then irrespective of the type of
						 * next token escape char, delimiter or normal char string
						 * attach it to the current token.
						 * 
						 * If end of tokens is reached then simple ignore the escape char
						 */
						if(token.equals(escStr))
						{
								if(!st.hasMoreTokens())
								{
										if(delimFlag && emptyTokens)
											lst.add("");
									break;
								}
								
							nextToken=st.nextToken();
							
								if(pretoken!=null)
								{
									nextToken=pretoken+nextToken;
									pretoken=null;
								}
							
								if(delimFlag || escFlag)//if esc char is at start 
									pretoken=nextToken;
								else
								{
										if(lst.size()>0)
											pretoken=lst.remove(lst.size()-1)+nextToken;
										else
											pretoken=nextToken;
								}
								
								if(!st.hasMoreTokens())//if end is reached
								{
									lst.add(pretoken);
									break;
								}
								
							delimFlag=false;
							escFlag=true;
							continue;
						}
						
						if(pretoken!=null)
							lst.add(pretoken+token);
						else
							lst.add(token);
						
					delimFlag=false;
					escFlag=false;
					pretoken=null;
				}
				
				if(lst.size()==0)
					return null;
			return lst.toArray(new String[0]);
		}
		
		/**
		 * Replaces "src" occurances in "str" with "dst".
		 * @param str String in which replacement should happen.
		 * @param src Source string to be replaced in str.
		 * @param dst Destination replacement string.
		 * @return Resultant string after replacement.
		 */
		public static String replace(String str,String src,String dst)
		{
				if(str==null)
					return null;
				
				if(src==null || src.length()==0)
					throw new NullPointerException("Replacement source string can not be null or empty string.");
				
				if(dst==null)
					throw new NullPointerException("Replacement destination string can not be null.");
			StringBuffer res=new StringBuffer();
			int curIdx=0;
			int idx=0;
			int len=src.length();
				while((idx=str.indexOf(src,curIdx))>=0)
				{
						if(curIdx<idx)
							res.append(str.substring(curIdx,idx));
					res.append(dst);
					
					curIdx=idx+len;
				}
				
				if(curIdx<str.length())
					res.append(str.substring(curIdx,str.length()));
			return res.toString();
		}
		
		/**
		 * Converts first char of input string into upper case and returns the same.
		 * For example, "name" into "Name".
		 * @param s Input string.
		 * @return Result of conversion
		 */
		public static String toStartUpper(String s)
		{
				if(s==null)
					return null;
				
				if(s.length()==0)
					return s;
				
				if(s.length()>1)
					s=Character.toUpperCase(s.charAt(0))+s.substring(1);
				else
					s=String.valueOf(Character.toUpperCase(s.charAt(0)));
			return s;
		}
		
		/**
		 * Converts first char of input string into lower case and returns the same.
		 * For example, "Name" into "name".
		 * @param s Input string.
		 * @return Result of conversion
		 */
		public static String toStartLower(String s)
		{
				if(s==null)
					return null;
				
				if(s.length()==0)
					return s;
				
				if(s.length()>1)
					s=Character.toLowerCase(s.charAt(0))+s.substring(1);
				else
					s=String.valueOf(Character.toLowerCase(s.charAt(0)));
			return s;
		}
		
		public static String getFirstWord(String s)
		{
			Pattern pattern=Pattern.compile("(\\w+)");
			Matcher matcher=pattern.matcher(s);
			
				if(matcher.find())
					return matcher.group(1);
				
			return null;
		}
		
		public static String joingString(Object arr[],String delim)
		{
			return joingString(new ArrayIterable<Object>(arr),delim,null,null);
		}
		
		public static String joingString(Iterable<? extends Object> iterable,String delim)
		{
			return joingString(iterable,delim,null,null);
		}
		
		public static String joingString(Object arr[],String delim,String start,String end)
		{
			return joingString(new ArrayIterable<Object>(arr),delim,start,end);
		}
	
		public static String joingString(Iterable<? extends Object> iterable,String delim,String start,String end)
		{
				if(iterable==null)
					return null;
				
			StringBuilder builder=new StringBuilder();
			boolean secondElem=false;
			
				if(start!=null)
					builder.append(start);
			
				for(Object s:iterable)
				{
						if(secondElem && delim!=null)
							builder.append(delim);
						
					builder.append(s);
					secondElem=true;
				}
				
				if(end!=null)
					builder.append(end);
				
			return builder.toString();
		}
		
		public static String toString(Object arr[])
		{
			return "["+joingString(arr,",")+"]";
		}
}
