package com.test.qry;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;

import junit.framework.TestCase;

import com.fw.dao.qry.QueryManager;
import com.fw.dao.qry.impl.BulkBeanQueryFilter;
import com.fw.dao.qry.impl.XMLQueryFactory;

public class BulkQueryTest extends TestCase
{
		public void testLoad() throws SQLException
		{
			QueryManager manager=XMLQueryFactory.loadFromXML("/testQueries.xml");

			LinkedList<TestBean> beans=new LinkedList<TestBean>();
			beans.add(new TestBean("test1",20));
			beans.add(new TestBean("test2",20));
			beans.add(new TestBean("test31",20));
			beans.add(new TestBean("test4",20));
			beans.add(new TestBean(null,20));
			beans.add(new TestBean(null,null));
			
			Integer res[]=manager.executeBulkUpdates("bulkInsert",new BulkBeanQueryFilter(beans));
			System.out.println("Result: "+Arrays.toString(res));
			
		}

		public static void main(String args[]) throws SQLException
		{
			new BulkQueryTest().testLoad();
		}
}
