package com.fw.persistence.rdbms;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fw.ccg.core.ValidateException;
import com.fw.ccg.core.Validateable;
import com.fw.ccg.util.CCGUtility;
import com.fw.persistence.freemarker.ParamCollectorDirective;
import com.fw.persistence.freemarker.TrimDirective;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class RdbmsConfiguration implements Validateable
{
	public static final String CHECK_SEQUENCE_QUERY = "checkSequenceTemplate";
	public static final String CREATE_SEQUENCE_QUERY = "createSequenceTemplate";
	public static final String CREATE_QUERY = "createTableTemplate";
	public static final String CREATE_INDEX = "createIndexTemplate";
	public static final String SAVE_QUERY = "saveTemplate";
	public static final String UPDATE_QUERY = "updateTemplate";
	public static final String SAVE_UPDATE_QUERY = "saveUpdateTemplate";
	public static final String DELETE_QUERY = "deleteTemplate";
	public static final String FINDER_QUERY = "finderTemplate";
	public static final String COUNT_QUERY = "countTemplate";
	public static final String CHILDREN_EXISTENCE_QUERY = "childrenExistenceTemplate";
	public static final String FETCH_CHILDREN_IDS_QUERY = "fetchChildrenIdsTemplate";
	public static final String DROP_QUERY = "dropTableTemplate";

	public static final String MANDATORY_QUERIES[] = {
		CREATE_QUERY, CREATE_INDEX,
		
		SAVE_QUERY, UPDATE_QUERY, DELETE_QUERY, FINDER_QUERY, COUNT_QUERY, 
		
		CHILDREN_EXISTENCE_QUERY, FETCH_CHILDREN_IDS_QUERY,
		
		DROP_QUERY
	};

	private Map<String, String> queryMap = new HashMap<>();
	private Map<String, Template> templateMap = new HashMap<>();
	
	private Configuration configuration = new Configuration();
	
	private ParamCollectorDirective paramCollectorDirective = new ParamCollectorDirective();
	
	public RdbmsConfiguration()
	{
		configuration.setSharedVariable("trim", new TrimDirective());
		configuration.setSharedVariable("param", paramCollectorDirective);
	}
	
	public void addTemplate(String name, String template)
	{
		queryMap.put(name, template);
	}
	
	private boolean isEmptyQuery(String name)
	{
		String query = queryMap.get(name);
		return (query == null || query.trim().length() == 0);
	}

	@Override
	public void validate() throws ValidateException
	{
		for(String query: MANDATORY_QUERIES)
		{
			if(isEmptyQuery(query))
			{
				throw new ValidateException("'" + query + "' template can not be null or empty");
			}
		}
	}
	
	/**
	 * Checks if a query is configured with specified name
	 * @param name
	 * @return
	 */
	public boolean hasQuery(String name)
	{
		return queryMap.containsKey(name);
	}
	
	public String buildQuery(String name, Object... contextEntries)
	{
		return buildQuery(name, null, CCGUtility.buildMap(contextEntries));
	}
	
	public String buildQuery(String name, List<Object> paramValues, Object... contextEntries)
	{
		return buildQuery(name, paramValues, CCGUtility.buildMap(contextEntries));
	}
	
	public String buildQuery(String name, List<Object> paramValues, Map<String, Object> context)
	{
		Template template = templateMap.get(name);
		
		if(template == null)
		{
			try
			{
				template = new Template(name, queryMap.get(name), configuration);
				templateMap.put(name, template);
			}catch(Exception ex)
			{
				throw new IllegalStateException("An error occurred while loading delete query template", ex);
			}
		}
		
		try
		{
			paramCollectorDirective.reset(context);;
			
			StringWriter writer = new StringWriter();
			template.process(context, writer);
			
			if(paramValues != null)
			{
				paramValues.addAll(paramCollectorDirective.getParamValues());
			}
			
			writer.flush();
			return writer.toString();
		}catch(Exception ex)
		{
			throw new IllegalStateException("An exception occurred while building query: " + name, ex);
		}
	}
}
