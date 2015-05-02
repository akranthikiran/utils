package com.fw.dao.qry.impl;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Map;

import com.fw.ccg.util.BeanUtil;
import com.fw.dao.qry.DataDigester;
import com.fw.dao.qry.FunctionInstance;
import com.fw.dao.qry.QueryResultData;
import com.fw.dao.qry.QueryResultDataProvider;
import com.fw.dao.qry.QueryUtil;

public class PropertyBeanDataDigester implements DataDigester<Object>
{
	//private static final Logger logger=Logger.getLogger(PropertyBeanDataDigester.class);
	
	private static final String SETTER_KEY="PropertyBeanDataDigester$setters#";
	private static final String BEAN_FUNC_KEY="PropertyBeanDataDigester$beanFunc#";

	public static final String QRY_PARAM_BEAN_TYE="beanType";
	public static final String QRY_PARAM_BEAN_FUNC_EXPR="beanFuncExpr";
	
	private String name;
	
		public PropertyBeanDataDigester(String name)
	    {
		    this.name=name;
	    }
		
		public PropertyBeanDataDigester()
        {}

		private FunctionInstance getBeanFunctionExpression(QueryResultData rsData)
		{
			String key=(name==null)?BEAN_FUNC_KEY:BEAN_FUNC_KEY+name;
			FunctionInstance funcInst=(FunctionInstance)rsData.getQueryAttribute(key);
			
				if(funcInst!=null)
					return funcInst;
				
			String paramName=(name==null)?QRY_PARAM_BEAN_FUNC_EXPR:QRY_PARAM_BEAN_FUNC_EXPR+"#"+name;
			String funcStr=rsData.getQueryParam(paramName);
			
				if(funcStr==null || funcStr.trim().length()==0)
					return null;
			
			funcInst=FunctionInstance.parse(funcStr,true,true);
			rsData.setQueryAttribute(key,funcInst);
			return funcInst;
		}
		
		private Object createBean(QueryResultData rsData)
		{
			FunctionInstance funcInst=getBeanFunctionExpression(rsData);
			
				if(funcInst!=null)
				{
					return funcInst.invoke(new QueryResultDataProvider(rsData));
				}
				
			String beanType=rsData.getQueryParam(QRY_PARAM_BEAN_TYE);
			
				if(beanType==null || beanType.trim().length()==0)
					throw new NullPointerException("No beanType specified.");
				
			Class<?> beanCls=null;
			
				try
				{
					beanCls=Class.forName(beanType.trim());
				}catch(Exception ex)
				{
					throw new IllegalStateException("Invalid bean type encountered: "+beanType,ex);
				}
				
				try
				{
					return beanCls.newInstance();
				}catch(Exception ex)
				{
					throw new IllegalStateException("An error occured while creating bean of type: "+beanType,ex);
				}
		}
	
		@SuppressWarnings("unchecked")
        private Map<String,Method> loadProperties(QueryResultData rsData,Class<?> cls)
		{
			Map<String,Method> nameToSetter=(Map<String,Method>)rsData.getQueryAttribute(SETTER_KEY);
			
				if(nameToSetter!=null)
					return nameToSetter;
				
			nameToSetter=BeanUtil.getSetterMethodMap(cls,false);
			rsData.setQueryAttribute(SETTER_KEY,nameToSetter);
			
			return nameToSetter;
		}

		@Override
	    public Object digest(QueryResultData rsData) throws SQLException
	    {
			Object bean=createBean(rsData);
			Map<String,Method> nameToSetter=loadProperties(rsData,bean.getClass());
			String columnNames[]=rsData.getColumnNames();
			Method setter=null;
			Object value=null;
			Class<?> paramTypes[]=null;
			
				for(int i=0;i<columnNames.length;i++)
				{
					setter=nameToSetter.get(columnNames[i]);
					
						if(setter==null)
							continue;
			
					value=rsData.executeColumnExpression(i+1);
					
						if(value==null)
							continue;
						
					paramTypes=setter.getParameterTypes();
					value=QueryUtil.convert(value,paramTypes[0]);
					
						try
						{
							setter.invoke(bean,value);
						}catch(Exception ex)
						{
							throw new IllegalStateException("An error occured while invoking property: "+columnNames[i],ex);
						}
				}
			
		    return bean;
	    }

		@Override
		public void finalizeDigester() 
		{}
}
