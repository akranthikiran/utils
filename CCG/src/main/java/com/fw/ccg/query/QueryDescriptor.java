package com.fw.ccg.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import com.fw.ccg.core.SimpleAttributedBean;

//TODO: equals & hashcode
public class QueryDescriptor extends SimpleAttributedBean implements Serializable,Cloneable
{
	private static final long serialVersionUID=1L;
	
	private ArrayList<Condition> conditions=null;
	private ArrayList<String> orderList=null;
	private ArrayList<String> groupList=null;
	private String ID=null;//query ID
	private String tableName;
		public QueryDescriptor(String ID)
		{
			setID(ID);
		}
		
		public final void setID(String ID)
		{
				if(ID==null)
					throw new NullPointerException("ID can not be null.");
			this.ID=ID;
		}
		
		public String getID()
		{
			return ID;
		}
	
		public void addCondition(Condition cond)
		{
				if(conditions==null)
					conditions=new ArrayList<Condition>();
			conditions.add(cond);
		}
		
		public void removeCondition(Condition cond)
		{
				if(conditions==null)
					return;
			conditions.remove(cond);
				if(conditions.isEmpty())
					conditions=null;
		}
		
		public Iterator getConditions()
		{
				if(conditions==null)
					return null;
			return conditions.iterator();
		}
		
		public int getConditionsCount()
		{
				if(conditions==null)
					return 0;
			return conditions.size();
		}
		
		public void setOrderingList(String cols[])
		{
				if(cols==null || cols.length==0)
				{
					orderList=null;
					return;
				}
				
				if(orderList==null)
					orderList=new ArrayList<String>();
				
			orderList.clear();
			
				for(int i=0;i<cols.length;i++)
					orderList.add(cols[i]);
		}
		
		public Iterator getOrderingList()
		{
				if(orderList==null)
					return null;
			return orderList.iterator();
		}
		
		public int getOrderingListCount()
		{
				if(orderList==null)
					return 0;
			return orderList.size();
		}
		
		
		public void setGroupingList(String cols[])
		{
				if(cols==null || cols.length==0)
				{
					groupList=null;
					return;
				}
				
				if(groupList==null)
					groupList=new ArrayList<String>();
				
			groupList.clear();
			
				for(int i=0;i<cols.length;i++)
					groupList.add(cols[i]);
		}
		
		public Iterator getGroupingList()
		{
				if(groupList==null)
					return null;
			return groupList.iterator();
		}

		public int getGroupingListCount()
		{
				if(groupList==null)
					return 0;
			return groupList.size();
		}
		
		public String getTableName()
		{
			return tableName;
		}

		public void setTableName(String tableName)
		{
			this.tableName=tableName;
		}
		
		@SuppressWarnings("unchecked")
		public Object clone()
		{
			QueryDescriptor res=new QueryDescriptor(ID);
			res.conditions=(conditions==null)?null:(ArrayList<Condition>)conditions.clone();
			res.orderList=(orderList==null)?null:(ArrayList<String>)orderList.clone();
			res.groupList=(groupList==null)?null:(ArrayList<String>)groupList.clone();
			res.tableName=tableName;
			
			return res;
		}
}
