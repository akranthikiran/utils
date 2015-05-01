package com.fw.dao.qry;

import java.util.Map;

/**
 * QueryFilter is used during parsing of queries and is responsible for
 * 	<OL>
 * 		<LI> Determining which parts of the queries to be included/excluded based on the input data.
 * 	</OL> 
 *   
 */
public interface QueryFilter
{
	/**
	 * Reject Flag: Ignores the current node and its contents.
	 */
	public int REJECT=0;
	/**
	 * Replace Flag: Replaces the current node and its contents with the value returned by {@link #getReplaceString(Map, int)}
	 */
	public int REPLACE=1;
	/**
	 * Accept Flag: Contents of the current node will be added to the final query.
	 */
	public int ACCEPT=2;
	/**
	 * Next Phase Flag: Simply indicates parser that this node can be processed only in next phase
	 */
	public int NEXT_PHASE=3;
	/**
	 * Process Collection Flag: Simply indicates parser that this node has to be executed for each element of result collection
	 */
	public int PROCESS_COLLECTION = 4;
	
	/**
	 * This method will be invoked by parser for every XML node inside the query.
	 * And this method is responsible for including/excluding/replacing the content of the current node.
	 * @param name Name of the current XML node
	 * @param nodeAttr Attributes as a map which are specified in the current node
	 * @param phaseNo Parsing phase number
	 * @return One of the flags in this (QueryFilter) interface.
	 */
	public FilterResult accept(String name,Map<String,String> nodeAttr,int phaseNo);
	
	/**
	 * Will be called by parser to get replacement string for the current node when accept() method returns
	 * REPLACE flag.
	 * 
	 * @param name Name of the current XML node
	 * @param nodeAttr Attributes as a map which are specified in the current node
	 * @param phaseNo Parsing phase number
	 * @return String replacement for the current node.
	 */
	public String getReplaceString(String name,Map<String,String> nodeAttr,int phaseNo);
	
	/**
	 * Invoked by parser to fetch property value referred in the query.
	 * <BR/><B>Example</B>: For ${id}, this method will be invoked by parsing "id" as "name" parameter
	 *  
	 * @param name Property name for which value needs to be fetched 
	 * @return Property value specified by "name"
	 */
	public Object getProperty(String name);
	
	/**
	 * A string representation of null value. 
	 * @return String representation of null value
	 */
	public String getNullString();
	
	/**
	 * Similar to {@link #getProperty(String)}, but this method will be called when the property value
	 * is needed as part of function invocation.
	 * @param funcName Function name in which current property is referenced.
	 * @param propName Name of property
	 * @return
	 */
	public Object getProperty(String funcName,String propName);
}
