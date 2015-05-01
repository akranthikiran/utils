package com.fw.ccg.ds;

/**
 * <BR><BR>
 * An iterator to iterate throw 2D data.
 * <BR>
 * @author A. Kranthi Kiran
 */
public interface TableIterator
{
	/**
	 * Returns true if this table iterator has more rows when traversing 
	 * the table in the forward direction.
	 * @return true if this table iterator has more rows when traversing 
	 * the table in the forward direction.
	 */
	public boolean hasNextRow();
	
	/**
	 * Returns true if this table iterator has more columns when traversing 
	 * the row in the forward direction.
	 * @return true if this table iterator has more rows when traversing 
	 * the row in the forward direction.
	 */
	public boolean hasNextColumn();
	
	/**
	 * Returns whether iteration to next row in forward direction is successful.
	 * @return true if iteration to next row in forward direction is successful.
	 */
	public boolean nextRow();
	
	/**
	 * Moves to next column in current row and returns element at current position.
	 * @return next column element in the current row .
	 * @throws NoSuchElementException if there is no next column.
	 */
	public Object nextColumn();
	
}
