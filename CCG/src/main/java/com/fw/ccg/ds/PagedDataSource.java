package com.fw.ccg.ds;

/**
 * <BR><BR>
 * <P>
 * This is an interface between page mechanism and undelying main data source. This interface 
 * is expected to fetch data from undelying data source and provided data to paging mechanism
 * in pages (data in certain specified amounts).
 * </P>
 * <BR>
 * @author A. Kranthi Kiran
 */
public interface PagedDataSource
{
	/**
	 * Fetches and returns data for the specified page attributes.
	 * @param att Encapsulation of page number and size.
	 * @return Data in specified page in Table format.
	 */
	public Table getPageData(PageAttributes att);
	
	/**
	 * Caluclates (depending on specified page size) and returns page count of the data 
	 * present in underlying data source.
	 * @param att Encapsulation of page number and size.
	 * @return Calculated page count.
	 */
	public int getPageCount(PageAttributes att);
	
	/**
	 * Queries and return if there is any chaneg in page count, which might be result
	 * of change in data in underlying data source.
	 * @return true if page count is changed.
	 */
	public boolean isPageCountChanged();
	
	public int getColumnSize();
}
