package com.fw.persistence.freemarker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

public class ParamCollectorDirective implements TemplateDirectiveModel
{
	private List<Object> paramValues = new ArrayList<Object>(20);
	private Map<String, Object> context;
	
	@SuppressWarnings("rawtypes")
	@Override
	public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException,IOException
	{
		String name = params.get("name").toString();
		//Object value = env.getVariable(name);
		
		try
		{
			paramValues.add(PropertyUtils.getProperty(context, name));
		}catch(Exception e)
		{
			throw new IllegalStateException(e);
		}

		env.getOut().append("?");
	}
	
	public void reset(Map<String, Object> context)
	{
		this.context = context;
		paramValues.clear();
	}
	
	public List<Object> getParamValues()
	{
		return paramValues;
	}
}
