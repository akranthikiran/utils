package com.fw.dao.qry.impl;

import java.lang.reflect.Constructor;
import java.sql.SQLException;

import com.fw.ccg.util.CCGUtility;
import com.fw.ccg.util.StringUtil;
import com.fw.dao.qry.DataDigester;
import com.fw.dao.qry.FunctionInstance;
import com.fw.dao.qry.QueryResultData;
import com.fw.dao.qry.QueryResultDataProvider;
import com.fw.dao.qry.QueryUtil;

/**
 * Currently only primitive/column/param data is accepted as params.
 * 
 * TODO: Constructor param can be one more constructor.
 * @author kranthikirana
 */
public class BeanDataDigester implements DataDigester<Object>
{
	private static final RecordDataDigester recDataDigester=new RecordDataDigester();
	private static final String CONSTR_KEY="BeanDataDigester$Construcotr#";
	private static final String FUNC_INST_KEY="BeanDataDigester$funcInst#";
	private static final String BEAN_FUNC_KEY="BeanDataDigester$beanFunc#";
	
	public static final String QRY_PARAM_CONSTRUCTOR="beanConstructor";
	public static final String QRY_PARAM_CONSTR_PARAM="constructorParams";
	public static final String QRY_PARAM_BEAN_FUNC_EXPR="beanFuncExpr";
	
	private String name=null;
	
		public BeanDataDigester(String name)
		{
			this.name=name;
		}
		
		public BeanDataDigester()
		{}
		
		private FunctionInstance geBeanFunctionExpression(QueryResultData rsData)
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
		
		private Constructor<?> getConstructor(QueryResultData rsData)
		{
			String key=(name==null)?CONSTR_KEY:CONSTR_KEY+name;
			
			Constructor<?> construct=(Constructor<?>)rsData.getQueryAttribute(key);
			
				if(construct!=null)
					return construct;
				
			String paramName=(name==null)?QRY_PARAM_CONSTRUCTOR:QRY_PARAM_CONSTRUCTOR+"#"+name;
			String constStr=rsData.getQueryParam(paramName);
			
				if(constStr==null)
					return null;
				
			constStr=constStr.trim();
			
				if(!constStr.endsWith(")"))
					throw new IllegalArgumentException("Constructor expression is expected to end with ')'.");
				
			int st=constStr.indexOf('(');
			
				if(st<0)
					throw new IllegalArgumentException("No '(' found in constructor declaration: "+constStr);
				
			int ed=constStr.lastIndexOf(')');
			
				if(ed<=st)
					throw new IllegalArgumentException("No ending ')' found in constructor declaration: "+constStr);
				
				if(ed-st==1)
					throw new IllegalArgumentException("Default constructors are not supported: "+constStr);
			
			String beanClsName=constStr.substring(0,st);
			String paramStr=constStr.substring(st+1,ed);
			
				try
				{
					Class<?> beanCls=CCGUtility.getClass(beanClsName.trim());
					String paramTokens[]=paramStr.split("\\s*\\,\\s*");
					Class<?> params[]=new Class<?>[paramTokens.length];
					String typeAndName[]=null;
					
						for(int i=0;i<paramTokens.length;i++)
						{
								if(paramTokens[i].length()==0)
									throw new IllegalStateException("Empty constructor paramter encountered: "+constStr);
								
							typeAndName=paramTokens[i].split("\\s+");
							
								if(typeAndName.length>2)
									throw new IllegalArgumentException("Invalid constructor paramter type encountered: "+paramTokens[i]);
							
								try
								{
									params[i]=CCGUtility.getClass(typeAndName[0]);
								}catch(Exception ex)
								{
									throw new IllegalArgumentException("Invalid constructor parameter type encounted: "+typeAndName[0],ex);
								}
						}
						
					construct=beanCls.getConstructor(params);
					
						if(construct==null)
							throw new IllegalArgumentException("No constructor found: "+constStr);
						
					rsData.setQueryAttribute(key,construct);
					return construct;
				}catch(Exception ex)
				{
					throw new IllegalStateException("An error occured while parsing constructor declaration: "+constStr,ex);
				}
		}
		
		private FunctionInstance getConstFunctionInstance(QueryResultData rsData)
		{
			String key=(name==null)?FUNC_INST_KEY:FUNC_INST_KEY+name;
			
			FunctionInstance inst=(FunctionInstance)rsData.getQueryAttribute(key);
			
				if(inst!=null)
					return inst;
				
			String paramName=(name==null)?QRY_PARAM_CONSTR_PARAM:QRY_PARAM_CONSTR_PARAM+"#"+name;
			String parsmStr=rsData.getQueryParam(paramName);
			
				if(parsmStr==null || parsmStr.trim().length()==0)
					throw new IllegalStateException("No constructor paramters are defined: "+QRY_PARAM_CONSTR_PARAM);
			
			FunctionInstance funcInst=FunctionInstance.parse("<init>",parsmStr,true,true);
			rsData.setQueryAttribute(key,funcInst);
			return funcInst;
		}
	
		@Override
	    public Object digest(QueryResultData rsData) throws SQLException
	    {
			FunctionInstance beanExpr=geBeanFunctionExpression(rsData);
			
				if(beanExpr!=null)
				{
					Object bean=beanExpr.invoke(new QueryResultDataProvider(rsData));
					
						if(processBeanRecord(bean))
							return null;
						
					return bean;
				}
			
			Constructor<?> construct=getConstructor(rsData);
			
				if(construct==null)
				{
					Object bean=recDataDigester.digest(rsData);
					
						if(processBeanRecord(bean))
							return null;
						
					return bean;
				}
				
			FunctionInstance funcInst=getConstFunctionInstance(rsData);
			Object paramValues[]=funcInst.getParamValues(new QueryResultDataProvider(rsData));
			
				try
				{
					paramValues=QueryUtil.convert(paramValues,construct.getParameterTypes());
					Object bean=construct.newInstance((Object[])paramValues);
					
						if(processBeanRecord(bean))
							return null;
						
					return bean;
				}catch(Exception ex)
				{
					String constStr=rsData.getQueryParam(QRY_PARAM_CONSTRUCTOR);
					throw new IllegalStateException("An error occured while invoking constructor: "+constStr+
								"\nValues: "+StringUtil.toString(paramValues),ex);
				}
	    }
		
		/**
		 * After construction of the bean this method will be called. By default this method will 
		 * not do anything and returns false.
		 * <BR/><BR/>
		 * Subclasses can override this method to digest the beans on the fly, instead of waiting 
		 * till the end. If the current bean need not be added to resultant bean list, then this method
		 * should return null.
		 * @param bean
		 * @return Return true, if "bean" should not be added to final resultant list 
		 */
		protected boolean processBeanRecord(Object bean)
		{
			return false;
		}

		@Override
		public void finalizeDigester() 
		{}
}
