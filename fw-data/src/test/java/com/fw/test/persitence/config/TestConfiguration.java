package com.fw.test.persitence.config;

import javax.sql.DataSource;

import com.fw.ccg.xml.XMLBeanParser;

public class TestConfiguration
{
	private static TestConfiguration instance = null;
	
	private DataSource dataSource;
	
	private TestConfiguration()
	{}
	
	public static synchronized TestConfiguration getTestConfiguration()
	{
		if(instance == null)
		{
			instance = new TestConfiguration();
			XMLBeanParser.parse(TestConfiguration.class.getResourceAsStream("/test-configuration.xml"), instance);
		}
		
		return instance;
	}
	
	public void setDataSource(DataSource dataSource)
	{
		this.dataSource = dataSource;
	}
	
	public DataSource getDataSource()
	{
		return dataSource;
	}
}
