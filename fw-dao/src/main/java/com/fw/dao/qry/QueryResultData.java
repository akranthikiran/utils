package com.fw.dao.qry;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;

public class QueryResultData
{
	private ResultSet rs;
	private String columnNames[];
	private Query query;
	private QueryFilter filter;
	private boolean stopProcessing=false;
	
	private QueryResultDataProvider dataProvider=null;
	
		QueryResultData(Query query,QueryFilter filter,ResultSet rs) throws SQLException
		{
			this.query=query;
			this.rs=rs;
			this.filter=filter;
			
			ResultSetMetaData meta=rs.getMetaData();
			int len=meta.getColumnCount();
			columnNames=new String[len];
			
				for(int i=1;i<=len;i++)
					columnNames[i-1]=meta.getColumnLabel(i);
		}
		
		public Object[] toObjectArray() throws SQLException
		{
			Object data[]=new Object[columnNames.length];
			
				for(int i=0;i<data.length;i++)
					data[i]=rs.getObject(i+1);
				
			return data;
		}
		
		public String getQueryParam(String name)
		{
			return query.getParam(name);
		}
		
		public Object getQueryAttribute(String name)
		{
			return query.getAttribute(name);
		}
		
		public void setQueryAttribute(String name,Object attr)
		{
			query.setAttribute(name,attr);
		}
		
		public Object getProperty(String name)
		{
			return filter.getProperty(name);
		}
		
		public Object getProperty(String funcName,String name)
		{
			return filter.getProperty(funcName,name);
		}
		
		public void stopProcessing()
		{
			stopProcessing=true;
		}
		
		boolean getStopProcessing()
		{
			return stopProcessing;
		}
		
		public String[] getColumnNames()
		{
			return columnNames.clone();
		}
		
		public String getColumnName(int idx)
		{
			return columnNames[idx];
		}
		
		public int getColumnCount()
		{
			return columnNames.length;
		}
		
		public FunctionInstance getColumnExpression(String colName)
		{
			return query.getColumnExpression(colName);
		}
		
		public boolean hasColumnExpression(String colName)
		{
			return query.hasColumnExpression(colName);
		}
		
		public Object executeColumnExpression(String colName) throws SQLException
		{
			FunctionInstance func=query.getColumnExpression(colName);
			
				if(func==null)
					return getObject(colName);
				
			
				if(dataProvider==null)
					dataProvider=new QueryResultDataProvider(this);
				
			return func.invoke(dataProvider);
		}

		public Object executeColumnExpression(int colIdx) throws SQLException
		{
				if(colIdx<=0)
					throw new IllegalArgumentException("Column index starts with 0. Invalid column index specified - "+colIdx);
				
			String colName=getColumnName(colIdx-1);
			return executeColumnExpression(colName);
		}
		
		public BigDecimal getBigDecimal(String columnLabel) throws SQLException
	    {
		    return rs.getBigDecimal(columnLabel);
	    }
	
		public InputStream getBinaryStream(String columnLabel) throws SQLException
	    {
		    return rs.getBinaryStream(columnLabel);
	    }
	
		public Blob getBlob(String columnLabel) throws SQLException
	    {
		    return rs.getBlob(columnLabel);
	    }
	
		public boolean getBoolean(String columnLabel) throws SQLException
	    {
		    return rs.getBoolean(columnLabel);
	    }
	
		public byte getByte(String columnLabel) throws SQLException
	    {
		    return rs.getByte(columnLabel);
	    }
	
		public byte[] getBytes(String columnLabel) throws SQLException
	    {
		    return rs.getBytes(columnLabel);
	    }
	
		public Reader getCharacterStream(String columnLabel) throws SQLException
	    {
		    return rs.getCharacterStream(columnLabel);
	    }
	
		public Clob getClob(String columnLabel) throws SQLException
	    {
		    return rs.getClob(columnLabel);
	    }
	
		public Date getDate(String columnLabel) throws SQLException
	    {
		    return rs.getDate(columnLabel);
	    }
	
		public double getDouble(String columnLabel) throws SQLException
	    {
		    return rs.getDouble(columnLabel);
	    }
	
		public float getFloat(String columnLabel) throws SQLException
	    {
		    return rs.getFloat(columnLabel);
	    }
	
		public int getInt(String columnLabel) throws SQLException
	    {
		    return rs.getInt(columnLabel);
	    }
	
		public long getLong(String columnLabel) throws SQLException
	    {
		    return rs.getLong(columnLabel);
	    }
	
		public NClob getNClob(String columnLabel) throws SQLException
	    {
		    return rs.getNClob(columnLabel);
	    }
	
		public String getNString(String columnLabel) throws SQLException
	    {
		    return rs.getNString(columnLabel);
	    }
	
		public Object getObject(String columnLabel) throws SQLException
	    {
		    return rs.getObject(columnLabel);
	    }
	
		public Ref getRef(String columnLabel) throws SQLException
	    {
		    return rs.getRef(columnLabel);
	    }
	
		public short getShort(String columnLabel) throws SQLException
	    {
		    return rs.getShort(columnLabel);
	    }
	
		public SQLXML getSQLXML(String columnLabel) throws SQLException
	    {
		    return rs.getSQLXML(columnLabel);
	    }
	
		public String getString(String columnLabel) throws SQLException
	    {
		    return rs.getString(columnLabel);
	    }
	
		public Time getTime(String columnLabel) throws SQLException
	    {
		    return rs.getTime(columnLabel);
	    }
	
		public Timestamp getTimestamp(String columnLabel) throws SQLException
	    {
		    return rs.getTimestamp(columnLabel);
	    }

		public Array getArray(int columnIndex) throws SQLException
        {
	        return rs.getArray(columnIndex);
        }

		public Array getArray(String columnLabel) throws SQLException
        {
	        return rs.getArray(columnLabel);
        }

		public InputStream getAsciiStream(int columnIndex) throws SQLException
        {
	        return rs.getAsciiStream(columnIndex);
        }

		public InputStream getAsciiStream(String columnLabel) throws SQLException
        {
	        return rs.getAsciiStream(columnLabel);
        }

		public BigDecimal getBigDecimal(int columnIndex) throws SQLException
        {
	        return rs.getBigDecimal(columnIndex);
        }

		public InputStream getBinaryStream(int columnIndex) throws SQLException
        {
	        return rs.getBinaryStream(columnIndex);
        }

		public Blob getBlob(int columnIndex) throws SQLException
        {
	        return rs.getBlob(columnIndex);
        }

		public boolean getBoolean(int columnIndex) throws SQLException
        {
	        return rs.getBoolean(columnIndex);
        }

		public byte getByte(int columnIndex) throws SQLException
        {
	        return rs.getByte(columnIndex);
        }

		public byte[] getBytes(int columnIndex) throws SQLException
        {
	        return rs.getBytes(columnIndex);
        }

		public Reader getCharacterStream(int columnIndex) throws SQLException
        {
	        return rs.getCharacterStream(columnIndex);
        }

		public Clob getClob(int columnIndex) throws SQLException
        {
	        return rs.getClob(columnIndex);
        }

		public Date getDate(int columnIndex) throws SQLException
        {
	        return rs.getDate(columnIndex);
        }

		public double getDouble(int columnIndex) throws SQLException
        {
	        return rs.getDouble(columnIndex);
        }

		public float getFloat(int columnIndex) throws SQLException
        {
	        return rs.getFloat(columnIndex);
        }

		public int getInt(int columnIndex) throws SQLException
        {
	        return rs.getInt(columnIndex);
        }

		public long getLong(int columnIndex) throws SQLException
        {
	        return rs.getLong(columnIndex);
        }

		public Reader getNCharacterStream(int columnIndex) throws SQLException
        {
	        return rs.getNCharacterStream(columnIndex);
        }

		public Reader getNCharacterStream(String columnLabel) throws SQLException
        {
	        return rs.getNCharacterStream(columnLabel);
        }

		public NClob getNClob(int columnIndex) throws SQLException
        {
	        return rs.getNClob(columnIndex);
        }

		public String getNString(int columnIndex) throws SQLException
        {
	        return rs.getNString(columnIndex);
        }

		public Object getObject(int columnIndex) throws SQLException
        {
	        return rs.getObject(columnIndex);
        }

		public Ref getRef(int columnIndex) throws SQLException
        {
	        return rs.getRef(columnIndex);
        }

		public RowId getRowId(int columnIndex) throws SQLException
        {
	        return rs.getRowId(columnIndex);
        }

		public RowId getRowId(String columnLabel) throws SQLException
        {
	        return rs.getRowId(columnLabel);
        }

		public short getShort(int columnIndex) throws SQLException
        {
	        return rs.getShort(columnIndex);
        }

		public SQLXML getSQLXML(int columnIndex) throws SQLException
        {
	        return rs.getSQLXML(columnIndex);
        }

		public String getString(int columnIndex) throws SQLException
        {
	        return rs.getString(columnIndex);
        }

		public Time getTime(int columnIndex) throws SQLException
        {
	        return rs.getTime(columnIndex);
        }

		public Timestamp getTimestamp(int columnIndex) throws SQLException
        {
	        return rs.getTimestamp(columnIndex);
        }
}
