package com.fw.ccg.ds;

/**
 * <P>
 * PagedListTable is a implementation of PagedTable backed by java.util.ArrayList. The
 * data in the table is obtained using PagedDataSource. So that, the paging in the table
 * can be customized as per the needs.
 * </P>
 * <P> 
 * For example, the PageDataSource may support
 * caching for data among the pages, hence improving the efficiency.
 * </P> 
 * <BR>
 * @author A. Kranthi Kiran
 */
public class PagedListTable extends ArrayTable implements PagedTable
{
	private static final long serialVersionUID=1L;
	
	private PagedDataSource source;
	private PageAttributes pageAtt=new PageAttributes();
	private int pageCount=0;
	
		/**
		 * Constructs PagedListTable with specified paged data source and with a page size
		 * of 100.
		 * @param source
		 */
		public PagedListTable(PagedDataSource source)
		{
			super(source.getColumnSize());
				if(source==null)
					throw new NullPointerException("Page data source cannot be null.");
			this.source=source;
			pageCount=source.getPageCount(pageAtt);
		}
		
		/**
		 * Constructs PagedListTable with specified paged data source and with specified 
		 * page size.
		 * @param source Paged data source.
		 * @param pageSize Size of the pages.
		 */
		public PagedListTable(PagedDataSource source,int pageSize)
		{
			this(source);
			pageAtt.setPageSize(pageSize);
		}
		
		/* (non-Javadoc)
		 * @see com.ccg.ds.PagedTable#hasNextPage()
		 */
		public boolean hasNextPage()
		{
			return (pageAtt.getPage()<pageCount-1);
		}
		
		/* (non-Javadoc)
		 * @see com.ccg.ds.PagedTable#hasPreviousPage()
		 */
		public boolean hasPreviousPage()
		{
			return (pageAtt.getPage()>0);
		}
		
		/* (non-Javadoc)
		 * @see com.ccg.ds.PagedTable#getCurrentPage()
		 */
		public int getCurrentPage()
		{
			return pageAtt.getPage();
		}
		
		/* (non-Javadoc)
		 * @see com.ccg.ds.PagedTable#getPageCount()
		 */
		public int getPageCount()
		{
			return pageCount;
		}
		
		/* (non-Javadoc)
		 * @see com.ccg.ds.PagedTable#getPageSize()
		 */
		public int getPageSize()
		{
			return pageAtt.getPageSize();
		}

		/**
		 * Changes the page size of this paged table to specified size.
		 * @param pageSize New page size.
		 */
		public void setPageSize(int pageSize)
		{
			int pgSize=pageAtt.getPageSize();
				if(pgSize==pageSize)
					return;
			pageAtt.setPageSize(pageSize);
			pageCount=source.getPageCount(pageAtt);
			pageAtt.setPage(0);
			setTableData(null);
		}
		
		/* (non-Javadoc)
		 * @see com.ccg.ds.PagedTable#refreshPage()
		 */
		public void refreshPage()
		{
			gotoPage(pageAtt.getPage());
		}
		
		/* (non-Javadoc)
		 * @see com.ccg.ds.PagedTable#nextPage()
		 */
		public void nextPage()
		{
			gotoPage(pageAtt.getPage()+1);
		}

		/* (non-Javadoc)
		 * @see com.ccg.ds.PagedTable#previousPage()
		 */
		public void previousPage()
		{
			gotoPage(pageAtt.getPage()-1);
		}
		
		/* (non-Javadoc)
		 * @see com.ccg.ds.PagedTable#gotoPage(int)
		 */
		public void gotoPage(int page)
		{
				if(source.isPageCountChanged())
					pageCount=source.getPageCount(pageAtt);
			
				if(page>=pageCount)
					page=pageCount-1;
	
				if(page<=0)
					page=0;
			pageAtt.setPage(page);
			Table table=source.getPageData(pageAtt);
				if(table!=null)
					setTableData(table);
				else
					setTableData(null);
		}

		/**
		 * Two PagedListTable are considered equal if and if all the attributs, data, page 
		 * positions and other page attributes (like page size, page count) and page data source are
		 * equal. 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object other)
		{
				if(this==other)
					return true;
				
				if(!(other instanceof PagedListTable))
					return false;
				
				if(!super.equals(other))
					return false;
				
			PagedListTable otherTable=(PagedListTable)other;
				if(source!=null)
					return (otherTable.source==null);
				
				if(!pageAtt.equals(otherTable.pageAtt))
					return false;
				
			return (pageCount==otherTable.pageCount);
		}

		/**
		 * The hash code for the paged table is calculated using following formulae
		 * 		super.hashCode()+pageCount+(hash code of page attributes)+
		 * 					(hashCode() of Page data source)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode()
		{
			return super.hashCode()+pageCount+pageAtt.hashCode()+source.hashCode();
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#clone()
		 */
		public Object clone()
		{
			PagedListTable res=new PagedListTable(source);
			res.pageCount=pageCount;
			res.pageAtt=(PageAttributes)pageAtt.clone();
			res.setTableData(this);
			res.makeCopy(this);
			return res;
		}
}
