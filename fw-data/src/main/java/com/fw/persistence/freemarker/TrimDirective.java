package com.fw.persistence.freemarker;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

public class TrimDirective implements TemplateDirectiveModel
{
	@SuppressWarnings("rawtypes")
	@Override
	public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException,IOException
	{
		StringWriter writer = new StringWriter();
		body.render(writer);
		
		env.getOut().append(writer.toString().trim());
	}
}
