package com.fw.dao.qry;

import java.sql.PreparedStatement;
import java.util.Set;


public interface QuerySource
{
	public ConnectionSource getConnectionSource();
	
	public DataDigester<?> getDataDigester(Query query);
	
	public Set<String> getQueryNames();
	public boolean hasQuery(String name);
	
	public Query getQuery(String name);
	public void customize(String name,PreparedStatement pstmt);
	
	public Object getGlobalProperty(String name);
}
