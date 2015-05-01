package com.fw.ccg.ds;

import com.fw.ccg.core.SimpleAttributedBean;

/**
 * <BR><BR>
 * PageAttributes is an encapsulation over page size and page number. 
 * <BR>
 * @author A. Kranthi Kiran
 */
public class PageAttributes extends SimpleAttributedBean implements Cloneable
{
	private static final long serialVersionUID=1L;
	
	private int page;
	private int pageSize=100;
		/**
		 * Returns current page number.
		 * @return current page number.
		 */
		public int getPage()
		{
			return page;
		}
		
		/**
		 * Sets the page number to specified value.
		 * @param page Page number.
		 */
		public void setPage(int page)
		{
				if(page<0)
					throw new IllegalArgumentException("From cannot be less than zero(0).");
			this.page=page;
		}
		
		/**
		 * Returns page size.
		 * @return Page size.
		 */
		public int getPageSize()
		{
			return pageSize;
		}
		
		/**
		 * Sets the specified value as page size.
		 * @param pageSize Page size.
		 */
		public void setPageSize(int pageSize)
		{
				if(pageSize<1)
					throw new IllegalArgumentException("From cannot be less than 1.");
			this.pageSize=pageSize;
		}
		
		/**
		 * If page is treated as set of rows, this method will return the starting row
		 * number.
		 * @return Starting row number. 
		 */
		public int getStart()
		{
			return page*pageSize;
		}
		
		/**
		 * If page is treated as set of rows, this method will return the ending row
		 * number.
		 * @return Ending page number.
		 */
		public int getEnd()
		{
			return (page+1)*pageSize;
		}

		/**
		 * Two PageAttributes are trweated as equal if both have same page number 
		 * and size.
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object other)
		{
				if(this==other)
					return true;
				
				if(!(other instanceof PageAttributes))
					return false;
			PageAttributes otherAtt=(PageAttributes)other;
			
			return (page==otherAtt.page && pageSize==otherAtt.pageSize);
		}

		/**
		 * Hash code of this object is calcuated as sum of page and page size.
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode()
		{
			return page+pageSize;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#clone()
		 */
		public Object clone()
		{
			PageAttributes res=new PageAttributes();
			res.page=page;
			res.pageSize=pageSize;
			return res;
		}
}
