package com.fw.ccg.ds;

/**
 * <BR><BR>
 * This interface defines the paging mechanism using Table structure. These tables provide 
 * data in pages in table format.
 * <BR>
 * @author A. Kranthi Kiran
 */
public interface PagedTable extends Table
{
	/**
	 * Returns whether more pages are available in forward direction.
	 * @return true if more pages are available in forward direction.
	 */
	public boolean hasNextPage();
	/**
	 * Returns whether more pages are available in reverse direction.
	 * @return true if more pages are available in reverse direction.
	 */
	public boolean hasPreviousPage();
	
	/**
	 * Returns current page number.
	 * @return current page number.
	 */
	public int getCurrentPage();
	/**
	 * Returns total number of pages available.
	 * @return Total number of pages.
	 */
	public int getPageCount();
	/**
	 * Returns page size, that is, number of rows per page.
	 * @return page size of this paged table. 
	 */
	public int getPageSize();
	
	/**
	 * Refreshes the current page contents of the table.
	 */
	public void refreshPage();
	
	/**
	 * Iterates to next page. If page is not available, this method call will not
	 * have any effect.
	 */
	public void nextPage();
	
	/**
	 * Iterate to previous page. If page is not available, this method call will not
	 * have any effect.
	 */
	public void previousPage();
	
	/**
	 * Iterates to the specified page. If specified page is not available, this method 
	 * call will not have any effect.
	 * @param page Page number which needs to be pointed by this table.
	 */
	public void gotoPage(int page);
}
