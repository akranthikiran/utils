package com.fw.dao.qry;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;

/**
 * This class acts as the registry for all the functions that can be used in the queries. 
 * A class with query functions can be registered with using {@link #register(Class)} method. 
 */
public class FunctionManager
{
	private static class FunctionDetail
	{
		private Method method;
		private int minArgCount;
		private Class<?> paramTypes[];

		public FunctionDetail(Method m, int minArgCount, Class<?> paramTypes[])
		{
			this.method = m;
			this.minArgCount = minArgCount;
			this.paramTypes = paramTypes;
		}

		public Class<?> getReturnType()
		{
			return method.getReturnType();
		}

		public int maxArgCount()
		{
			if(paramTypes == null)
				return 0;

			return paramTypes.length;
		}

		public Object invoke(Object params[])
		{
			if(params == null && minArgCount != 0)
				throw new IllegalArgumentException("Insufficient number of arguments encountered: 0\nRequired: " + minArgCount);

			if(params != null && params.length < minArgCount)
				throw new IllegalArgumentException("Insufficient number of arguments encountered: 0\nRequired: " + minArgCount);

			if(paramTypes == null || paramTypes.length == 0)
				return null;

			Object res[] = new Object[paramTypes.length];

			if(params != null && params.length > 0)
			{
				for(int i = 0; i < params.length; i++)
				{
					if(params[i] == null)
					{
						res[i] = null;
						continue;
					}

					try
					{
						res[i] = QueryUtil.convert(params[i], paramTypes[i]);
						continue;
					}catch(Exception ex)
					{}

					if(!paramTypes[i].isAssignableFrom(params[i].getClass()))
						throw new IllegalArgumentException("Invalid parameter type encountered. Argument# " + i + "\n Argument Type: " + params[i].getClass().getName() + "\n Argument Value: " + params[i] + "\n Expected Type: " + paramTypes[i].getName());

					res[i] = params[i];
				}
			}

			try
			{
				return method.invoke(null, (Object[])res);
			}catch(Exception ex)
			{
				throw new IllegalStateException("An error occured while invoking query function: " + method.getName() + "\nWith Params: " + Arrays.toString(res), ex);
			}
		}
	}

	private static HashMap<String, FunctionDetail> nameToDet = new HashMap<String, FunctionDetail>();

	static
	{
		register(DefaultQueryFunctions.class);
	}

	/**
	 * Loads all the Query Functions available in the specified class. For a method to become query function
	 * it should follow below criteria
	 * <UL>
	 * 		<LI>Should be public</LI>
	 * 		<LI>Should be static</LI>
	 * 		<LI>Should have at least one argument</LI>
	 * 		<LI>Should be annotated with  {@link QueryFunction} </LI>
	 * </UL>
	 * <BR/> If the functions are overloaded the result is ambiguous. And if already a function is present with
	 * the function name being loaded, then old one will be overridden. 
	 * 
	 * @param cls
	 */
	public static void register(Class<?> cls)
	{
		Method metArr[] = cls.getMethods();
		int mod = 0;
		int minArgCont = 0;

		FunctionDetail funDet = null;
		Class<?> paramTypes[] = null;
		int count = 0;

		for(Method m : metArr)
		{
			mod = m.getModifiers();

			if(!Modifier.isPublic(mod) || !Modifier.isStatic(mod))
				continue;

			QueryFunction func = m.getAnnotation(QueryFunction.class);

			if(func == null)
				continue;

			minArgCont = func.minArgCount();
			paramTypes = m.getParameterTypes();

			if(paramTypes == null || paramTypes.length == 0)
				throw new IllegalStateException("A non-parameter method can not be declared as QueryFunction: " + m.getName());

			if(minArgCont < 0 || minArgCont > paramTypes.length)
				minArgCont = paramTypes.length;

			funDet = new FunctionDetail(m, minArgCont, paramTypes);
			nameToDet.put(m.getName(), funDet);
			count++;
		}

		if(count == 0)
			throw new IllegalStateException("No query methods found in specified class: " + cls.getName());
	}

	/**
	 * Checks if a query function is registered with specified name
	 * @param name Name of the query function
	 * @return true, if a Query function is registered with specified name
	 */
	public static boolean isValidFunction(String name)
	{
		return nameToDet.containsKey(name);
	}

	/**
	 * Checks if a query function is registered with specified name and if the
	 * provided Argument count is sufficient for method invocation.
	 * @param name Query function name
	 * @param argCount Number of arguments
	 * @return true, if a Query function is registered with specified name and specified argument count is sufficient
	 */
	public static boolean isValidFunction(String name, int argCount)
	{
		FunctionDetail det = nameToDet.get(name);

		if(det == null)
			return false;

		if(argCount < det.minArgCount || argCount > det.maxArgCount())
			return false;

		return true;
	}

	/**
	 * Executes function with specified name and with specified parameters.
	 * @param func Function name
	 * @param param List of parameters for method invocation
	 * @return Return value of the function
	 */
	public static Object evaluate(String func, Object param[])
	{
		if(func == null)
			return null;

		FunctionDetail det = nameToDet.get(func);

		if(det == null)
			throw new IllegalArgumentException("Invalid function name encountered:" + func);

		return det.invoke(param);
	}

	public static Class<?> getReturnType(String func)
	{
		if(func == null)
			return null;

		FunctionDetail det = nameToDet.get(func);

		if(det == null)
			throw new IllegalArgumentException("Invalid function name encountered:" + func);

		return det.getReturnType();
	}
}
