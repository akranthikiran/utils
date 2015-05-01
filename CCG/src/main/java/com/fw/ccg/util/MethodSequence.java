package com.fw.ccg.util;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * © Copyright 2006 IBM Corporation
 * <BR><BR>
 * This class represents a nested property that can be invoked using invoke(). The instance
 * of this class can be obtained by buildMethodSequence() method of CCGUtility.
 * <BR>
 * @author A. Kranthi Kiran
 */
public class MethodSequence implements Serializable
{
	private static final long serialVersionUID=1L;
	private Method methods[];
		/**
		 * Builds empty method sequence.
		 */
		MethodSequence()
		{
		}
		
		/**
		 * Adds list of sequence methods that can be invoked. 
		 * @param met Array of methods that consititutes this method sequence.
		 */
		void initalize(Method met[])
		{
				if(met==null || met.length==0)
					throw new NullPointerException("Methods list cannot be null or empty.");
			methods=met;
		}
		
		/**
		 * Say this method sequence constitiutes of three methods and arg is not null and
		 * sufficiently long. Then met[0] will be called on bean and on the result met[1] will be
		 * called and on the result of the second call met[3] will be caleed passing
		 * arg[0], arg[1] and arg[2] as parameters during each call. And the final result 
		 * will be returned by this method.
		 * @param bean Bean on which method sequence needs to be invoked.
		 * @param arg Arguments to be passed at different levels.
		 * @return Result of calling sequence of methods represented by this object.
		 * @throws IllegalArgumentException
		 * @throws IllegalAccessException
		 * @throws InvocationTargetException
		 */
		public Object invoke(Object bean,Object arg[][]) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
		{
				if(methods==null)
					throw new IllegalStateException("MethodSequence is not initalized.");
				
				for(int i=0;i<methods.length;i++)
				{
						if(i<arg.length)
							bean=methods[i].invoke(bean,arg[i]);
						else
							bean=methods[i].invoke(bean,(Object[])null);
						
						if(bean==null && i<methods.length-1)
						{
							String strMet=CCGUtility.toString(methods[i]);
							throw new NullPointerException("Null encountered during nested property invokation.\nMethod \""+strMet+"\" of class \""+methods[i].getDeclaringClass().getName()+"\" returned null.");
						}
				}
			return bean;
		}
		
		/**
		 * @return The return type of the nested property represnted by this method sequence.
		 */
		public Class getReturnType()
		{
				if(methods==null || methods.length==0)
					return null;
			return methods[methods.length-1].getReturnType();
		}
		
		/**
		 * @return Number of levels in the nested property represented by this method sequence. 
		 */
		public int getLevelCount()
		{
				if(methods==null)
					return 0;
			return methods.length;
		}
		
		/**
		 * The string version returned by this method will be signatures of the methods to
		 * be invoked separated by dot(.).
		 * @see java.lang.Object#toString()
		 */
		public String toString()
		{
				if(methods==null)
					return "";
				
			StringBuffer buff=new StringBuffer();
				for(int i=0;i<methods.length;i++)
				{
					buff.append(CCGUtility.toString(methods[i]));
						if(i<methods.length-1)
							buff.append(".");
				}
			return buff.toString();
		}
}
