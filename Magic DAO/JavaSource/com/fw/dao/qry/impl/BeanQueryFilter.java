package com.fw.dao.qry.impl;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fw.ccg.util.BeanUtil;

public class BeanQueryFilter extends MapQueryFilter
{
	private static Logger logger = LogManager.getLogger(BeanQueryFilter.class);

	private Map<String, Method> nameToGetter;
	private Map<String, Object> propToVal = new HashMap<String, Object>();
	private Object bean;
	protected Class<?> beanType;

	private Map<String, Object> extraProperties = new HashMap<String, Object>();

	public BeanQueryFilter()
	{}

	public BeanQueryFilter(Object bean)
	{
		setBean(bean);
	}

	protected void loadProperties(Object bean)
	{
		if(bean == null)
			throw new NullPointerException("Bean can not be null.");

		beanType = bean.getClass();
		nameToGetter = BeanUtil.getGetterMethods(bean.getClass());
	}

	protected void clear()
	{
		propToVal.clear();

		if(nameToGetter != null)
		{
			nameToGetter.clear();
		}
	}

	public BeanQueryFilter setBean(Object bean)
	{
		if(bean == null)
		{
			throw new NullPointerException("Bean can not be null.");
		}

		beanType = bean.getClass();
		nameToGetter = BeanUtil.getGetterMethods(beanType);
		propToVal.clear();
		extraProperties.clear();

		this.bean = bean;
		return this;
	}

	protected Method getGetter(String name)
	{
		return nameToGetter.get(name);
	}

	@Override
	protected Object getValue(String key)
	{
		if(this.bean == null)
		{
			if(extraProperties.containsKey(key))
			{
				return extraProperties.get(key);
			}

			throw new IllegalStateException("No query bean is configured.");
		}

		if("this".equals(key))
		{
			return bean;
		}

		if(propToVal.containsKey(key))
			return propToVal.get(key);

		Method getter = nameToGetter.get(key);

		if(getter == null)
			return extraProperties.get(key);

		Object value = null;

		try
		{
			if(!getter.isAccessible())
			{
				getter.setAccessible(true);
			}

			value = getter.invoke(bean);
			propToVal.put(key, value);
		}catch(Exception ex)
		{
			logger.error("An error occured while fetching property: " + key, ex);
		}

		return value;
	}

	public void addExtraProperty(String name, Object value)
	{
		extraProperties.put(name, value);
	}
	

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder(super.toString());
		builder.append("[Bean: " + bean);
		builder.append(", Extra-Properties: " + extraProperties);
		builder.append("]");
		
		return builder.toString();
	}
	
}
