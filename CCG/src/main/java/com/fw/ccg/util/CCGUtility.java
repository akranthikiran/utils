package com.fw.ccg.util;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.fw.ccg.core.UnsupportedDataTypeException;

/**
 * © Copyright 2006 IBM Corporation
 * <BR><BR>
 * Set of utility functions dealing with string parsing, 
 * reflections and primitive/wrapper conversions.
 * <BR>
 * @author A. Kranthi Kiran
 */
public class CCGUtility
{
	private CCGUtility()
	{}

	public static boolean isEqual(Object obj1, Object obj2)
	{
		if(obj1 == null && obj2 == null)
			return true;

		if(obj1 == null || obj2 == null)
			return false;

		if(!obj1.getClass().equals(obj2.getClass()))
			return false;

		if(obj1.getClass().isArray())
		{
			if(obj1.getClass().getComponentType().isPrimitive())
			{
				Class<?> compType = obj1.getClass().getComponentType();

				if(byte.class.equals(compType))
					return Arrays.equals((byte[])obj1, (byte[])obj2);

				if(boolean.class.equals(compType))
					return Arrays.equals((boolean[])obj1, (boolean[])obj2);

				if(char.class.equals(compType))
					return Arrays.equals((char[])obj1, (char[])obj2);

				if(int.class.equals(compType))
					return Arrays.equals((int[])obj1, (int[])obj2);

				if(long.class.equals(compType))
					return Arrays.equals((long[])obj1, (long[])obj2);

				if(double.class.equals(compType))
					return Arrays.equals((double[])obj1, (double[])obj2);

				if(float.class.equals(compType))
					return Arrays.equals((float[])obj1, (float[])obj2);

				throw new IllegalStateException("Unknown primitive type encountered: " + compType.getName());
			}

			Object objArr1[] = (Object[])obj1;
			Object objArr2[] = (Object[])obj2;

			if(objArr1.length != objArr2.length)
				return false;

			for(int i = 0; i < objArr1.length; i++)
				if(!isEqual(objArr1[i], objArr2[i]))
					return false;

			return true;
		}

		return obj1.equals(obj2);
	}

	public static Object[] convert(Object from[], Object to[])
	{
		if(from == null)
			return null;

		Object obj[] = (Object[])Array.newInstance(to.getClass().getComponentType(), from.length);

		for(int i = 0; i < from.length; i++)
			obj[i] = from[i];

		return obj;
	}

	public static Object[] convertArray(Object from, Class<?> type)
	{
		if(from == null)
			return null;

		if(!from.getClass().isArray())
			throw new IllegalArgumentException("From should be an array");

		Object obj[] = (Object[])Array.newInstance(type.getComponentType(), Array.getLength(from));

		for(int i = 0; i < obj.length; i++)
			obj[i] = Array.get(from, i);

		return obj;
	}

	/**
	 * If "type" represents wrapper class, then its mapping primitive class will be
	 * returned. Otherwise returns null.
	 * @param type Wrapper type.
	 * @return Mapping primitive type corresponding to specified wrapper type.
	 */
	public static Class<?> getPrimitiveClass(Class<?> type)
	{
		if(type == null)
			return null;

		if(type.equals(Boolean.class))
			return Boolean.TYPE;
		else if(type.equals(Character.class))
			return Character.TYPE;
		else if(type.equals(Byte.class))
			return Byte.TYPE;
		else if(type.equals(Short.class))
			return Short.TYPE;
		else if(type.equals(Integer.class))
			return Integer.TYPE;
		else if(type.equals(Long.class))
			return Long.TYPE;
		else if(type.equals(Float.class))
			return Float.TYPE;
		else if(type.equals(Double.class))
			return Double.TYPE;
		else if(type.equals(Void.class))
			return Void.TYPE;
		return null;
	}

	/**
	 * Returns wrapper type corresponding to the primitive type indicated by "type". If 
	 * "type" does not represent primitive class, null is returned.
	 * @param type Primitive class.
	 * @return Mapped wrapper class.  
	 */
	public static Class<?> getWrapperClass(Class<?> type)
	{
		if(type == null || !type.isPrimitive())
			return null;

		if(type.equals(Boolean.TYPE))
			return Boolean.class;
		else if(type.equals(Character.TYPE))
			return Character.class;
		else if(type.equals(Byte.TYPE))
			return Byte.class;
		else if(type.equals(Short.TYPE))
			return Short.class;
		else if(type.equals(Integer.TYPE))
			return Integer.class;
		else if(type.equals(Long.TYPE))
			return Long.class;
		else if(type.equals(Float.TYPE))
			return Float.class;
		else if(type.equals(Double.TYPE))
			return Double.class;
		else if(type.equals(Void.TYPE))
			return void.class;
		return null;
	}

	/**
	 * A default value for specified type as per java standards. If specified type is
	 * primitive a corresponding wrapper object is returned with its default value.
	 * @param type Primitive type
	 * @return default value for specified type. 
	 */
	public static Object getDefaultPrimitiveValue(Class<?> type)
	{
		if(type == null)
			return null;

		if(type.equals(Boolean.TYPE) || type.equals(Boolean.class))
			return new Boolean(false);
		else if(type.equals(Character.TYPE) || type.equals(Character.class))
			return new Character('\0');
		else if(type.equals(Byte.TYPE) || type.equals(Byte.class))
			return new Byte((byte)0);
		else if(type.equals(Short.TYPE) || type.equals(Short.class))
			return new Short((short)0);
		else if(type.equals(Integer.TYPE) || type.equals(Integer.class))
			return new Integer(0);
		else if(type.equals(Long.TYPE) || type.equals(Long.class))
			return new Long(0);
		else if(type.equals(Float.TYPE) || type.equals(Float.class))
			return new Float(0);
		else if(type.equals(Double.TYPE) || type.equals(Double.class))
			return new Double(0);
		return null;
	}

	/**
	 * Behaves like Class.forName(). But this function also support java primitives like
	 * int, float, etc (int.class and float.class respectively).
	 * @param clsType Name of the class or string version of primitive type.
	 * @return Class represented by clsType
	 * @throws InvalidValueException If sepecified class can not be loaded.
	 */
	public static Class<?> getClass(String clsType)
	{
		if(clsType == null || clsType.trim().length() == 0)
			throw new NullPointerException("Class name cannot be null or empty string.");

		clsType = clsType.trim();

		if(clsType.endsWith("[]"))
		{
			int dimCount = 0;
			String clsCompType = clsType;

			while(clsCompType.endsWith("[]"))
			{

				if(clsCompType.length() == 2)
					throw new IllegalArgumentException("Invalid class name specified: " + clsType);

				clsCompType = clsCompType.substring(0, clsCompType.length() - 2).trim();
				dimCount++;
			}

			Class<?> compTypeCls = getClass(clsCompType);
			int dimArr[] = new int[dimCount];
			Object inst = Array.newInstance(compTypeCls, dimArr);
			return inst.getClass();
		}

		if(clsType.indexOf(".") < 0)
		{
			if("byte".equals(clsType))
				return byte.class;

			if("boolean".equals(clsType))
				return boolean.class;

			if("char".equals(clsType))
				return char.class;

			if("short".equals(clsType))
				return short.class;

			if("int".equals(clsType))
				return int.class;

			if("long".equals(clsType))
				return long.class;

			if("float".equals(clsType))
				return float.class;

			if("double".equals(clsType))
				return double.class;

			if("void".equals(clsType))
				return void.class;

			try
			{
				return Class.forName("java.lang." + clsType);
			}catch(Exception ex)
			{}
		}

		try
		{
			return Class.forName(clsType);
		}catch(Exception ex)
		{
			throw new InvalidValueException("Error in loading class with name: " + clsType, ex);
		}
	}

	public static boolean isWrapperClass(Class<?> type)
	{
		if(Boolean.class.equals(type))
			return true;
		if(Character.class.equals(type))
			return true;
		if(Byte.class.equals(type))
			return true;
		if(Short.class.equals(type))
			return true;
		if(Integer.class.equals(type))
			return true;
		if(Float.class.equals(type))
			return true;
		if(Long.class.equals(type))
			return true;
		if(Double.class.equals(type))
			return true;

		return false;
	}

	/**
	 * Parses value into specified type. Type can be any primitive type (or thier wrapper) or
	 * java.util.Date.
	 * <BR><BR>
	 * For Date if format is not specified, MM/dd/yyyy is used as format.
	 * <BR>
	 * <B>Note:</B>Currently format is used only for date objects.
	 * 
	 * @param value Value to be parsed.
	 * @param type Primitive/Wrapper type or java.util.Date type.
	 * @param format Currently used only to format java.util.Date.
	 * @return
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static Object toObject(String value, Class<?> type, String format)
	{
		if(type == null)
			throw new NullPointerException("Type can not be null.");

		if(value == null)
			throw new NullPointerException("Value can not be null.");

		value = value.trim();

		if(type.equals(String.class))
			return value;

		if(type.isEnum())
		{
			return Enum.valueOf((Class)type, value);
		}

		if(Class.class.equals(type))
		{
			try
			{
				Class<?> cls = Class.forName(value.trim());
				return cls;
			}catch(Exception ex)
			{
				throw new IllegalArgumentException("Invalid class name encountered: " + value, ex);
			}
		}

		if(type.equals(Character.TYPE) || type.equals(Character.class))
			return new Character(value.charAt(0));

		if(type.equals(Double.TYPE) || type.equals(Double.class))
			return Double.valueOf(value);

		if(type.equals(Float.TYPE) || type.equals(Float.class))
			return Float.valueOf(value);

		if(type.equals(Long.TYPE) || type.equals(Long.class))
			return Long.valueOf(value);

		if(type.equals(Integer.TYPE) || type.equals(Integer.class))
			return Integer.valueOf(value);

		if(type.equals(Short.TYPE) || type.equals(Short.class))
			return Short.valueOf(value);

		if(type.equals(Boolean.TYPE) || type.equals(Boolean.class))
			return Boolean.valueOf(value);

		if(type.equals(Byte.TYPE) || type.equals(Byte.class))
			return Byte.valueOf(value);

		if(type.equals(java.util.Date.class))
		{
			SimpleDateFormat frm = null;
			format = (format == null)? "MM/dd/yyyy": format;
			try
			{
				frm = new SimpleDateFormat(format);
				return frm.parse(value);
			}catch(Exception e)
			{
				throw new InvalidValueException("Invalid date value/format (" + format + ") encountered: " + value);
			}
		}

		try
		{
			Constructor<?> construct = type.getConstructor(String.class);

			try
			{
				if(construct != null)
					return construct.newInstance(value);
			}catch(Exception ex)
			{
				throw new IllegalStateException("An error occured while invoking constructor: <init>(String) on type: " + type.getName(), ex);
			}
		}catch(NoSuchMethodException ex)
		{}

		throw new UnsupportedDataTypeException("Unsupported object type encountered: " + type.getName());
	}

	/**
	 * Checks if the object/value of "from" type is assignable to "to" type reference. Note,
	 * as per this method primitive versions are assignable from/to thier corresponding wrapper
	 * types.
	 * @param from
	 * @param to
	 * @return If "from" type is assignable to "to" type.
	 */
	public static boolean isAssignable(Class<?> from, Class<?> to)
	{
		if(from == null)
			throw new NullPointerException("From type can not be null.");

		if(to == null)
			throw new NullPointerException("To type can not be null.");

		if(to.isAssignableFrom(from))
			return true;

		if(from.isPrimitive())
			from = getWrapperClass(from);

		if(to.isPrimitive())
			to = getWrapperClass(to);

		return from.equals(to);
	}

	/**
	 * Equivalent to calling invokeGetProperty(bean,property,arg,false)
	 * @param bean
	 * @param property
	 * @param arg
	 * @return
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	public static Object invokeGetProperty(Object bean, String property, Object arg[][]) throws InvocationTargetException, IllegalAccessException
	{
		return invokeGetProperty(bean, property, arg, false);
	}

	/**
	 * <P>
	 * This method invokes specified nested property on specified bean. The property is the 
	 * sequence of standard getters (starting with get or is) to be called on the bean passing 
	 * arg elements at each step.
	 * </P>
	 * <P>
	 * For example, if property value is employee.name, then getEmployee().getName() will be
	 * called on bean. 
	 * </P>
	 * <P>
	 * Note while searching for properties, both "get" and "is" versions  will be searched, 
	 * if both are present or if mutiple methods are present with required criteria or if
	 * no method exists with required criteria then appropriate exception will be thrown.
	 * </P>
	 * @param bean Bean on which nested property needs to be invoked.
	 * @param property Nested property which needs to be invoked.
	 * @param arg Argument lists that needs to passed to methods at different levels.
	 * 				argTypes[i] indicates argument values at level "i".
	 * @param stat Flag indicating whether static method should be considering for nested
	 * 				properties.  
	 * @return Result of invocation of specified property.
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	public static Object invokeGetProperty(Object bean, String property, Object arg[][], boolean stat) throws InvocationTargetException, IllegalAccessException
	{
		if(bean == null)
			throw new NullPointerException("Bean can not be null.");

		if(property == null)
			throw new NullPointerException("Nested property string can not be null.");
		Class<?> cls = null;
		StringTokenizer st = new StringTokenizer(property, ".");
		int idx = 0;
		String prop = null;
		Method met = null;
		Method tmpMet[] = null;
		StringBuffer process = new StringBuffer();
		Object params[] = null;
		Class<?> types[] = null;
		Object curArgs[] = null;

		while(st.hasMoreTokens())
		{
			cls = bean.getClass();
			prop = st.nextToken();
			prop = (Character.toUpperCase(prop.charAt(0))) + prop.substring(1, prop.length());

			if(arg != null && idx < arg.length)
				curArgs = arg[idx];
			else
				curArgs = null;

			if(curArgs != null && curArgs.length > 0)
				tmpMet = getMethodsIn(cls, new String[]{"get" + prop, "is" + prop}, -1, curArgs, stat);
			else
				tmpMet = getMethodsIn(cls, new String[]{"get" + prop, "is" + prop}, 0, null, stat);

			if(tmpMet == null)
				throw new MethodSearchException(prop, true, curArgs, idx, cls);

			if(tmpMet.length > 1)
				throw new MethodSearchException(prop, false, curArgs, idx, cls);

			met = tmpMet[0];
			if(process.length() > 0)
				process.append(".");
			process.append(toString(met));
			types = met.getParameterTypes();
			if(types == null || types.length == 0)
				params = null;
			else
				params = curArgs;

			bean = met.invoke(bean, params);

			if(bean == null && st.hasMoreTokens())
				throw new NullPointerException("null encountered while processing property tree after: " + process);

			idx++;
		}

		return bean;
	}

	/**
	 * Equivalent to calling buildMethodSequence(rootType,property,argTypes,false)
	 * @param rootType
	 * @param property
	 * @param argTypes
	 * @return
	 */
	public static MethodSequence buildMethodSequence(Class<?> rootType, String property, Class<?> argTypes[][])
	{
		return buildMethodSequence(rootType, property, argTypes, false);
	}

	/**
	 * This method is useful in compiling a method sequence into a MethodSequence and use
	 * that sequence for invoking specified nested property. As the parsing and validation
	 * is done only once (while building MethodSequence), performance will get improved.
	 * <BR>
	 * Note while searching for properties, both "get" and "is" versions  will be searched, 
	 * if both are present or if mutiple methods are present with required criteria or if
	 * no method exists with required criteria then appropriate exception will be thrown.
	 * <BR>
	 * @param rootType Type of beans on which nested property is expected to be invoked. 
	 * @param property Nested property for which method sequence neeeds to be built.
	 * @param argTypes Type of arguments for nested properties at different levels. 
	 * 				argTypes[i] indicates argument types at level "i".
	 * @param stat Flag indicating whether static methods should be considered for nested
	 * 			properties.
	 * @return MethodSequence representing specified nested property.
	 */
	public static MethodSequence buildMethodSequence(Class<?> rootType, String property, Class<?> argTypes[][], boolean stat)
	{
		if(rootType == null)
			throw new IllegalArgumentException("Root type can not be null.");

		if(property == null || property.trim().length() == 0)
			throw new IllegalArgumentException("Property string can not be null or empty string.");

		Class<?> cls = rootType;
		StringTokenizer st = new StringTokenizer(property, ".");
		int idx = 0;
		String prop = null;
		Method met = null;
		Method tmpMet[] = null;
		ArrayList<Method> methods = new ArrayList<Method>();
		StringBuffer process = new StringBuffer();
		Class<?> curArgTypes[] = null;

		while(st.hasMoreTokens())
		{
			prop = st.nextToken();
			prop = (Character.toUpperCase(prop.charAt(0))) + prop.substring(1, prop.length());

			if(argTypes != null && idx < argTypes.length)
				curArgTypes = argTypes[idx];
			else
				curArgTypes = null;

			if(curArgTypes != null && curArgTypes.length > 0)
				tmpMet = getMethodsIn(cls, new String[]{"get" + prop, "is" + prop}, -1, curArgTypes, stat);
			else
				tmpMet = getMethodsIn(cls, new String[]{"get" + prop, "is" + prop}, 0, null, stat);

			if(tmpMet == null)
				throw new MethodSearchException(prop, true, curArgTypes, idx, cls);

			if(tmpMet.length > 1)
				throw new MethodSearchException(prop, false, curArgTypes, idx, cls);

			met = tmpMet[0];
			if(process.length() > 0)
				process.append(".");
			process.append(toString(met));
			cls = met.getReturnType();

			if(void.class.equals(cls) && st.hasMoreTokens())
				throw new MethodSearchException("A void method encountered while processing property tree after: " + process);

			methods.add(met);
			idx++;
		}

		MethodSequence seq = new MethodSequence();
		seq.initalize(methods.toArray(new Method[0]));
		return seq;

	}

	/**
	 * Returns unsigned value of specified byte value.
	 * @param b Input value.
	 * @return unsigned input value.
	 */
	public static int toUnsignedByte(byte b)
	{
		if(b > 0)
			return b;
		return b & 0xFF;
	}

	/**
	 * Returns unsigned value of specified short value.
	 * @param s Input value.
	 * @return unsigned input value.
	 */
	public static int toUnsignedShort(short s)
	{
		if(s >= 0)
			return s;
		return s & 0xFFFF;
	}

	/**
	 * Returns unsigned value of specified int value.
	 * @param i Input value.
	 * @return unsigned input value.
	 */
	public static long toUnsignedInt(int i)
	{
		if(i >= 0)
			return i;
		return (i & 0xFFFFFFFFL);
	}

	/**
	 * Fetches all the methods in cls having name as one of specified methodNames and 
	 * which can accept args as arguments. If both argCount<=0 and args==null is true, 
	 * then the method which accepts no arguments will be returned.
	 * <BR><BR> 
	 * If args is specified, then argCount will not be considered. If args is null, then all 
	 * the methods with specified name and which have argCount number of arguments will be 
	 * returned.
	 * <BR><BR>
	 * @param cls Class in which method search needs to be performed.
	 * @param methodNames Method names to be searched for.
	 * @param argCount Number of arguments the searching methods need to be accept.
	 * @param args Argument the searching methods need to accept. args[i] indicates arguemt
	 * 				values to be passed at level "i".
	 * @param stat Specified whether static methods should be considered during method search.
	 * @return null if no method found with specified criteria otherwise array of methods 
	 * matching specified criteria.
	 */
	public static Method[] getMethodsIn(Class<?> cls, String methodNames[], int argCount, Object args[], boolean stat)
	{
		if(methodNames == null || methodNames.length == 0)
			throw new NullPointerException("Method name cannot be null or empty.");

		if(cls == null)
			throw new NullPointerException("Source class can not be null.");

		Class<?> argTypes[] = null;
		if(args != null)
		{
			argTypes = new Class<?>[args.length];
			for(int i = 0; i < args.length; i++)
			{
				if(args[i] == null)
					continue;
				argTypes[i] = args[i].getClass();
			}
		}

		return getMethodsIn(cls, methodNames, argCount, argTypes, stat);
	}

	/**
	 * Fetches all the methods in cls having name as one of specified methodNames and 
	 * which can accept specified argument types. If both argCount<=0 and args==null is true, 
	 * then the method which accepts no arguments will be returned.
	 * <BR><BR> 
	 * If args is specified, then argCount will not be considered. If args is null, then all 
	 * the methods with specified name and which have argCount number of arguments will be 
	 * returned. Note, if argument type is null for a single argument at some level 
	 * (arg[i][j]==null), then that null indicates presence of argument (and it can be
	 * of any type). 
	 * <BR><BR>
	 * @param cls Class on which method search needs to be performed. 
	 * @param methodNames Names of the methods to be searched.
	 * @param argCount Number of arguments the searching methods needs to be accepted.
	 * @param argTypes Types the searching methods need to accept.
	 * @param stat Indicates whether the static methods need to be considered.
	 * @return Method array matching specified criteria. If no method matches, null 
	 * is returned.
	 */
	public static Method[] getMethodsIn(Class<?> cls, String methodNames[], int argCount, Class<?> argTypes[], boolean stat)
	{
		if(methodNames == null || methodNames.length == 0)
			throw new NullPointerException("Method name can not be null or empty.");

		if(cls == null)
			throw new NullPointerException("Source class can not be null.");

		if(argTypes != null)
			argCount = argTypes.length;

		Method met[] = cls.getMethods();
		Class<?> typ[] = null;
		List<Method> res = new ArrayList<Method>();
		int j = 0;
		NEXT_METHOD: for(int i = 0; i < met.length; i++)
		{
			if(!stat && Modifier.isStatic(met[i].getModifiers()))
				continue;

			typ = met[i].getParameterTypes();

			for(j = 0; j < methodNames.length; j++)
			{
				if(methodNames[j].equals(met[i].getName()))
					break;
			}

			//if method name doesnt match with specified names 
			if(j == methodNames.length)
				continue;

			if(typ.length == 0 && argCount == 0)
			{

				//there can be only one method in a class with the given and having
				//zero arguments
				return new Method[]{met[i]};
			}

			if(argCount >= 0 && typ.length != argCount)
				continue;

			//there can be multiple methods which can accept specified arguments
			if(argTypes != null)
			{
				for(j = 0; j < typ.length; j++)
				{
					if(argTypes[j] == null)
						continue;

					//if argument is not null then only consider its type 
					if(isAssignable(typ[j], argTypes[j]))
						continue;

					continue NEXT_METHOD;
				}
			}

			res.add(met[i]);
		}

		if(res.size() == 0)
			return null;
		return res.toArray(new Method[0]);
	}

	/**
	 * String represntation of the specified method. For example string conversion of 
	 * this method will be 
	 * <BR>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;toString(java.lang.reflect.Method) 
	 * @param met Method whose string representation is needed.
	 * @return String representation of met.
	 */
	public static String toString(Method met)
	{
		if(met == null)
			return null;
		StringBuffer res = new StringBuffer(met.getName() + "(");
		Class<?> arg[] = met.getParameterTypes();
		if(arg != null && arg.length > 0)
		{
			for(int i = 0; i < arg.length; i++)
			{
				res.append(arg[i].getName());
				if(i < arg.length - 1)
					res.append(",");
			}
		}
		return res.append(")").toString();
	}
	
	public static Map<String, Object> buildMap(Object... contextEntries)
	{
		Map<String, Object> context = new HashMap<String, Object>();
		
		if(contextEntries != null && contextEntries.length > 0)
		{
			if((contextEntries.length % 2) != 0)
			{
				throw new IllegalArgumentException("Invalid number of map entries specified: " + contextEntries.length);
			}
			
			for(int i = 0; i < contextEntries.length; i += 2)
			{
				context.put(contextEntries[i].toString(), contextEntries[i + 1]);
			}
		}

		return context;
	}

}
