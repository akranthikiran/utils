package com.fw.dao.qry;

class SQLNull
{
	private int type;

		public SQLNull(int type)
	    {
		    this.type=type;
	    }

		public int getType()
        {
        	return type;
        }
		
		public String toString()
		{
			return "NULL";
		}
}
