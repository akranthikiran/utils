package com.fw.ccg.manager;

import com.fw.ccg.xml.XMLAttributeMap;

/**
 * © Copyright 2006 IBM Corporation
 * <P>
 * This is a supporting interface for the CCG manager building process.
 * </P>
 * <P>
 * Manager Handler is meant for those applications which need control 
 * on their manager building. For, example, support of manager specific reserve 
 * nodes, etc. The bean factory can be specified in the XML data itself (in the root node), 
 * which is passed as input to the manager builder.
 * </P> 
 * <BR>
 * @author A. Kranthi Kiran
 */
public interface ManagerHandler
{
	/**
	 * Once the manager bean is built, this method will be called on the handler.
	 * Note by this time none of the properties are loaded to the manager.
	 * <BR><BR>
	 * @param manager  Manager being built
	 */
	public void init(Object manager,ManagerContext context);
	
	/**
	 * Invoked at the start of noraml nodes. This method is passed with newly built bean
	 * representing the node. This method is expected to process the reserve attributes 
	 * (if any) and update the bean appropriately. If the return value is not null, then
	 * the return value will replace the bean built to represent the current node.<BR>
	 * Note, the return type of this method (if not null) should be compatible with the
	 * type expected for this node.
	 * @param nodeName Name of the current node.
	 * @param bean Bean representing current node.
	 * @param parent Parent bean, representing the enclosing node of the current node.
	 * @param type Expected type of the bean.
	 * @param att Attributes specified in the current XML node.
	 * @return Bean represnting current bean (can be null).
	 */
	public Object processBean(ManagerNode node,XMLAttributeMap att);
	
	/**
	 * Called when a reserve node is encountered which is not supported by manager builder.
	 * If the reserve node is not supported, then this method should throw exception with
	 * appropriate message.
	 * <BR><BR>
	 * @param nodeName Name of the current reserve node.
	 * @param parent Bean representing parent node.
	 * @param att Attributes specified in the XML node.
	 * @return Bean representing the reserve node. (can be null for empty or text-based
	 * 			reserve nodes. 
	 */
	public Object processReserveNode(ManagerNode node,XMLAttributeMap att);
	
	/**
	 * To notify a reserve node end (not supported by the builder) has been encountered.
	 * <BR><BR>
	 * @param nodeName Name of the current reserve node.
	 * @param parent Bean representing parent node.
	 * @param bean Bean created by processReserveNode() (if any). For text based reserve nodes
	 * 				this will text specified in the xml node body i.e., java.lang.String object.
	 * @param att Attributes specified in the XML node.
	 */
	public void processReserveNodeEnd(ManagerNode node,XMLAttributeMap att);
	
	/**
	 * Once the bean is loaded, this method will be called. Note if the bean is of type 
	 * com.ccg.core.Validateable, then first validate() method is called over the bean 
	 * then this method will be called.
	 * <BR><BR>
	 * @param nodeName Name of the current reserve node.
	 * @param parent  Bean representing parent node.
	 * @param bean Bean representing current node.
	 */
	public void validateBean(ManagerNode node);
}
