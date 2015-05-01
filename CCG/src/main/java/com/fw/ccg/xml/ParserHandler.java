package com.fw.ccg.xml;

import com.fw.ccg.core.ValidateException;


/**
 * © Copyright 2006 IBM Corporation
 * <BR><BR>
 * <P>
 * Handler interface (factory sum builder helper model) for the CCG Bean XML parser. 
 * This interface is used to support customizable behaviour of CCG parser and represents
 * helper factory/builder model for XMLBeanParser.
 * </P>
 * <P>
 * This interface objects controls how the beans are created, how reserve nodes/attributes
 * to be treated, specifies descriptions for nodes & beans which will be used for 
 * debugging purposes and also specified date format to be used while parsing date objects.
 * </P> 
 * <BR>
 * @author A. Kranthi Kiran
 */
public interface ParserHandler extends XMLConstants
{
	public static Object NOT_SUPPORTED=new Object();
	
	public void setParser(XMLBeanParser parser);
	/**
	 * This method will be called only the rootBean is specified in parse() method of 
	 * XMLBeanParser. 
	 * @param bean Created root bean.
	 * @param att Attributes specified in the root node.
	 */
	public void setRootBean(BeanNode node,XMLAttributeMap att);
	
	/**
	 * This is the first method that will be called and this method is expected 
	 * to create the root bean and return the same. This method is called only
	 * if root bean is not specified in parse() of XMLBeanParser.
	 * <BR>
	 * @param rootName Name of the root node.
	 * @param att Attributes specified in the root node.
	 * @return A bean representing the root node of the input XML data.
	 */
	public Object createRootBean(BeanNode node,XMLAttributeMap att);
	
	/**
	 * Whenever there is a node start, this method will be called and node name, 
	 * all the attributes (as XMLAttributeMap) are passed as parameters to this method. 
	 * Depending on the setter/adder that’s been chosen for this node, the type of bean 
	 * being expected is also passed as parameter. This method is expected to return 
	 * a bean representing the current node, whose type should be compatible/assignable to 
	 * the type of bean specified as argument.
	 * <BR>
	 * @param nodeName Name of the current node
	 * @param parent Parent bean, that is the bean representing the immediate enclosing node.
	 * @param type Expected type of the bean, that is going to represent this node.   
	 * @param att Attributes specified in this node.
	 * @return A bean representing the current node and also which is compatible with type.
	 */
	public Object createBean(BeanNode node,XMLAttributeMap att);
	
	/**
	 * This method will be used to determine the dynamic type for the current node.
	 * 
	 * The type returned by this method should match with the type of the corresponding parent
	 * bean setter/adder.
	 * 
	 * @param node
	 * @param att
	 * @return
	 */
	public Class<?> getDynamicBeanType(BeanNode node,XMLAttributeMap att);
	
	public Object parseTextNodeValue(BeanNode node,XMLAttributeMap att);
	
	public Object createAttributeBean(BeanNode node,String attName,Class<?> type);
	
	/**
	 * Whenever there is a node end, this method will be called. Since, this method is 
	 * called at the end of the node there will be no setter and adder methods invocation 
	 * after this point. This method is expected to validate the bean, for example, check if all 
	 * the mandatory data is specified or not. If data provided is not valid or sufficient, 
	 * this method is expected to throw ValidateException with a proper message. The exception will 
	 * be wrapped with XMLLoadException and adds the node XML path at where exception is 
	 * thrown and then this new XMLLoadException will be thrown.
	 * <BR>
	 * @param nodeName Name of the current node
	 * @param parent Parent bean, that is the bean representing the immediate enclosing node.
	 * @param bean Bean representing the current node.
	 */
	public void validateBean(BeanNode node) throws ValidateException;
	
	
	/**
	 * <P>
	 * This method will be called at start of each node. Generally the return string value
	 * is used to represent the current node (when exception is thrown).  
	 * </P>
	 * <P>
	 * Other than above mentioned usage, this method is capable of making XMLBeanParser to
	 * skip/ignore the current node and all its sub nodes. The node being skipped can be a
	 * standard node/reserve node. For skipping a node, this method should return constant 
	 * SKIP_NODE_ELEMENT.
	 * </P>
	 * <BR>
	 * @param nodeName Name of the current node.
	 * @param att Attributes of the current node.
	 * @param reserved Flag indicating whether current node is a reserve node or normal node.
	 * @return String describing th current node.
	 */
	public String getNodeDescription(BeanNode node,XMLAttributeMap att);
	
	/**
	 * Fetch a description text for the specified bean. Note, this method may be called
	 * at any time. Before bean is completely loaded or when loaded partially. 
	 * <BR>
	 * @param bean Bean for which description is needed.
	 * @return Description representiung the bean.
	 */
	public String getBeanDescription(Object bean);
	
	/**
	 * This method is invoked when a date property is encountered. The format returned by 
	 * this method is used to parse the date string mentioned in XML to date object. The 
	 * date format returned by this method should be according to the java.text.SimpleDateFormat 
	 * class standard.
	 * <BR>
	 * @return Date format to be used to parse dates.
	 */
	public String getDateFormat();
	
	/**
	 * This method is called when a start of reserved node is encountered. If the reserved 
	 * node is expected to have sub nodes then this method should return a bean representing 
	 * the reserve node. Otherwise the reserved node is a <B><I>null-based reserve node</I></B> 
	 * and these nodes should not contain sub nodes. A non-bean reserve node body should be 
	 * either empty or text.
	 * <BR>
	 * 
	 * @param nodeName Reserve node name
	 * @param parent Parent of the current reserve node.
	 * @param att Attributes of the current reserve node
	 * @return An object representing reserved node. This value can be null for non-bean reserved
	 * 		   nodes.
	 */
	public Object processReservedNode(BeanNode node,XMLAttributeMap att);
	
	/**
	 * This method will be invoked when the end of reserved node is reached. If the reserve 
	 * node is not a null-based reserved node, then the bean created during processReservedNode() 
	 * will be passed as argument to this method. Subnodes of the bean based reserve 
	 * nodes will be treated as normal sub-nodes but the considering reserve node bean as 
	 * parent bean.
	 * 		If the current reserve node is a null-based text node, then the text specified 
	 * in the node-body is passed as bean argument to this method.
	 * <BR>
	 * 
	 * @param nodeName Name of the current reserve node.
	 * @param parent Bean representing the parent node.
	 * @param bean For null-based reserved nodes this value will be null. For bean based reserve 
	 * 				node this will be value returned by corresponding processReservedNode() method. 
	 * 				This value will be String object for null-based text reserve nodes.  
	 * @param att Attributes specified in the reserve node start. 
	 */
	public void processReserveNodeEnd(BeanNode node,XMLAttributeMap att);
	
	public BeanFactory getBeanFactory(Class<?> type);
	
	public String getConstantValue(String name);
	
	public String processText(Object rootBean, String text);
}
