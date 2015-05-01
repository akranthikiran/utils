package com.test.qry;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.fw.dao.qry.FunctionInstance;

import junit.framework.TestCase;

public class FunctionInstanceTest extends TestCase
{
	private class FuncDataProvider implements FunctionInstance.DataProvider
	{
		private Map<String,Object> nameToParam=new HashMap<String,Object>();
		private Map<String,Object> nameToCol=new HashMap<String,Object>();
		
			@Override
	        public Object getColumn(String funcName,String name)
	        {
		        return nameToCol.get(name);
	        }
	
			@Override
	        public Object getProperty(String funcName,String name)
	        {
		        return nameToParam.get(name);
	        }
	}
	
	public void testArgs()
	{
		String args="test,@param1,3,@param2";
		FunctionInstance inst=FunctionInstance.parse("testWithArgs",args,true,false);
		
		System.out.println("Input: testWithArgs("+args+")");
		System.out.println("Output: "+inst);
	}
	
	public void testArgsWithComma()
	{
		String args="test,@param1,3,@param2,bla\\,bla";
		FunctionInstance inst=FunctionInstance.parse("testArgsWithComma",args,true,false);
		
		System.out.println("Input: testWithArgs("+args+")");
		System.out.println("Output: "+inst);
	}
	
	public void testArgsWithFunc()
	{
		String args="test,@param1,3,@param2,lower(@param1),upper(2,top(2,pop(3,4)))";
		FunctionInstance inst=FunctionInstance.parse("testArgsWithFunc",args,true,false);
		
		System.out.println("Input: testWithArgs("+args+")");
		System.out.println("Output: "+inst);
	}
	
	public void testFuncExe1()
	{
		String args="@date,MM/dd/yyyy hh:mm:ss";
		FunctionInstance inst=FunctionInstance.parse("dateToStr",args,true,false);
		
		FuncDataProvider dp=new FuncDataProvider();
		dp.nameToParam.put("date",new Date());
		
		System.out.println("Input: dateToStr("+args+")");
		System.out.println("Output: "+inst);
		System.out.println("Exe Output: "+inst.invoke(dp));
	}
	
	public void testFuncExe2()
	{
		String args="@col,\\(,\\,,\\)";
		FunctionInstance inst=FunctionInstance.parse("colToStr",args,true,false);
		
		FuncDataProvider dp=new FuncDataProvider();
		LinkedList<String> lst=new LinkedList<String>();
		lst.add("test1");lst.add("test2");
		lst.add("test3");lst.add("test4");
		
		dp.nameToParam.put("col",lst);
		
		System.out.println("Input: colToStr("+args+")");
		System.out.println("Output: "+inst);
		System.out.println("Exe Output: "+inst.invoke(dp));
	}
	
	public void testFuncExe3()
	{
		String args="&str";
		FunctionInstance inst=FunctionInstance.parse("toList",args,true,true);
		
		FuncDataProvider dp=new FuncDataProvider();
		
		dp.nameToCol.put("str","This is a test string");
		
		System.out.println("Input: toList("+args+")");
		System.out.println("Output: "+inst);
		System.out.println("Exe Output: "+inst.invoke(dp));
	}
	
	public static void main(String args[])
	{
		FunctionInstanceTest inst=new FunctionInstanceTest();
		inst.testFuncExe3();
	}
	
	
}
