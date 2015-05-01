package com.fw.ccg.ds;

import java.util.Collection;
import java.util.ListIterator;

import com.fw.ccg.core.Attributes;

/**
 * <BR><BR>
 * <P>
 * Table is a structure to represent 2D data. Just like the standard 1D list interface 
 * (java.util.List), Table is meant for 2D. 
 * </P>
 * <P>
 * The table is divided into rows and columns. Table can have different column count for 
 * different rows, in other words, just like java 2D array, all the row sizes need not be
 * same. Thus giving the flexibility for each to grow indepedent of other row.
 * </P>
 * <P>
 * Just like the iterator for List objects, Table supports TableIterator, an iterator to 
 * iterate through 2D data.
 * </P>
 * <BR>
 * @author A. Kranthi Kiran
 */
public interface Table extends Attributes
{
	/**
	 * @return Number of rows in this table.
	 */
	public int noOfRows();
	
	/**
	 * Fetches number of columns in specified row.
	 * @param row Row number.
	 * @return Number of columns in specified row.
	 * @throws IndexOutOfBoundsException if row&lt;0 or row&gt;=noOfRows()
	 */
	public int noOfColoumns();
	
	/**
	 * Returns a Table iterator who initial position points to specified row and column. 
	 * @param row Row number.
	 * @param col Column number in specified row.
	 * @return  Table iterator at specified position.
	 * @throws IndexOutOfBoundsException if row&lt;0 or row&gt;=noOfRows() or 
	 * 			if col&lt;0 or col&gt;=noOfColumns(row)
	 */
	public TableIterator tableIterator(int row,int col);
	
	/**
	 * @return Table iterator for this table.
	 */
	public TableIterator tableIterator();
	
	public int add(Collection elements);
	/**
	 * Appends specified collection of elements at the end of specified row. 
	 * If row = noOfRows() then a new row is created
	 * and collection elements gets appended in that row.
	 * @param row Row number.
	 * @param elements Elements that needs to be added.
	 * @throws IndexOutOfBoundsException if row&lt;0 or row&gt;noOfRows()
	 */
	public int add(int row,Collection elements);
	
	/**
	 * Inseterts specified collection of elements at specified column in specified row.
	 * @param row Row number.
	 * @param col Column number in specified row.
	 * @param elements Elements that needs to be inserted.
	 * @throws IndexOutOfBoundsException if row&lt;0 or row&gt;noOfRows() or 
	 * 			row=noOfRows() and col!=0 or
	 * 			if col&lt;0 or col&gt;=noOfColumns(row)
	 */
	public int add(int row,int col,Collection elements);
	
	/**
	 * Replaces the element at specified position with the specified element.
	 * @param row Row number.
	 * @param col Column number in the specified row.
	 * @param element Element that needs to be set at specified position.
	 * @throws IndexOutOfBoundsException if row&lt;0 or row&gt;=noOfRows() or 
	 * 			if col&lt;0 or col&gt;=noOfColumns(row)
	 */
	public Object set(int row,int col,Object element);
	
	/**
	 * Fetches the element at specified position.
	 * @param row Row number.
	 * @param col Column number in the specified row.
	 * @return Element at specified position.
	 * @throws IndexOutOfBoundsException if row&lt;0 or row&gt;=noOfRows() or 
	 * 			if col&lt;0 or col&gt;=noOfColumns(row)
	 */
	public Object get(int row,int col);
	
	public void add(int row);
	public void add();
	
	
	/**
	 * Removes specified row of elements from the table.
	 * @param row Row number that needs to be removed.
	 * @return Removed row as a List of elements.
	 * @throws IndexOutOfBoundsException if row&lt;0 or row&gt;=noOfRows()
	 */
	public Object[] removeRow(int row);
	public Object[] removeColumn(int col);
	
	
	/**
	 * Clears all the data from this table.
	 */
	public void clear();
	
	/**
	 * If the other object is also table and contains same data and attributes as this table 
	 * then the target table is considered to be equal.
	 * @param other
	 * @return true if other is equal to this table.
	 */
	public boolean equals(Object other);
	
	public ListIterator rowIterator(int row);
	public ListIterator columnIterator(int col);
	public ListIterator rowIterator(int row,int from);
	public ListIterator columnIterator(int col,int from);
}
