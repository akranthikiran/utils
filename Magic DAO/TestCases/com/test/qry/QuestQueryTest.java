package com.test.qry;

import java.sql.SQLException;
import java.util.HashMap;

import junit.framework.TestCase;

import com.fw.dao.qry.QueryManager;
import com.fw.dao.qry.impl.MapQueryFilter;
import com.fw.dao.qry.impl.XMLQueryFactory;

public class QuestQueryTest extends TestCase
{
		public void testQuestParams() throws SQLException
		{
			QueryManager manager=XMLQueryFactory.loadFromXML("/testQuestParam.xml");
			
			HashMap<String,Object> map=new HashMap<String,Object>();
			map.put("appNo",1);
			
			System.out.println(manager.fetchRecords("getAppNames",new MapQueryFilter(map)));
		}
	
		public void testQuestFunc() throws SQLException
		{
			QueryManager manager=XMLQueryFactory.loadFromXML("/testQuestParam.xml");
			
			HashMap<String,Object> map=new HashMap<String,Object>();
			map.put("appName","%aol%");
			
			System.out.println(manager.fetchRecords("getAppNames_1",new MapQueryFilter(map)));
		}
		
		public static void main(String args[]) throws SQLException
		{
			new QuestQueryTest().testQuestFunc();
		}
}
