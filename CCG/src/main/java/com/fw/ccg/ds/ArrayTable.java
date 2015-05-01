package com.fw.ccg.ds;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import com.fw.ccg.core.SimpleAttributedBean;

/**
 * <BR><BR>
 * An implementation of Table interface using java.util.ArrayListas rows and standard object arrays for column data.
 * <BR>
 * @author A. Kranthi Kiran
 */
public class ArrayTable extends SimpleAttributedBean implements Serializable,Table,Cloneable
{
	private static final long serialVersionUID=1L;

	private ArrayList records=new ArrayList();//this list should be randomly accessible
	private int columnSize;
	
		/**
		 * Constructs an empty table with specified number of columns.
		 */
		public ArrayTable(int columns)
		{
				if(columns<=0)
					throw new IllegalArgumentException("Invalid column size specified: "+columns);
			columnSize=columns;
		}
		
		/**
		 * Constructs a table with the same data as specified table.
		 * @param table Source table data.
		 */
		public ArrayTable(Table table)
		{
				if(table instanceof ArrayTable)
					setTableData((ArrayTable)table);
				else
					setTableData(table);
		}
		
		/**
		 * Clears current table data. And makes the current table data as replica of 
		 * specified table.
		 * @param table Source table data.
		 */
		public void setTableData(Table table)
		{
				if(table==null)
					throw new NullPointerException("Source table is null");
				
				if(table instanceof ArrayTable)
				{
					setTableData((ArrayTable)table);
					return;
				}
				
			records.clear();
			
				if(table.noOfRows()==0)
					return;
				
			TableIterator it=table.tableIterator();
			Object colData[]=null;
			int cols=table.noOfColoumns();
			
				if(cols<=0)
					throw new IllegalStateException("Invalid column size encountered: "+cols);
				
			columnSize=cols;
			int col=0;
				while(it.hasNextRow())
				{
					colData=new Object[columnSize];
					it.nextRow();
					
						for(col=0;it.hasNextColumn();col++)
							colData[col]=it.nextColumn();
						
					records.add(colData);
				}
		}
		
		/**
		 * Clears current table data. And makes the current table data as replica of 
		 * specified table.
		 * @param table Source table data.
		 */
		public void setTableData(ArrayTable table)
		{
				if(table==null)
					throw new NullPointerException("Source table is null");
				
			records.clear();
			
				if(table.noOfRows()==0)
					return;
			
			Object colList[]=null;
			Iterator it=table.records.iterator();
			columnSize=table.noOfColoumns();
				while(it.hasNext())
				{
					colList=(Object[])((Object[])it.next()).clone();
					records.add(colList);
				}
		}
		
		/* (non-Javadoc)
		 * @see com.ccg.ds.Table#noOfRows()
		 */
		public int noOfRows()
		{
			return records.size();
		}
		
		/* (non-Javadoc)
		 * @see com.ccg.ds.Table#noOfColoumns(int)
		 */
		public int noOfColoumns()
		{
			return columnSize;
		}
		
		/* (non-Javadoc)
		 * @see com.ccg.core.SimpleAttributedBean#clear()
		 */
		public void clear()
		{
			records.clear();
			super.clear();
		}

		/* (non-Javadoc)
		 * @see com.ccg.ds.Table#tableIterator(int, int)
		 */
		public TableIterator tableIterator(int row,int col)
		{
				if(row<0 || row>=records.size())
					throw new IndexOutOfBoundsException("Row index is out of bounds: "+row);
			
				if(col<0 || col>=columnSize)
					throw new IndexOutOfBoundsException("Column index is out of bounds: "+col);
			return new ListTableIterator(records,row,col);
		}

		/* (non-Javadoc)
		 * @see com.ccg.ds.Table#tableIterator()
		 */
		public TableIterator tableIterator()
		{
			return new ListTableIterator(records,columnSize);
		}

		
		
		/* (non-Javadoc)
		 * @see com.ccg.ds.Table#add(int, java.util.Collection)
		 */
		public int add(int row,Collection elements)
		{
				if(elements==null)
				{
					add(row);
					return 0;
				}
				
			int size=records.size();
				if(row<0 || row>size)
					throw new IndexOutOfBoundsException("Row index is out of bounds: "+row);
			Object colList[]=new Object[columnSize];
			records.add(row,colList);
			
			int i=0;
			Iterator it=elements.iterator();
				for(i=0;i<columnSize && it.hasNext();i++)
					colList[i]=it.next();
				
			return i;
		}

		/* (non-Javadoc)
		 * @see com.ccg.ds.Table#add(java.util.Collection)
		 */
		public int add(Collection elements)
		{
			return add(records.size(),elements);
		}
		
		/* (non-Javadoc)
		 * @see com.ccg.ds.Table#add(int, int, java.util.Collection)
		 */
		public int add(int row,int col,Collection elements)
		{
				if(elements==null)
				{
					add(row);
					return 0;
				}
			int size=records.size();
				if(row<0 || row>size)
					throw new IndexOutOfBoundsException("Row index is out of bounds: "+row);
				
				if(col<0 || col>columnSize)
					throw new IndexOutOfBoundsException("Column index is out of bounds: "+col);
			
			Object colList[]=null;
			colList=new Object[columnSize];
				if(row==size)
					records.add(colList);
				else
					records.add(row,colList);
				
			int i=col;
			Iterator it=elements.iterator();
			int count=0;
				for(;i<columnSize && it.hasNext();i++,count++)
					colList[i]=it.next();
					
			return count;
		}

		/* (non-Javadoc)
		 * @see com.ccg.ds.Table#set(int, int, java.lang.Object)
		 */
		public Object set(int row,int col,Object element)
		{
				if(row<0 || row>=records.size())
					throw new IndexOutOfBoundsException("Row index is out of bounds: "+row);
				
				if(col<0 || col>=columnSize)
					throw new IndexOutOfBoundsException("Column index is out of bounds: "+col);
				
			Object colList[]=(Object[])records.get(row);
			Object prev=colList[col];
			colList[col]=element;
			return prev;
		}

		/* (non-Javadoc)
		 * @see com.ccg.ds.Table#get(int, int)
		 */
		public Object get(int row,int col)
		{
				if(row<0 || row>=records.size())
					throw new IndexOutOfBoundsException("Row index is out of bounds: "+row);
				
				if(col<0 || col>=columnSize)
					throw new IndexOutOfBoundsException("Column index is out of bounds: "+col);
				
			Object colList[]=(Object[])records.get(row);
			return colList[col];
		}

		/* (non-Javadoc)
		 * @see com.ccg.ds.Table#addRow()
		 */
		public void add()
		{
			records.add(new Object[columnSize]);
		}

		public void add(int row)
		{
				if(row<0 || row>records.size())
					throw new IndexOutOfBoundsException("Row index is out of bounds: "+row);
			records.add(row,new Object[columnSize]);
		}

		/* (non-Javadoc)
		 * @see com.ccg.ds.Table#removeRow(int)
		 */
		public Object[] removeRow(int row)
		{
				if(row<0 || row>=records.size())
					throw new IndexOutOfBoundsException("Row index is out of bounds: "+row);
			return (Object[])records.remove(row);
		}

		public Object[] removeColumn(int col)
		{
				if(col<0 || col>=columnSize)
					throw new IndexOutOfBoundsException("Column index is out of bounds: "+col);
			ArrayList res=new ArrayList();
			Object rec[]=null;
			Iterator it=records.iterator();
				while(it.hasNext())
				{
					rec=(Object[])it.next();
					res.add(rec[col]);
					rec[col]=null;
				}
			return res.toArray();
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString()
		{
			StringBuffer res=new StringBuffer(super.toString());
			res.append("\n");
			Iterator it=records.iterator();
			Object row[]=null;
			int i=0;
			int colLen=columnSize-1;
				while(it.hasNext())
				{
					row=(Object[])it.next();
						for(i=0;i<colLen;i++)
						{
							res.append(row[i]);
							res.append(",");
						}
					res.append(row[colLen]);
					res.append("\n");
				}
			return res.toString();
		}

		/* (non-Javadoc)
		 * @see com.ccg.ds.Table#equals(java.lang.Object)
		 */
		public boolean equals(Object other)
		{
				if(this==other)
					return true;
				
				if(!(other instanceof ArrayTable))
					return false;
				
			ArrayTable otherTable=(ArrayTable)other;
			
				if(otherTable.noOfRows()!=records.size())
					return false;
			
				if(otherTable.noOfColoumns()!=columnSize)
					return false;
				
			Object row[]=null;
			Object otherRow[]=null;
			Iterator it=records.iterator();
			Iterator otherIt=otherTable.records.iterator();
			int i=0;
				while(it.hasNext())
				{
					row=(Object[])it.next();
					otherRow=(Object[])otherIt.next();
					
						for(i=0;i<columnSize;i++)
						{
								if((row[i]==null || otherRow[i]==null) && 
										(row[i]!=null || otherRow[i]!=null))
										return false;
							
								if(!row[i].equals(otherRow[i]))
									return false;
						}
				}
			return true;
		}

		/**
		 * This value will sum of the hashcodes of the objects in this 
		 * table. Thus when two tables are equal, thier hash codes will also be equal.
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode()
		{
			Object row[]=null;
			Iterator it=records.iterator();
			int i=0;
			int res=0;
				while(it.hasNext())
				{
					row=(Object[])it.next();
					
						for(i=0;i<columnSize;i++)
						{
								if(row[i]==null)
									continue;
							res+=row[i].hashCode();
						}
				}
			return res;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#clone()
		 */
		public Object clone()
		{
			ArrayTable res=new ArrayTable(this);
			res.makeCopy(this);
			return res;
		}

		/* (non-Javadoc)
		 * @see com.ccg.ds.Table#rowIterator(int)
		 */
		public ListIterator rowIterator(int row)
		{
				if(row<0 || row>=records.size())
					throw new IndexOutOfBoundsException("Specified row index is out of bounds.");
				
			Object record[]=(Object[])records.get(row);
			return new RowIterator(record);
		}

		/* (non-Javadoc)
		 * @see com.ccg.ds.Table#columnIterator(int)
		 */
		public ListIterator columnIterator(int col)
		{
				if(col<0 || col>=columnSize)
					throw new IndexOutOfBoundsException("Column index is out of bounds: "+col);
			return new ColumnIterator(records,col);
		}

		/* (non-Javadoc)
		 * @see com.ccg.ds.Table#rowIterator(int, int)
		 */
		public ListIterator rowIterator(int row,int from)
		{
				if(row<0 || row>=records.size())
					throw new IndexOutOfBoundsException("Specified row index is out of bounds.");
				
				if(from<0 || from>=columnSize)
					throw new IndexOutOfBoundsException("Specified from index is out of bounds: "+from);
				
			Object record[]=(Object[])records.get(row);
			return new RowIterator(record,from);
		}

		/* (non-Javadoc)
		 * @see com.ccg.ds.Table#columnIterator(int, int)
		 */
		public ListIterator columnIterator(int col,int from)
		{
				if(col<0 || col>=columnSize)
					throw new IndexOutOfBoundsException("Column index is out of bounds: "+col);
				
				if(from<0 || from>=records.size())
					throw new IndexOutOfBoundsException("From index is out of bounds: "+from);
				
			return new ColumnIterator(records,col,from);
		}
}

/**
 * <BR><BR>
 * A simple ListIterator implementation, for iterating throught the column data of a row in the table.
 * <BR>
 * @author A. Kranthi Kiran
 */
class RowIterator implements ListIterator
{
	private Object row[];
	private int idx=-1;
	private int prevIdx=-1;
	
		public RowIterator(Object row[])
		{
			this.row=row;
		}
		
		public RowIterator(Object row[],int from)
		{
			this.row=row;
			idx=from;
		}
		
		public boolean hasNext()
		{
			return (idx<row.length-1);
		}
	
		public Object next()
		{
				if(idx>=row.length-1)
					throw new NoSuchElementException("Next element is not available.");
			
			prevIdx=++idx;
			return row[prevIdx];
		}
	
		public boolean hasPrevious()
		{
			return (idx>=0);
		}
	
		public Object previous()
		{
				if(idx<0)
					throw new NoSuchElementException("Previous element is not available.");
			prevIdx=idx--;
			return row[prevIdx];
		}
	
		public int nextIndex()
		{
			return idx+1;
		}
	
		public int previousIndex()
		{
			return idx;
		}
	
		public void remove()
		{
			throw new UnsupportedOperationException("Remove method is not supported by table iterators");
		}
	
		public void set(Object value)
		{
			throw new UnsupportedOperationException("Set method is not supported by table iterators");
		}
	
		public void add(Object value)
		{
			throw new UnsupportedOperationException("Add method is not supported by table iterators");
		}
}

/**
 * <BR><BR>
 * A Simple implementation of ListIterator to iterate through the row data in a column of the table.
 * <BR>
 * @author Administrator
 */
class ColumnIterator implements ListIterator
{
	private int col;
	private ListIterator iterator;
	private Object prevCol[];
		public ColumnIterator(ArrayList list,int column)
		{
			col=column;
			iterator=list.listIterator();
		}
	
		public ColumnIterator(ArrayList list,int column,int from)
		{
			col=column;
			iterator=list.listIterator(from);
		}
		
		public boolean hasNext()
		{
				try
				{
					return iterator.hasNext();
				}catch(ConcurrentModificationException ex)
				{
					throw new ConcurrentModificationException("Source table is modified.");
				}
		}
	
		public Object next()
		{
			try
			{
				prevCol=(Object[])iterator.next();
				return prevCol[col];
			}catch(ConcurrentModificationException ex)
			{
				throw new ConcurrentModificationException("Source table is modified.");
			}
		}
	
		public void remove()
		{
			throw new UnsupportedOperationException("Remove method is not supported by table iterators.");
		}

		public boolean hasPrevious()
		{
			try
			{
				return iterator.hasPrevious();
			}catch(ConcurrentModificationException ex)
			{
				throw new ConcurrentModificationException("Source table is modified.");
			}
		}

		public Object previous()
		{
			try
			{
				prevCol=(Object[])iterator.previous();
				return prevCol[col];
			}catch(ConcurrentModificationException ex)
			{
				throw new ConcurrentModificationException("Source table is modified.");
			}
		}

		public int nextIndex()
		{
			try
			{
				return iterator.nextIndex();
			}catch(ConcurrentModificationException ex)
			{
				throw new ConcurrentModificationException("Source table is modified.");
			}
		}

		public int previousIndex()
		{
			try
			{
				return iterator.previousIndex();
			}catch(ConcurrentModificationException ex)
			{
				throw new ConcurrentModificationException("Source table is modified.");
			}
		}

		public void set(Object value)
		{
			throw new UnsupportedOperationException("Ser method is not supported by table iterators.");
		}

		public void add(Object value)
		{
			throw new UnsupportedOperationException("Add method is not supported by table iterators.");
		}
}

/**
 * <BR><BR>
 * A simple table iterator implementation for the array table. 
 * <BR>
 * @author A. Kranthi Kiran
 */
class ListTableIterator implements TableIterator
{
	List rows;
	Object curRecord[];
	private ListIterator rowIterator;
	int rowNo=0;
	int colNo=0;
	int rowCount=0;
	int colCount=0;
	
		public ListTableIterator(List rows,int colSize)
		{
			this.rows=rows;
				if(rows!=null)
				{
					rowIterator=rows.listIterator();
					rowCount=rows.size();
					colCount=colSize;
				}
		}
		
		public ListTableIterator(List rows,int rowNo,int colNo)
		{
			this.rows=rows;
			rowIterator=rows.listIterator(rowNo);
			curRecord=(Object[])rowIterator.next();
			colCount=curRecord.length;
			this.rowNo=rowNo+1;
			this.colNo=colNo;
			
			rowCount=rows.size();
		}
		
		public boolean hasNextRow()
		{
			return (rowNo<rowCount);
		}
		
		public boolean hasNextColumn()
		{
			return (colNo<colCount);
		}
		
		public boolean nextRow()
		{
				if(rowNo>=rowCount)
					return false;
			curRecord=(Object[])rowIterator.next();
			rowNo++;
			colNo=0;
			return true;
		}
		
		public Object nextColumn()
		{
				if(colNo>=colCount)
					throw new NoSuchElementException("No column element found.");
			return curRecord[colNo++];
		}
}