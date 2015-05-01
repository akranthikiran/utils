package com.fw.dao.qry;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import com.fw.ccg.util.CCGUtility;
import com.fw.dao.qry.impl.MapQueryFilter;

/**
 * This class provides list of default functions that are available for usage while defining 
 * queries.
 */
public class DefaultQueryFunctions
{
	/**
	 * Default format used by date functions.
	 */
	public static final SimpleDateFormat STD_DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy");

	/**
	 * Converts "s" into its lower case version
	 * @param s Text to be converted into lower case
	 * @return Lower case version of "s". Null if "s" is null.
	 */
	@QueryFunction
	public static String lower(String s)
	{
		if(s == null)
			return null;

		return s.toLowerCase();
	}

	/**
	 * Converts "s" into its upper case version
	 * @param s Text to be converted into upper case
	 * @return Upper case version of "s". Null if "s" is null.
	 */
	@QueryFunction
	public static String upper(String s)
	{
		if(s == null)
			return null;

		return s.toUpperCase();
	}

	/**
	 * Converts data into String into "format" format.
	 * <BR/><B>Minimum Argument Count:</B> 1
	 * @param date Date to be converted.
	 * @param format OPTIONAL. If not specified STD_DATE_FORMAT will be used.
	 * @return String version of specified date.
	 */
	@QueryFunction(minArgCount = 1)
	public static String dateToStr(Date date, String format)
	{
		if(date == null)
			return null;

		SimpleDateFormat dateFormat = null;

		if(format != null)
			dateFormat = new SimpleDateFormat(format);
		else
			dateFormat = STD_DATE_FORMAT;

		return dateFormat.format(date);
	}

	/**
	 * Converts specified text to Date. dateStr should be in "format" date-format.
	 * <BR/><B>Minimum Argument Count:</B> 1
	 * @param dateStr String to be converted to Date
	 * @param format OPTIONAL. Format of dateStr. If not specified STD_DATE_FORMAT will be used.
	 * @return Converted date object
	 */
	@QueryFunction(minArgCount = 1)
	public static Date strToDate(String dateStr, String format)
	{
		if(dateStr == null)
			return null;

		SimpleDateFormat dateFormat = null;

		if(format != null)
			dateFormat = new SimpleDateFormat(format);
		else
			dateFormat = STD_DATE_FORMAT;

		try
		{
			return dateFormat.parse(dateStr);
		}catch(Exception ex)
		{
			throw new IllegalArgumentException("Invalid date string encountered: " + dateStr + "\nDate expected in format: " + dateFormat);
		}
	}

	/**
	 * Converts specified collection into String.
	 * <BR/><BR/>
	 * Example:<BR/>
	 * 		//arrLst = {a,b,c,d,e}<BR/>
	 * 		colToStr(arrLst,"[","-","]");<BR/>
	 * 		Result: [a-b-c-d-e]
	 * 
	 * <BR/><BR/><B>Note:</B> Use escape character "\" when special character needs to be used for 
	 * prefix, separator or for postfix
	 * 
	 * <BR/><B>Minimum Argument Count:</B> 1
	 * @param values Collection to be converted into String.
	 * @param prefix OPTIONAL. Prefix for the resultant string. If not provided, empty string will be used.
	 * @param separator OPTIONAL.Separator for the values.  If not provided, empty string will be used.
	 * @param postfix OPTIONAL. Postfix for the resultant string.  If not provided, empty string will be used.
	 * @return Resultant string conversion of values.
	 * @throws NullPointerException If values is null
	 */
	@QueryFunction(minArgCount = 1)
	public static String colToStr(Collection<?> values, String prefix, String separator, String postfix)
	{
		StringBuilder finalValue = new StringBuilder();

		if(prefix != null)
			finalValue.append(prefix);

		if(separator == null || separator.length() == 0)
			separator = null;

		Iterator<?> it = values.iterator();
		Object v = null;

		while(it.hasNext())
		{
			v = it.next();
			finalValue.append(v);

			if(separator != null && it.hasNext())
				finalValue.append(separator);
		}

		if(postfix != null)
			finalValue.append(postfix);

		return finalValue.toString();
	}

	/**
	 * Converts specified array into String.
	 * <BR/><BR/>
	 * Example:<BR/>
	 * 		//arr = {a,b,c,d,e}<BR/>
	 * 		arrToStr(arr,"[","-","]");<BR/>
	 * 		Result: [a-b-c-d-e]
	 * 
	 * <BR/><BR/><B>Note:</B> Use escape character "\" when special character needs to be used for 
	 * prefix, separator or for postfix
	 * 
	 * <BR/><B>Minimum Argument Count:</B> 1
	 * @param arr Array to be converted into String.
	 * @param prefix OPTIONAL. Prefix for the resultant string. If not provided, empty string will be used.
	 * @param separator OPTIONAL.Separator for the values.  If not provided, empty string will be used.
	 * @param postfix OPTIONAL. Postfix for the resultant string.  If not provided, empty string will be used.
	 * @return Resultant string conversion of values.
	 * @throws NullPointerException If arr is null
	 */
	@QueryFunction(minArgCount = 1)
	public static String arrToStr(Object arr, String prefix, String separator, String postfix)
	{
		Class<?> arrCls = arr.getClass();

		if(!arrCls.isArray())
			throw new IllegalArgumentException("Only arrays are supported by this function\nExpected: Array\nFound: " + arr);

		StringBuilder finalValue = new StringBuilder();

		if(prefix != null)
			finalValue.append(prefix);

		if(separator == null || separator.length() == 0)
			separator = null;

		String sepStr = (separator == null)? "": separator;
		int len = Array.getLength(arr);
		int len1 = len - 1;

		for(int i = 0; i < len; i++)
		{
			finalValue.append(Array.get(arr, i));

			if(i < len1)
				finalValue.append(sepStr);
		}

		if(postfix != null)
			finalValue.append(postfix);

		return finalValue.toString();
	}

	@QueryFunction(minArgCount = 3)
	public static Object[] strToArray(String str, Class<?> targetType, String separator) throws ClassNotFoundException
	{
		if(str == null || str.trim().length() == 0)
		{
			return null;
		}
		
		String strArr[] = str.split(separator);
		List<Object> res = new ArrayList<>(strArr.length);
		
		for(String strVal: strArr)
		{
			res.add(CCGUtility.toObject(strVal, targetType, null));
		}
		
		return res.toArray((Object[])Array.newInstance(targetType, res.size()));
	}
	
	
	/**
	 * Tokenizes valObj into List of Strings using specified delimiter.
	 * <BR/>Before tokenizing toStr() will be invoked on valObj to convert it into String.
	 * <BR/><B>Minimum Argument Count:</B> 1
	 * @param valObj Object to be tokenized.
	 * @param delimRegex OPTIONAL. Delimiter regular expression for tokenizing. 
	 * 			If not specified, all white-spaces will be used as delimiters.
	 * @return List of strings after tokenizing.
	 */
	@QueryFunction(minArgCount = 1)
	public static List<String> toList(Object valObj, String delimRegex)
	{
		if(valObj == null)
			return null;

		String val = toStr(valObj);

		if(delimRegex == null)
			delimRegex = "\\s";

		Pattern delimPtrn = Pattern.compile(delimRegex);
		String tokens[] = delimPtrn.split(val);
		ArrayList<String> lst = new ArrayList<String>(tokens.length);

		for(String s : tokens)
			lst.add(s);

		return lst;
	}

	@QueryFunction(minArgCount = 1)
	public static String[] toArray(Object valObj, String delimRegex)
	{
		List<String> lst = toList(valObj, delimRegex);

		if(lst == null)
		{
			return null;
		}

		return lst.toArray(new String[0]);
	}

	/**
	 * Tokenizes valObj into Set of Strings using specified delimiter.
	 * <BR/>Before tokenizing toStr() will be invoked on valObj to convert it into String.
	 * <BR/><BR/>
	 * If setType is "sorted" then the result set will be in sorted order. Otherwise the resultant
	 * set will in the order of tokenizing.
	 * 
	 * <BR/><B>Minimum Argument Count:</B> 1
	 * @param valObj Object to be tokenized.
	 * @param delimRegex OPTIONAL. Delimiter regular expression for tokenizing. 
	 * 			If not specified, all white-spaces will be used as delimiters.
	 * @param setType Type of set. "sorted" or "ordered"
	 * @return Set of strings after tokenizing.
	 */
	@QueryFunction(minArgCount = 1)
	public static Set<String> toSet(Object valObj, String delimRegex, String setType)
	{
		if(valObj == null)
			return null;

		String val = toStr(valObj);

		if(delimRegex == null)
			delimRegex = "\\s";

		Pattern delimPtrn = Pattern.compile(delimRegex);
		String tokens[] = delimPtrn.split(val);
		Set<String> set = ("sorted".equals(setType))? new TreeSet<String>(): new LinkedHashSet<String>();

		for(String s : tokens)
			set.add(s);

		return set;
	}

	/**
	 * Converts specified obj into String.
	 * <BR/><B>Minimum Argument Count:</B> 1
	 * @param obj Object to be converted
	 * @return String version of obj
	 */
	@QueryFunction(minArgCount = 1)
	public static String toStr(Object obj)
	{
		if(obj == null)
			return null;

		if(obj instanceof String)
			return (String)obj;

		if(obj instanceof byte[])
			return new String((byte[])obj);

		if(obj instanceof char[])
			return new String((char[])obj);

		if(obj instanceof Blob)
		{
			Blob blob = (Blob)obj;
			try
			{
				return readStream(blob.getBinaryStream());
			}catch(Exception ex)
			{
				throw new IllegalStateException("An error occured while reading blob data.", ex);
			}
		}

		if(obj instanceof Clob)
		{
			Clob clob = (Clob)obj;
			try
			{
				return readStream(clob.getAsciiStream());
			}catch(Exception ex)
			{
				throw new IllegalStateException("An error occured while reading clob data.", ex);
			}
		}

		return obj.toString();
	}

	private static String readStream(InputStream is) throws IOException
	{
		byte buff[] = new byte[1024];
		int read = 0;
		StringBuilder res = new StringBuilder();

		while((read = is.read(buff)) > 0)
		{
			res.append(new String(buff, 0, read));
		}

		return res.toString();
	}

	/**
	 * Converts java.sql.Blob data to byte[].
	 * <BR/><BR/>
	 * data can be of type Blob or byte[]. If data is byte[] type, the same will be returned unaltered. 
	 * <BR/><B>Minimum Argument Count:</B> 1
	 * @param data Data to be converted.
	 * @return byte[] version of specified blob.
	 */
	@QueryFunction(minArgCount = 1)
	public static byte[] blobToByte(Object data)
	{
		if(data == null)
			return null;

		if(data instanceof byte[])
			return (byte[])data;

		if(!(data instanceof Blob))
			throw new IllegalArgumentException("Invalid data specified in place of blob: " + data.getClass().getName());

		Blob blob = (Blob)data;

		byte buff[] = new byte[1024];
		int read = 0;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		try
		{
			InputStream is = blob.getBinaryStream();

			while((read = is.read(buff)) > 0)
			{
				bos.write(buff, 0, read);
			}

			bos.flush();
		}catch(Exception ex)
		{
			throw new IllegalStateException("An error occured while reading blob data", ex);
		}

		return bos.toByteArray();
	}

	@QueryFunction(minArgCount = 1)
	public static Object toJavaObj(Object data)
	{
		byte bdata[] = blobToByte(data);

		if(bdata == null || bdata.length == 0)
			return null;

		try
		{
			ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bdata));
			return ois.readObject();
		}catch(Exception ex)
		{
			throw new IllegalStateException("An error occured while reading java (serialized) object.", ex);
		}
	}

	@QueryFunction(minArgCount = 1)
	public static byte[] javaObjToBytes(Object data)
	{
		try
		{
			if(data == null)
			{
				return null;
			}

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);

			oos.writeObject(data);
			oos.flush();

			bos.flush();
			return bos.toByteArray();
		}catch(Exception ex)
		{
			throw new IllegalStateException("An error occured while converting java (serialized) object to bytes.", ex);
		}
	}

	/**
	 * Replaces all "regex" regular-expression matches in str with rep.
	 * <BR/><B>Minimum Argument Count:</B> 1
	 * @param str String in which replacement needs to happen
	 * @param regex Regular-expression to be replaced
	 * @param rep Value that will be used for replacement.
	 * @return Resultant string after replacement
	 */
	@QueryFunction(minArgCount = 3)
	public static String regExpRep(String str, String regex, String rep)
	{
		if(str == null)
			return null;

		return str.replaceAll(regex, rep);
	}

	@QueryFunction(minArgCount = 3)
	public static String strReplace(String str, String source, String dst)
	{
		if(str == null)
			return null;

		return str.replace(source, dst);
	}

	/**
	 * If "arg" equals "val" returns "trueVal" otherwise "falseVal" will be returned.
	 * <BR/><BR/>
	 * If "arg" or "val" is null then falseVal will be returned.
	 * <BR/><B>Minimum Argument Count:</B> 1
	 * @param arg
	 * @param val
	 * @param trueVal
	 * @param falseVal
	 * @return
	 */
	@QueryFunction(minArgCount = 3)
	public static Object decode(Object arg, String val, Object trueVal, Object falseVal)
	{
		if(arg == null || val == null)
			return falseVal;

		String argStr = arg.toString();

		if(argStr.equals(val))
			return trueVal;

		return falseVal;
	}

	@QueryFunction(minArgCount = 2)
	public static Integer addInt(int arg, String value)
	{
		int iValue = Integer.parseInt(value);
		return arg + iValue;
	}

	@QueryFunction(minArgCount = 2)
	public static Object queryResult(String queryName, String executionType, 
			String argName1, Object arg1, 
			String argName2, Object arg2,
			String argName3, Object arg3) throws SQLException
	{
		QueryManager queryManager = QueryManager.getQueryManager();
		MapQueryFilter filter = new MapQueryFilter();
		
		if(argName1 != null && arg1 != null)
		{
			filter.addValue(argName1, arg1);
		}
		
		if(argName2 != null && arg2 != null)
		{
			filter.addValue(argName2, arg2);
		}
		
		if(argName3 != null && arg3 != null)
		{
			filter.addValue(argName3, arg3);
		}
		
		switch(executionType)
		{
			case "fetchBean":
				return queryManager.fetchBean(queryName, filter);
			case "fetchBeans":
				return queryManager.fetchBeans(queryName, filter);
			case "fetchInt":
				return queryManager.fetchInt(queryName, filter);
			case "fetchLong":
				return queryManager.fetchLong(queryName, filter, 0);
			case "fetchSingleColumnList":
				return queryManager.fetchSingleColumnList(queryName, filter);
			default:
				throw new IllegalArgumentException("Unsupported execution type specified: " + queryName);
		}
	}

	@QueryFunction(minArgCount = 1)
	public static String toString(Object value)
	{
		if(value == null)
		{
			return null;
		}
		
		return value.toString();
	}
}
