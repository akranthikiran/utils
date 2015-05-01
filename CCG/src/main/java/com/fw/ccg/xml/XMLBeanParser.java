package com.fw.ccg.xml;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;

import org.xml.sax.SAXException;


/**
 * © Copyright 2006 IBM Corporation
 * <BR><BR>
 * XMLBeanParser maps XML file data to a bean or in short generates a bean from XML data. XMLBeanParser works
 * with a simple logic/assumption that entire XML file represents a single bean called <B>"root bean"</B> 
 * and all sub-nodes and attributes are the properties of that root bean.
 * <BR><BR>
 * 
 * 
 * <U><B>Types of Nodes</B></U>
 * <BR><BR>
 * XMLBeanParser does not support meta-text nodes. A node having text and sub nodes in its
 * body.  
 * While loading XML file, XMLBeanParser groups/treats the XML nodes to be in one of the
 * follwoing group as per the description given below:
 * <OL>
 * 		<LI>
 * 			<B>Standard Node:</B> A node with/without attributes and with/without sub nodes.
 * 			<BR>
 * 			This type of nodes represent a bean and all the node attributes and sub-nodes
 * 			will be mapped to this bean properties (setters and adders).
 * 			<BR><BR>
 * 			Note: Reserved attributes (described below) will not be considered while 
 * 			determining, a node as standard node.   
 * 		</LI>
 * 		<LI>
 * 			<B>Text Node:</B> A node which has only text (or CDATA section) in its 
 * 			body and doesnt have any standard attributes. White spaces at the starting 
 * 			and end will get trimmed. Even if the text spans multiple lines, whitespaces
 * 			will be trimmed only at starting and ending of the text. Intermidiate 
 * 			whitespaces will not be effected. 
 * 			<BR>Eg., 
 * 					&lt;Query&gt;
 * 						Select * from emp
 * 					&lt;/Query&gt;
 * 			<BR><BR>
 * 			This type of node represents a property of the bean 
 * 			representing immediate enclosing parent node. For example, setQuery(String)
 * 			<BR> 
 * 			Note: Reserved attributes (described below) will not be considered while 
 * 			determining, a node as text node.   
 *		</LI>
 *		<LI>
 *			<B>ID-Based Text Node:</B>  A node which is having only text (or CDATA section)
 *			in its body and have only one single standard attribute (with any name). The text 
 *			will be trimmed just like normal text nodes. 
 *			<BR>Eg., 
 *					&lt;skill name="java"&gt;
 *						5
 *					&lt;/skill&gt;
 * 			<BR><BR>
 * 			This type of node represents a ID based property (or mapping property) of the bean 
 * 			representing immediate enclosing parent node. For example, setSkill(String,int)
 * 			<BR> 
 * 			Note: Reserved attributes (described below) will not be considered while 
 * 			determining, a node as ID based text node.   
 *		</LI>
 *		<LI>
 * 			<B>Meta Text Node:</B> A node which is having combination of text and sub-nodes
 * 			in its body. Or mutiple standard attributes and with text in its body.
 * 			<BR>
 * 			<B>This type of nodes are not supported by XMLBeanParser.</B>
 * 		</LI> 
 * </OL>
 * <B>Note:</B> Even though meta-text nodes are not supported by XMLBeanParser, XMLBeanParser 
 * will not consider trailing whitespaces (generally used for indentation) as part of text.
 * 
 * 
 * <BR><BR>
 * <B><U>Reserve Nodes and Attributes</B></U>
 * <BR><BR>
 * All the nodes and attributes which are in "/fw/ccg/XMLBeanParser" namespace 
 * are treated as reserve nodes and attributes respectively. The loading of these reserve 
 * nodes or using of reserved attributes are expected to be taken care by Parser Handler.
 * 
 * </BR></BR>
 * <U><B>Mapping XML Data to Bean Properties</B></U>
 * <BR><BR>
 * In order to design XML files that can be parsed by Bean parser its very important to 
 * understand the logic how XML elements gets mapped to bean properties. Following 
 * points describes how XML elements are mapped to beans and their properties:
 * <BR><BR>
 * <OL>
 * 		<LI>
 * 			<B>Node and SubNodes:</B>The root of the XML data is represented by the root 
 * 			bean. For each subnode under this node, setter/adder will be searched in root 
 * 			bean which matches with the subnode name and which takes single attribute. If 
 * 			both adder and setter are present then adder will be used. If multiple subnodes 
 * 			are present then the selected adder/setter will be called multiple times.
 * 			<BR>For example,<BR>
 * 			&lt;Query&gt;&lt;select&gt;
 * 			<BR>....................<BR>
 * 			&lt;/select&gt;&lt;/Query&gt;<BR><BR>
 *  
 *    		Say in the above example if Query is the root of the XML, then under root bean setSelect() and 
 *    		addSelect() will be searched and lets say there is a method called addSelect(SelectQuery qry) in the
 *    		root bean. Then an instanceof SelectQuery will be created and the same process is repeated for 
 *    		&lt;select&gt; node and the resultant bean is passed to addSelect()
 *    		of root bean.
 *    		<BR>
 *    		Thus for nested subnodes beans will be created and gets assigned to the bean representing enclosing
 *    		node.
 * 		</LI>
 * 		<LI>
 * 			<B>Text Node:</B> Text node is also processed in the same way as subnode but the 
 * 			paramter accepted by appropriate setter/adder should be one of the supported 
 * 			attribute types mentioned below.
 * 		</LI>
 * 		<LI>
 * 			<B>Attributes:</B> For each attribute setter (only) will be searched which 
 * 			matches with the name of the attribute. The parameter type of the matching 
 * 			setter can be one of the supported attribute types mentioned below.
 * 		</LI>
 * </OL>
 * 
 * 
 * <BR><BR>
 * <B><U>Supported Attribute Types</B></U>
 * <BR><BR>
 * The current version of the XMLBeanParser supports following data types for attributes
 * and text based sub-nodes (both normal and ID based). In ID-based text nodes, both the id and
 * the value can be one of the following types.
 * <OL>
 * 		<LI>Primitive Types</LI>
 * 		<LI>Primitive Wrapper classes</LI>
 * 		<LI>java.lang.String</LI>
 * 		<LI>java.lang.StringBuffer</LI>
 * 		<LI>java.util.Date</LI> 
 * </OL>
 * <BR>
 * Note: If multiple version of methods (overloaded) are available in the bean with above
 * types, then the selection of method for that property can not be determined.
 * <BR><BR>
 * @author A. Kranthi Kiran
 */
public class XMLBeanParser
{
	private SAXEventHandler saxHandler;
		private Object parseXML(InputStream xmlInput,Object toBean,ParserHandler handler,Schema schema)
		{
			try
			{
				handler=(handler==null)?new DefaultParserHandler():handler;
				
				saxHandler=new SAXEventHandler(handler,toBean);
				SAXParserFactory saxFactory=SAXParserFactory.newInstance();
					if(schema!=null)
						saxFactory.setSchema(schema);
					else
						saxFactory.setValidating(true);
				
				saxFactory.setNamespaceAware(true);
				handler.setParser(this);
				
				saxFactory.newSAXParser().parse(xmlInput,saxHandler);
				return saxHandler.getRootBean();
			}catch(CCGInternalException ex)
			{
				return null;
			}catch (SAXException e)
			{
				throw new XMLLoadException("Error in reading XML Stream.",e);
			}catch (IOException e)
			{
				throw new XMLLoadException("Error in reading XML Stream.",e);
			}catch (ParserConfigurationException e)
			{
				throw new XMLLoadException("Error in reading XML Stream.",e);
			}//other than the above exceptions, the exceptions thrown inside 
				//other methods should not be caught
		}
		
		public void stopProcessing()
		{
			saxHandler.stopProcessing();
		}
		
		/**
		 * This method call is equivalent to calling parse(xmlInput,toBean,null,null)
		 * @param xmlInput
		 * @param toBean
		 * @return
		 */
		public static Object parse(InputStream xmlInput,Object toBean)
		{
			return parse(xmlInput,toBean,null,null);
		}
		
		/**
		 * This method call is equivalent to calling parse(xmlInput,toBean,null,schema)
		 * @param xmlInput
		 * @param toBean
		 * @param schema
		 * @return
		 */
		public static Object parse(InputStream xmlInput,Object toBean,Schema schema)
		{
			return parse(xmlInput,toBean,null,schema);
		}
		
		/**
		 * This method call is equivalent to calling parse(xmlInput,toBean,handler,null)
		 * @param xmlInput
		 * @param toBean
		 * @param handler
		 * @return
		 */
		public static Object parse(InputStream xmlInput,Object toBean,ParserHandler handler)
		{
			return parse(xmlInput,toBean,handler,null);
		}
		
		/**
		 * This method call is equivalent to calling parse(xmlInput,null,null,null)
		 * @param xmlInput
		 * @return
		 */
		public static Object parse(InputStream xmlInput)
		{
			return parse(xmlInput,null,null,null);
		}
		
		/**
		 * This method call is equivalent to calling parse(xmlInput,null,handler,null)
		 * @param xmlInput
		 * @param handler
		 * @return
		 */
		public static Object parse(InputStream xmlInput,ParserHandler handler)
		{
			return parse(xmlInput,null,handler,null);
		}
		
		/**
		 * This method call is equivalent to calling parse(xmlInput,null,handler,schema)
		 * @param xmlInput
		 * @param handler
		 * @param schema
		 * @return
		 */
		public static Object parse(InputStream xmlInput,ParserHandler handler,Schema schema)
		{
			return parse(xmlInput,null,handler,schema);
		}
		
		/**
		 * <P>
		 * Loads the specified XML file to the specified root bean. If root bean specified is 
		 * null, then specified handlers's createRootBean() is called to create root bean.
		 * <BR>
		 * If the handler is not specified, a new instance of DefaultParserHandler 
		 * (com.ccg.xml.DefaultParserHandler) will be created and used as handler.
		 * </P>
		 * <P>
		 * Paramter toBean is expected to have adders/setters that are needed to load 
		 * specified XML file.Object toBean represents root of the input XML data.<BR>
		 * 
		 * This method uses default handler (BeanParserHandler) to load data from XML stream. The defualt handler 
		 * behaviour can be found in this class level documentation.
		 * </P>
		 * <P>
		 * Note: If schema is null, then validating is set enabled. In other words DTD
		 * validation will be done, if specified in XML.
		 * </P>
		 * @param xmlInput XML input stream from which XML data needs to be loaded.
		 * @param toBean Bean to which data from XML needs to be loaded.
		 * @param handler Handler factory instance to support loading of XML data.
		 * @param schema Schema that needs to be used to validate input XML (optional).
		 * @return The root bean represnting the XML data.
		 */
		public static Object parse(InputStream xmlInput,Object toBean,ParserHandler handler,Schema schema)
		{
			return new XMLBeanParser().parseXML(xmlInput,toBean,handler,schema);
		}
	
}

