package com.fw.dao.qry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fw.ccg.core.ValidateException;
import com.fw.ccg.core.Validateable;
import com.fw.ccg.xml.DynamicAttributeAcceptor;
import com.fw.ccg.xml.DynamicDataAcceptor;
import com.fw.ccg.xml.HybridTextBean;
import com.fw.dao.qry.impl.BeanQueryFilter;
import com.fw.util.PatternGroupMatcher;

/**
 * This is the parsed version of DB queries. This class is designed to be loaded directly from XML using "FW XML" API.
 * This class also holds the additional configuration for the queries.
 * <BR/><BR/>
 * This class is not expected to be used by developers directly. 
 */
public class Query implements Validateable
{
	private static Logger logger = LogManager.getLogger(Query.class);
	
	private static Pattern PARAM_PATTERN=Pattern.compile("\\$\\{(\\w+)\\}");
	private static Pattern QUEST_PARAM_PATTERN=Pattern.compile("(\\?\\??)");
	private static Pattern NAME_QUEST_PARAM_PATTERN=Pattern.compile("\\#\\{(\\w+)\\}");
	private static Pattern PARAM_W_FUNC_PATTERN=Pattern.compile("\\$\\{(\\w+)\\(([^\\{\\}]*)\\)\\}");
	private static Pattern QUEST_PARAM_W_FUNC_PATTERN=Pattern.compile("\\#\\{(\\w+)\\(([^\\{\\}]*)\\)\\}");
	
		private static class QueryFilterDataProvider implements FunctionInstance.DataProvider
		{
			private QueryFilter filter;
			
				public QueryFilterDataProvider(QueryFilter filter)
				{
					this.filter=filter;
				}
				
				@Override
	            public Object getColumn(String funcName,String name)
	            {
		            return null;
	            }
	
				@Override
	            public Object getProperty(String funcName,String name)
	            {
		            return filter.getProperty(funcName,name);
	            }
		}
	
		public static class QueryResult
		{
			private int phaseNo=-1;
			private LinkedList<Object> lst=new LinkedList<Object>();
			private boolean reqNextPhase=true;
			private StringBuilder builder=new StringBuilder();
			
				private QueryResult(QueryElement root)
				{
					lst.add(root);
				}
			
				private void addText(String txt)
				{
					builder.append(txt);
				}
				
				private void addObject(QuestObject obj)
				{
						if(builder.length()>0)
						{
							lst.add(builder.toString());
							builder.setLength(0);
						}
						
					lst.add(obj);
				}
				
				private void addElementForNextPhase(QueryElement element)
				{
						if(builder.length()>0)
						{
							lst.add(builder.toString());
							builder.setLength(0);
						}
						
					lst.add(element);
					reqNextPhase=true;
				}
				
				private boolean requiresNextPhase()
				{
					return reqNextPhase;
				}
				
				private void process(QueryFilter filter)
				{
					process(filter, false);
				}
				
				private void process(QueryFilter filter,boolean isBulkQuery)
				{
						if(!reqNextPhase)
							return;
						
					phaseNo++;
					reqNextPhase=false;
					
					List<Object> oldLst=lst;
					this.lst=new LinkedList<Object>();
					
						for(Object elem:oldLst)
						{
								if(elem instanceof ParamQueryElement)
								{
									if(isBulkQuery)
										throw new IllegalStateException("Param query element is found in bulk query execution: "+elem);
								}
								
								if(elem instanceof QueryElement)
								{
									((QueryElement)elem).process(filter,this, isBulkQuery);
									continue;
								}
								
							addText((String)elem);
						}

						if(builder.length()>0)
						{
							lst.add(builder.toString());
							builder.setLength(0);
						}
				}
				
				private String toString(QueryFilter filter,List<Object> finalParams,Object... params)
				{
					StringBuilder builder=new StringBuilder();
					int paramIdx=0;
					QuestObject questObj=null;
					Object value=null;
					
						for(Object o:lst)
						{
								if(o instanceof QuestObject)
								{
										if(finalParams==null)
										{
											builder.append("?");
											continue;
										}
										
									questObj=(QuestObject)o;
									
										if(!questObj.isNamed() && (params==null || params.length<=paramIdx))
											throw new IllegalStateException("Insufficient number of parameters supplied: "+paramIdx);
										
									value=questObj.getValue(filter,paramIdx,params);
										
										if(value==null)
										{
											builder.append(filter.getNullString());
										}
										else
										{
											builder.append("?");
											finalParams.add(value);
										}
										
										if(!questObj.isNamed())
											paramIdx++;
										
									continue;
								}
								
							builder.append(o);
						}
						
					return builder.toString();
				}
				
				public String toString(BulkQueryFilter filter)
				{
					StringBuilder builder=new StringBuilder();
					QuestObject questObj=null;
					
						for(Object o:lst)
						{
								if(o instanceof QuestObject)
								{
									questObj=(QuestObject)o;
									
										if(!questObj.isNamed())
											throw new IllegalStateException("Query param (?) is not supported for bulk operations.");
										
									builder.append("?");
									continue;
								}
								
							builder.append(o);
						}
						
					return builder.toString();
				}
				
				public void populateParams(BulkQueryFilter filter,List<Object> finalParams)
				{
					QuestObject qryElem=null;
					Object value=null;
					Class<?> paramType=null;
					
						for(Object o:lst)
						{
								if(o instanceof QuestObject)
								{
									qryElem=(QuestObject)o;
									value=qryElem.getValue(filter,-1);
									
										if(value==null)
										{
											paramType=qryElem.getValueType(filter);
											value=new SQLNull(SQLTypeMapping.getSqlMapping(paramType));
										}
										
									finalParams.add(value);
									continue;
								}
						}
				}
		}
		
		public static abstract class QueryElement
		{
			public abstract void process(QueryFilter filter,QueryResult res, boolean isBulkQuery);
		}
		
		public static interface QuestObject
		{
			public Object getValue(QueryFilter filter,int paramIdx,Object... params);
			public Class<?> getValueType(BulkQueryFilter filter);
			public boolean isNamed();
		}
		
		public static class TextQueryElement extends QueryElement
		{
			private String text;
			
				public TextQueryElement(String text)
				{
					this.text=text;
				}
				
				public void process(QueryFilter filter,QueryResult res, boolean isBulkQuery)
				{
					res.addText(text);
				}
				
				public String toString()
				{
					return super.toString()+"{"+text+"}";
				}
		}
		
		public static class QuestParamQueryElement extends QueryElement implements QuestObject
		{
			private String name;
			
				public QuestParamQueryElement()
				{}
				
				public QuestParamQueryElement(String name)
				{
					this.name=name;
				}
				
				@Override
				public boolean isNamed()
				{
					return (name!=null);
				}
				
				public String getName()
				{
					return name;
				}
				
				@Override
	            public void process(QueryFilter filter,QueryResult res, boolean isBulkQuery)
	            {
					res.addObject(this);
	            }

				@Override
                public Object getValue(QueryFilter filter,int paramIdx,Object... params)
                {
						if(name!=null)
							return filter.getProperty(name);
						
	                return params[paramIdx];
                }

				@Override
                public Class<?> getValueType(BulkQueryFilter filter)
                {
	                return filter.getParamType(name);
                }
				
				@Override
				public String toString()
				{
					return "#{"+name+"}";
				}
		}
		
		public static class ParamQueryElement extends QueryElement
		{
			private String name;
			
				public ParamQueryElement(String name)
				{
					this.name=name;
				}
				
				public void process(QueryFilter filter,QueryResult res, boolean isBulkQuery)
				{
					Object param=filter.getProperty(name);
					
						if(param==null)
							res.addText(filter.getNullString());
						else
							res.addText(param.toString());
				}
				
				@Override
				public String toString()
				{
					return "${"+name+"}";
				}
		}
		
		public static class FunctionQueryElement extends QueryElement implements QuestObject
		{
			private FunctionInstance func;
			private boolean questParam;
			
				public FunctionQueryElement(String name,String params,boolean questParam)
				{
					func=FunctionInstance.parse(name,params,true,false);
					func.validate();
					this.questParam=questParam;
				}
				
				public FunctionQueryElement(String name,String params)
				{
					this(name,params,false);
				}
				
				public boolean isQuestParam()
                {
                	return questParam;
                }

				public void process(QueryFilter filter,QueryResult res, boolean isBulkQuery)
				{
						if(questParam)
						{
							res.addObject(this);
							return;
						}
						
						if(isBulkQuery)
							throw new IllegalStateException("A param based function paramter encountered in bulk query execution: "+this);
						
					Object retVal=func.invoke(new QueryFilterDataProvider(filter));
					
						if(retVal==null)
							res.addText(filter.getNullString());
						else
							res.addText(retVal.toString());
				}

				@Override
                public Object getValue(QueryFilter filter,int paramIdx,Object... params)
                {
					Object retVal=func.invoke(new QueryFilterDataProvider(filter));
	                return retVal;
                }

				@Override
                public Class<?> getValueType(BulkQueryFilter filter)
                {
	                return func.getReturnType();
                }

				@Override
                public boolean isNamed()
                {
	                return true;
                }
				
				@Override
				public String toString()
				{
						if(questParam)
							return "#"+func;
						
					return "$"+func;
				}
		}
		
		
		public static class NodeQueryElement extends QueryElement implements DynamicDataAcceptor,HybridTextBean,Validateable,DynamicAttributeAcceptor
		{
			private String name;
			private List<QueryElement> elements;
			private Map<String,String> nameToAttr=new HashMap<String,String>();
			
				public NodeQueryElement(String name)
				{
						if(name==null || name.trim().length()==0)
							throw new IllegalArgumentException("Name cannnot be null or empty string.");
						
					this.name=name;
				}
				
				public NodeQueryElement()
				{}

	            public void set(String propName,String value)
	            {
					nameToAttr.put(propName,value);
	            }
	
				@Override
	            public void addText(String text)
	            {
						if(elements==null)
							elements=new ArrayList<QueryElement>(5);
						
					parseQueryString(text,elements);
	            }
				
				public void addPart(NodeQueryElement element)
				{
						if(elements==null)
							elements=new ArrayList<QueryElement>(5);
						
					elements.add(element);
				}
				
				public void process(QueryFilter filter,QueryResult res, boolean isBulkQuery)
				{
						//if this is the root element
						if(name==null)
						{
							Query.toString(elements,filter,res, isBulkQuery);
							return;
						}
						
					FilterResult filterResult = filter.accept(name,nameToAttr,res.phaseNo);
					int filterResVal = filterResult.getResult();
					
						if(filterResVal==QueryFilter.REJECT)
						{
							return;
						}
						
						if(filterResVal==QueryFilter.REPLACE)
						{
							res.addText(filter.getReplaceString(name,nameToAttr,res.phaseNo));
						}
						else if(filterResVal==QueryFilter.NEXT_PHASE)
						{
							res.addElementForNextPhase(this);
							return;
						}
						else if(filterResVal == QueryFilter.PROCESS_COLLECTION)
						{
							@SuppressWarnings({"unchecked", "rawtypes"})
							Collection<Object> collection = (Collection)filterResult.getValue();
							BeanQueryFilter beanQueryFilter = null;
							String delimiter = filterResult.getDelimiter();
							Iterator<Object> it = collection.iterator();
							
							if(delimiter == null)
							{
								delimiter = "";
							}
							else
							{
								delimiter = delimiter.replace("\\n", "\n");
							}
							
							Object val = null;
							
							while(it.hasNext())
							{
								val = it.next();
								
								if(beanQueryFilter == null)
								{
									beanQueryFilter = new BeanQueryFilter(val);
								}
								else
								{
									beanQueryFilter.setBean(val);
								}
								
								Query.toString(elements, beanQueryFilter, res, isBulkQuery);
								
								if(it.hasNext())
								{
									res.addText(delimiter);
								}
							}
							
							return;
						}
						
					Query.toString(elements,filter,res, isBulkQuery);
				}

				@Override
                public void validate() throws ValidateException
                {
						if(elements!=null)
							elements=new ArrayList<QueryElement>(elements);
                }

				@Override
                public void add(String propName,Object obj)
                {
					addPart((NodeQueryElement)obj);
                }

				@Override
                public void add(String propName,String id,Object obj)
                {}

				@Override
                public boolean isIDBased(String propName)
                {
	                return false;
                }
				
				@Override
				public String toString()
				{
					return "<"+name+"/>";
				}
		}
		
	private Map<String,String> paramMap=new HashMap<String,String>();
	private Map<String,Object> attrMap=new HashMap<String,Object>();
	private Map<String,FunctionInstance> colToExpr=new HashMap<String,FunctionInstance>();
	private NodeQueryElement dbQuery;
	
		private static void toString(List<QueryElement> elements,QueryFilter filter,QueryResult res, boolean isBulkQuery)
		{
				for(QueryElement e:elements)
				{
						if(isBulkQuery && (e instanceof ParamQueryElement))
						{
							logger.warn("A param query element found in build query execution: " + e);
							//throw new IllegalStateException("A param query element found in build query execution: "+e);
						}
						
					e.process(filter, res, isBulkQuery);
				}
		}
		
		private static void parseQueryString(String qry,List<QueryElement> elements)
		{
			StringBuffer buff=new StringBuffer();
			PatternGroupMatcher groupMatcher=new PatternGroupMatcher(qry,PARAM_PATTERN,PARAM_W_FUNC_PATTERN,
												QUEST_PARAM_PATTERN,NAME_QUEST_PARAM_PATTERN,QUEST_PARAM_W_FUNC_PATTERN);
			
			String name=null,params=null;
			
				while(groupMatcher.find())
				{
					groupMatcher.appendReplacement(buff, "");
					
						if(buff.length()>0)
						{
							elements.add(new TextQueryElement(buff.toString()));
						}
						
					name=params=null;
					
						switch(groupMatcher.getMatchIndex())
						{
							case 0:
								name=groupMatcher.group(1);
								elements.add(new ParamQueryElement(name));
							break;
							case 1:
								name=groupMatcher.group(1);
								params=groupMatcher.group(2);
								elements.add(new FunctionQueryElement(name,params));
							break;
							case 2:
								params=groupMatcher.group(1);
								
									if(params.length()>1)
										elements.add(new TextQueryElement("?"));
									else
										elements.add(new QuestParamQueryElement());
							break;
							case 3:
								name=groupMatcher.group(1);
								elements.add(new QuestParamQueryElement(name));
							break;
							case 4:
								name=groupMatcher.group(1);
								params=groupMatcher.group(2);
								elements.add(new FunctionQueryElement(name,params,true));
							break;
						}
						
					buff.setLength(0);
				}
				
			groupMatcher.appendTail(buff);
			
				if(buff.length()>0)
				{
					elements.add(new TextQueryElement(buff.toString()));
				}
		}
		
		public void setDbQuery(NodeQueryElement dbQuery)
        {
        	this.dbQuery=dbQuery;
        }

		public void addColExpr(String name,String value)
		{
				try
				{
					colToExpr.put(name,FunctionInstance.parse(value,true,true));
					return;
				}catch(Exception ex)
				{
					throw new IllegalArgumentException("Non function expression encountered for column expression \""+name+"\": "+value);
				}
		}
		
		public boolean hasColumnExpression(String name)
		{
			return colToExpr.containsKey(name);
		}
		
		public FunctionInstance getColumnExpression(String name)
		{
			return colToExpr.get(name);
		}
		
		public void addParam(String name,String value)
		{
			paramMap.put(name,value);
		}
		
		public String getParam(String name)
		{
			return paramMap.get(name);
		}
		
		public void setAttribute(String name,Object attr)
		{
			attrMap.put(name,attr);
		}
		
		public Object getAttribute(String name)
		{
			return attrMap.get(name);
		}
	
		public String toText(QueryFilter filter,List<Object> finalParams,Object... params)
		{
			QueryResult qryRes=new QueryResult(dbQuery);
			
				while(qryRes.requiresNextPhase())
				{
					qryRes.process(filter);
				}
				
			String res=qryRes.toString(filter,finalParams,params);
			return res.replaceAll("\\n\\s+","\n");
		}

		public QueryResult buildBulkQuery(QueryFilter filter)
		{
			QueryResult qryRes=new QueryResult(dbQuery);
			
				while(qryRes.requiresNextPhase())
				{
					qryRes.process(filter,true);
				}
				
			return qryRes;
		}
		
		@Override
        public void validate() throws ValidateException
        {
				if(dbQuery==null || dbQuery.elements.isEmpty())
					throw new ValidateException("DB Query can not be null or empty");
        }

}
