package com.test.qry;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import com.fw.dao.qry.QueryManager;
import com.fw.dao.qry.impl.BeanDataDigester;
import com.fw.dao.qry.impl.BeanQueryFilter;
import com.fw.dao.qry.impl.MapQueryFilter;
import com.fw.dao.qry.impl.XMLQueryFactory;

import junit.framework.TestCase;

public class QueryManagerTest extends TestCase
{
		public void testLoad()
		{
			QueryManager manager=XMLQueryFactory.loadFromXML("/testQueries.xml");
			
			HashMap<String,Object> map=new HashMap<String,Object>();
			System.out.println("Query getAppDetails: \n"+manager.getQuery("getAppDetails",new MapQueryFilter(map)));
		}

		public void testBulkCreate()
		{
			QueryManager manager=XMLQueryFactory.loadFromXML("/testQueries.xml");
			
			HashMap<String,Object> map=new HashMap<String,Object>();
			System.out.println("Query Without Param: \n"+manager.getQuery("getAppDetails1",new MapQueryFilter(map)));
			
			map.put("appNo",1000);
			System.out.println("\n\nQuery Without appNo: \n"+manager.getQuery("getAppDetails1",new MapQueryFilter(map)));
			
			map.put("appName","aOL");
			System.out.println("\n\nQuery Without appNo & name: \n"+manager.getQuery("getAppDetails1",new MapQueryFilter(map)));
		}
		
		public void testBuilding1()
		{
			QueryManager manager=XMLQueryFactory.loadFromXML("/testQueries.xml");
			
			HashMap<String,Object> map=new HashMap<String,Object>();
			System.out.println("Query Without Param: \n"+manager.getQuery("getAppDetails_param",new MapQueryFilter(map)));
			
			System.out.println("Query With NULL Param: \n"+manager.getQuery("getAppDetails_param",new MapQueryFilter(map),new LinkedList<Object>(),(Object)null));
			
			System.out.println("\n\nQuery With param: \n"+manager.getQuery("getAppDetails_param",new MapQueryFilter(map),new LinkedList<Object>(),2100));
		}
		
		public void testBuildingWithOtherParams()
		{
			QueryManager manager=XMLQueryFactory.loadFromXML("/testQueries.xml");
			
			HashMap<String,Object> map=new HashMap<String,Object>();
			System.out.println("Query Without Param: \n"+manager.getQuery("getAppDetails2",new MapQueryFilter(map)));
			map.put("mainFile","seRvI");
			System.out.println("\n\nQuery Without mainFile: \n"+manager.getQuery("getAppDetails2",new MapQueryFilter(map)));
			
			map.put("appNo",1000);
			System.out.println("\n\nQuery Without mainFile, appNo: \n"+manager.getQuery("getAppDetails2",new MapQueryFilter(map)));
			
			map.put("appName","aOL");
			System.out.println("\n\nQuery Without mainFile, appNo & name: \n"+manager.getQuery("getAppDetails2",new MapQueryFilter(map)));

		}
		
		public void testFetchRecords() throws SQLException
		{
			QueryManager manager=XMLQueryFactory.loadFromXML("/testQueries.xml");
			
			HashMap<String,Object> map=new HashMap<String,Object>();
			map.put("appName","%aOL%");
			
			System.out.println(manager.fetchRecords("getAppDetails1",new MapQueryFilter(map)));
			
		}
		
		public void testWithBeanFilter() throws SQLException
		{
			QueryManager manager=XMLQueryFactory.loadFromXML("/testQueries.xml");
			
			Application app=new Application(0,"%aOL%");
			
			BeanQueryFilter filter=new BeanQueryFilter(app);
			System.out.println("************With appNo=0 & appName=%aOL%");
			System.out.println(manager.getQuery("getAppDetails3",filter));
			System.out.println(manager.fetchRecords("getAppDetails3",filter));
			
			app=new Application(2,"   ");
			filter=new BeanQueryFilter(app);
			System.out.println("************With appNo=2 & appName=    ");
			System.out.println(manager.getQuery("getAppDetails3",filter));
			System.out.println(manager.fetchRecords("getAppDetails3",filter));

			app=new Application(2,"%this is test%");
			filter=new BeanQueryFilter(app);
			System.out.println("************With appNo=2 & appName=%this is test%");
			System.out.println(manager.getQuery("getAppDetails3",filter));
			System.out.println(manager.fetchRecords("getAppDetails3",filter));

			app=new Application(30,"%aol%");
			filter=new BeanQueryFilter(app);
			System.out.println("************With appNo=30 & appName=%aol%");
			System.out.println(manager.getQuery("getAppDetails3",filter));
			System.out.println(manager.fetchRecords("getAppDetails3",filter));
		}
		
		public void testBeanDigester() throws SQLException
		{
			QueryManager manager=XMLQueryFactory.loadFromXML("/testQueries.xml");
			
			Application app=new Application(0,"%aOL%");
			BeanQueryFilter filter=new BeanQueryFilter(app);
			System.out.println("************With appNo=0 & appName=%aOL%");
			System.out.println(manager.getQuery("getAppDetails4",filter));

			System.out.println("\n\n************With Default Digester");
			System.out.println(manager.fetchBeans("getAppDetails4",filter,new BeanDataDigester()));
			
			System.out.println("\n\n************With Digester1");
			System.out.println(manager.fetchBeans("getAppDetails4",filter,new BeanDataDigester("dig1")));

			System.out.println("\n\n************With Digester2");
			System.out.println(manager.fetchBeans("getAppDetails4",filter,new BeanDataDigester("dig2")));

			System.out.println("\n\n************With Digester3");
			System.out.println(manager.fetchBeans("getAppDetails4",filter,new BeanDataDigester("dig3")));
		}
		
		public void testPropBeanDigester() throws SQLException
		{
			QueryManager manager=XMLQueryFactory.loadFromXML("/testQueries.xml");
			
			Application app=new Application(0,"%aOL%");
			BeanQueryFilter filter=new BeanQueryFilter(app);
			
			System.out.println("************Output of getAppDetails5\n");
			System.out.println(manager.fetchBeans("getAppDetails5",filter));

			System.out.println("************Output of getAppDetails5_1\n");
			System.out.println(manager.fetchBeans("getAppDetails5_1",filter));
		}
		
		public void testFetchList() throws SQLException
		{
			QueryManager manager=XMLQueryFactory.loadFromXML("/testQueries.xml");
			
			Application app=new Application(0,"%aol%");
			BeanQueryFilter filter=new BeanQueryFilter(app);
			System.out.println(manager.fetchSingleColumnList("getAppNames",filter));
		}
		
		public void testFetchListWithParam() throws SQLException
		{
			QueryManager manager=XMLQueryFactory.loadFromXML("/testQueries.xml");
			
			Application app=new Application(0,"%aol%");
			BeanQueryFilter filter=new BeanQueryFilter(app);
			System.out.println(manager.fetchSingleColumnList("getAppNames_1",filter));
		}
		
		public void testFetchMap() throws SQLException
		{
			QueryManager manager=XMLQueryFactory.loadFromXML("/testQueries.xml");
			
			Application app=new Application(0,"%aol%");
			BeanQueryFilter filter=new BeanQueryFilter(app);
			System.out.println(manager.fetchMap("getAppMap",filter));
		}
		
		public void testFetchMapWithParams() throws SQLException
		{
			QueryManager manager=XMLQueryFactory.loadFromXML("/testQueries.xml");
			
			Application app=new Application(0,"%aol%");
			BeanQueryFilter filter=new BeanQueryFilter(app);
			System.out.println(manager.fetchMap("getAppMap_1",filter));
			System.out.println(manager.fetchMap("getAppMap_2",filter));
		}
		
		public void textWithCollectionParam() throws SQLException
		{
			ArrayList<String> lst=new ArrayList<String>();
			
			lst.add("waolversioning");
			lst.add("AOL TopSpeed");
			lst.add("AOL");
			
			QueryManager manager=XMLQueryFactory.loadFromXML("/testQueries.xml");
			
			HashMap<String,Object> map=new HashMap<String,Object>();
			map.put("appName",lst);
			
			System.out.println(manager.fetchRecords("getAppDetails8",new MapQueryFilter(map)));
		}
		
		public void testQuestParam() throws SQLException
		{
			QueryManager manager=XMLQueryFactory.loadFromXML("/testQueries.xml");
			
			System.out.println(manager.fetchSingleColumnList("getAppNames_2","%aol%"));
			
			System.out.println(manager.fetchBean("getAppDetails4_1","%aol%"));
		}
		
		public static void main(String args[]) throws SQLException
		{
			new QueryManagerTest().testQuestParam();
		}
}
