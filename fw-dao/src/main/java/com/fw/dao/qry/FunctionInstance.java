package com.fw.dao.qry;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Represents compiled function instance in the queries. Parameters to these functions can be parameters
 * column-names, constants or other functions.  
 */
public class FunctionInstance
{
	/**
	 * Pattern for parsing functions
	 */
	public static Pattern FUNC_PATTERN = Pattern.compile("(\\w+)\\s*\\(([^\\{\\}]+)\\)");
	private static Logger logger = LogManager.getLogger(FunctionInstance.class);

	/**
	 * Class to represent param arguments 
	 */
	public static class Param
	{
		/**
		 * Name of the parameter argument
		 */
		public final String name;

		public Param(String name)
		{
			this.name = name;
		}

		public String toString()
		{
			return name;
		}
	}

	/**
	 * Class to represent column argument
	 */
	public static class Column
	{
		/**
		 * Name of the column argument
		 */
		public final String name;

		public Column(String name)
		{
			this.name = name;
		}

		public String toString()
		{
			return name;
		}
	}

	/**
	 * While invoking a function instance, DataProvider will provide data 
	 * for the parameters of the function.
	 */
	public static interface DataProvider
	{
		/**
		 * Will be invoked to fetch param argument value with name "name".
		 * @param funcName Function name for which this method is being invoked.
		 * @param name Name of the param argument
		 * @return Value for specified param argument.
		 */
		public Object getProperty(String funcName, String name);

		/**
		 * Will be invoked to fetch column argument value with name "name".
		 * @param funcName Function name for which this method is being invoked.
		 * @param name Name of the column
		 * @return Value of the specified column
		 */
		public Object getColumn(String funcName, String name);
	}

	private String funcStr;
	private String name;
	private Object params[];

	private FunctionInstance(String funcStr, String name, Object params[])
	{
		this.funcStr = funcStr;
		this.name = name;
		this.params = params;
	}

	/**
	 * Method to parse string into function instance. funcExpr should be in format <BR/>
	 *            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	 *            <b>functionName(arg1,arg2,...)</b><BR/>
	 * Arguments can be <BR/>
	 * <UL>
	 * 		<LI>param argument. Prefixed with DAOConstants.PARAM_PREFIX (@)</LI>
	 * 		<LI>column argument. Prefixed with DAOConstants.COLUMN_PREFIX(#)</LI>
	 * 		<LI>Another function invocation (following same pattern)</LI>
	 * 		<LI>Constants</LI>
	 * </UL>
	 * <BR/> This method invokes <B>{@link #parse(String,String,boolean,boolean)}</B> to parse arguments. 
	 * @param funcExpr Function expression string.
	 * @param allowParams Param argument (@) are allowed only if this flag is true
	 * @param allowCols Column arguments (#) are allowed only if this flag is true
	 * @return Instance of this class representing specified function expression.
	 * @see DAOConstants
	 */
	public static FunctionInstance parse(String funcExpr, boolean allowParams, boolean allowCols)
	{
		if(funcExpr == null || funcExpr.trim().length() == 0)
			throw new NullPointerException("Function expression cannot be null or empty string.");

		funcExpr = funcExpr.trim();
		Matcher matcher = FUNC_PATTERN.matcher(funcExpr);

		if(!matcher.matches())
			throw new IllegalArgumentException("Invalid function expression encountered: " + funcExpr);

		String funcName = matcher.group(1);
		String paramStr = matcher.group(2);

		return parse(funcName, paramStr, allowParams, allowCols);
	}

	/**
	 * Method to parse function parameters.
	 * Arguments can be <BR/>
	 * <UL>
	 * 		<LI>param argument. Prefixed with DAOConstants.PARAM_PREFIX (@)</LI>
	 * 		<LI>column argument. Prefixed with DAOConstants.COLUMN_PREFIX(#)</LI>
	 * 		<LI>Another function invocation (following same pattern)</LI>
	 * 		<LI>Constants</LI>
	 * </UL>
	 * <BR/>
	 * A forward-slash can be used to escape following character. This will be useful to include
	 * space, single-quote('), double-quotes (") which are not allowed or ignored by default.
	 * 
	 * @param name Name of the function
	 * @param params Comma separated argument string
	 * @param allowParams Param argument (@) are allowed only if this flag is true
	 * @param allowCols Column arguments (#) are allowed only if this flag is true
	 * @return Instance of this class representing specified function name and argument expression.
	 * @see DAOConstants
	 */
	public static FunctionInstance parse(String name, String params, boolean allowParams, boolean allowCols)
	{
		params = params.trim();
		final String funcStr = name + "(" + params + ")";

		if(params.length() == 0)
		{
			//throw new IllegalArgumentException("No arguments specified: "+funcStr);
			return new FunctionInstance(funcStr, name, new Object[0]);
		}

		LinkedList<Object> lst = new LinkedList<Object>();
		char chArr[] = params.trim().toCharArray();
		StringBuilder builder = new StringBuilder();
		String token = null;
		int end = 0, st = 0, len = 0;
		boolean paramAdded = true;
		boolean func = false;

		for(int i = 0; i < chArr.length; i++)
		{
			paramAdded = false;

			//if(Character.isWhitespace(chArr[i]))
			//continue;

			if(func)
			{
				if(chArr[i] == ',')
				{
					func = false;
					continue;
				}

				throw new IllegalStateException("A function is expected to be followed by comma only: " + chArr[i] + "\nFunction: " + funcStr);
			}

			if(chArr[i] == '\\')
			{
				if(i == chArr.length - 1)
					continue;

				builder.append(chArr[i + 1]);
				i++;
				continue;
			}

			if(chArr[i] == '\'' || chArr[i] == '\"')
				throw new IllegalStateException("Single quote(') and double quotes(\") are not valid in function invocations: " + funcStr);

			if(chArr[i] == '(')
			{
				token = builder.toString();
				token = token.trim();

				if(token.length() == 0)
					throw new IllegalStateException("A ( encountered without function name(" + i + "): " + funcStr);

				end = getParamStringEnd(funcStr, chArr, i);
				st = i + 1;
				len = (end - 1) - st + 1;

				lst.add(parse(token, new String(chArr, st, len), allowParams, allowCols));
				i = end;
				builder.setLength(0);
				paramAdded = true;
				func = true;
				continue;
			}

			if(chArr[i] == ',')
			{
				token = builder.toString().trim();
				lst.add(parseParamToken(token, allowParams, allowCols, funcStr));

				builder.setLength(0);
				continue;
			}

			builder.append(chArr[i]);
		}

		if(!paramAdded)
		{
			token = builder.toString().trim();
			lst.add(parseParamToken(token, allowParams, allowCols, funcStr));
		}

		return new FunctionInstance(funcStr, name, lst.toArray());
	}

	private static Object parseParamToken(String token, boolean allowParams, boolean allowCols, String funcStr)
	{
		if(token.startsWith(DAOConstants.PARAM_PREFIX) && token.length() > 1)
		{
			if(!allowParams)
				throw new IllegalStateException("An unexpected parameter(" + DAOConstants.PARAM_PREFIX + ") argument encountered: " + funcStr);

			return new Param(token.substring(1));
		}
		else if(token.startsWith(DAOConstants.COLUMN_PREFIX) && token.length() > 1)
		{
			if(!allowCols)
				throw new IllegalStateException("An unexpected column(" + DAOConstants.COLUMN_PREFIX + ") argument encountered: " + funcStr);

			return new Column(token.substring(1));
		}
		else
			return token;
	}

	private static int getParamStringEnd(String funcStr, char ch[], int st)
	{
		int count = 0;

		for(int i = st + 1; i < ch.length; i++)
		{
			if(ch[i] == ')')
			{
				if(count == 0)
					return i;

				count--;
				continue;
			}

			if(ch[i] == '(')
				count++;
		}

		throw new IllegalStateException("There has been mismatch between ( and ) counts (" + st + "): " + funcStr);
	}

	/**
	 * This function can be used to get list of argument values that are specified for this
	 * function without invoking the function.
	 * 
	 * @param dataProvider Data provider to provide param/column values for this function arguments
	 * @return List of argument values
	 */
	public Object[] getParamValues(DataProvider dataProvider)
	{
		Object values[] = new Object[params.length];
		int len = params.length;

		for(int i = 0; i < len; i++)
		{
			if(params[i] instanceof Param)
				values[i] = dataProvider.getProperty(name, ((Param)params[i]).name);
			else if(params[i] instanceof Column)
				values[i] = dataProvider.getColumn(name, ((Column)params[i]).name);
			else if(params[i] instanceof FunctionInstance)
				values[i] = ((FunctionInstance)params[i]).invoke(dataProvider);
			else
				values[i] = params[i];
		}

		return values;
	}
	
	public int getParamCount()
	{
		return params.length;
	}

	/**
	 * This method will invoke this function after fetching the argument values using specified 
	 * dataProvider.
	 * @param dataProvider Data provider to provide values for arguments
	 * @return value returned by the function represented by this instance
	 */
	public Object invoke(DataProvider dataProvider)
	{
		Object paramVals[] = getParamValues(dataProvider);

		try
		{
			return FunctionManager.evaluate(name, paramVals);
		}catch(Exception ex)
		{
			logger.error("An error occured while invoking function: " + funcStr, ex);
			throw new IllegalStateException("An error occured while invoking function: " + funcStr, ex);
		}
	}

	public Class<?> getReturnType()
	{
		return FunctionManager.getReturnType(name);
	}

	/** 
	 * Converts the current function instance in the format<BR/>
	 * 	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 
	 *  <B>&lt;functionName&gt;(P: paramArg,C: columnArg,V: valueArg, F: func(...),..)</B> 
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		StringBuilder builder = new StringBuilder(name);
		builder.append("(");

		int len = params.length;

		for(int i = 0; i < len; i++)
		{
			if(params[i] instanceof Param)
				builder.append("P: ").append(params[i]);
			else if(params[i] instanceof Column)
				builder.append("C: ").append(params[i]);
			else if(params[i] instanceof FunctionInstance)
				builder.append("F: ").append(params[i]);
			else
				builder.append("V: ").append(params[i]);

			if(i < len - 1)
				builder.append(",");
		}

		builder.append(")");
		return builder.toString();
	}

	/**
	 * Checks if the function represented with this instance is registered with {@link FunctionManager} and 
	 * has required number of arguments. 
	 */
	public void validate()
	{
		if(!FunctionManager.isValidFunction(name, params.length))
			throw new IllegalStateException("Invalid function name/argument-count encountered: " + funcStr);

		for(Object o : params)
		{
			if(o instanceof FunctionInstance)
				((FunctionInstance)o).validate();
		}
	}
}
