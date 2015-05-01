package com.fw.ccg.manager;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.fw.ccg.xml.XMLBeanParser;

/**
 * © Copyright 2006 IBM Corporation
 * <BR><BR>
 * <P>
 * ManagerBuilder helps in building highly efficient and configurable <B><I>"Configuration 
 * Manager(s)"</I></B> from the abstract definitions (interfaces) of the manager provided by the 
 * applications. ManagerBuilder accepts XML resource name(s)/stream(s) as input and expects
 * all the configuration details including abstract manager definition(s) itself to be in the
 * input XML data.
 * </P>
 * 
 * <P>
 * ManagerBuilder in turn uses "CCG XML Bean Parser" but does not support ID-based adders. 
 * Just like "XML Bean Parser", this class also provides a handler (com.ccg.manager.ManagerHandler)
 * which can be used to customize the XML loading and provide additional reserve node/attribute
 * definitions. Unlike "XML Bean Parser", the dynamic manager being consructed also 
 * support getters also, ofcourse with few restrictions.
 * </P>
 * 
 * <P>
 * The resultant "Configuration Manager" itself can be configured in the input XML data.
 * CCG proposes certain standards for the "Configuration Managers", whcih in turn needs to
 * be followed by the application specific manager abstract definitions.Following is the 
 * list of standards to be maintained by CCG compatible managers:
 * </P>
 * 		<OL>
 * 			<LI><B>Abstractness:</B> Manager itself should not be a class, it should be a 
 * 				public interface(s) and no implementations of this interface(s)
 * 				needs to be provided. In fact, a dynamic object will be built by CCG 
 * 				using these interface(s).
 *			</LI>
 * 			<LI><B>Setters and Getters:</B> The interface can have standard setter and getter 
 * 				properties of any type.
 *			</LI>
 * 			<LI><B>Adders and Getters:</B> The adders and getters proposed by CCG are little 
 * 				different from standard ones. Here adder will have only one parameter whose 
 * 				type should be same as corresponding getter return type. And getter should 
 * 				take a String parameter which is actually an ID for the bean to be 
 * 				fetched, which inturn is specified in XML. Note, adder doesn’t take any ID 
 * 				parameter, the ID’s are maintained internally by dynamic objects built by CCG.
 *			</LI>
 *		</OL>
 * <BR>
 * 
 * <P>
 * Even though the dynamic manager constructed supports only above mentioned standard methods,
 * the behaviour of the other methods present in manager definition(s) can be customized using
 * below mentioned reserve nodes.
 * </P>
 * 
 * 
 * <BR>
 * <B><U>Reserve Node(s) and Attribute(s)</U></B>
 * <P>
 * Some of the reserve nodes supported by ManagerBuilder are XML specific, some are 
 * root node specific and some are for method behaviour customizations. So the reserve nodes
 * and attributes supported by this class can be grouped and described as follows:
 * 
 * 
 * <BR><BR>
 * <B> First XML resource specific reserve attributes:</B>
 * <P>
 * As mentioned earlier ManagerBuilder supports loading/building of "Configuration Manager"
 * from multiple XML files. Among these, the first XML resource/stream hold a critical
 * information such as the manager abstract definitions and specifications for undleying
 * caching mechanism. <I><B>All the below mentioned reserve attributes should be present
 * only in the root node of the first XML resource. Otherwise they will be simply 
 * ignored.<B></I>
 * </P>
 * 
 * <TABLE BORDER="1">
 * 		<TR>
 * 			<TH>Reserve Attribute</TH>
 * 			<TH>Usage</TH>
 * 		</TR>
 * 		<TR>
 * 			<TD>beanType</B>
 * 			<TD>
 * 				This value holds the abstract definition of manager(s). That is, this should
 * 				have list of comma(,) separated public interface name(s) which represents the 
 * 				the definitions for application specific manager.
 * 			</TD>
 * 		</TR>
 * 		<TR>
 * 			<TD>useCache</B>
 * 			<TD>
 * 				This value shuld be true or false. Which indicates whether a caching 
 * 				mechanism to be used by the dynamic manager built.
 * 				<BR>
 * 				<B><I>By default this value is false.</I></B>
 *      	</TD>
 *     	</TR>
 * 		<TR>
 * 			<TD>cacheType</B>
 * 			<TD>
 * 				Specifies the cache mechanism to be used. This value should hold the 
 * 				cache (com.ccg.util.Cache) implementation class name.
 * 				<BR>
 * 				<B><I>If this value is not specified and "useCache" is specified to be 
 * 				true, then "Time Based Cache Map" (com.ccg.ds.TimeBasedCacheMap) will be
 * 				used.</B></I>
 *      	</TD>
 *     	</TR>
 * 		<TR>
 * 			<TD>cacheInterval</B>
 * 			<TD>
 * 				<B><I>Used only if "cacheType" is not specified.</I></B>
 * 				<BR>
 * 				
 * 				Specifies time interval for the cache mechanism. If the beans (serializable 
 * 				only) in the memory are not used for this duration, they will get auto 
 * 				cached. 
 * 				<BR>
 *  			<B><I>By default this value is equal to TimeBasedCacheMap.DEFAULT_TIME_GAP
 *  			</B></I>
 * 			</TD>
 *     	</TR>
 * 		<TR>
 * 			<TD>cachePath & cacheFile</B>
 * 			<TD>
 * 				<B><I>
 * 					Used only if "cacheType" is not specified. These values are considered
 * 					only if both the values are specified.
 * 				</I></B>
 * 				<BR>
 * 				<B>cachePath:</B> Specifies the directory where the catch file should be 
 * 				maintained.
 * 				<BR>
 * 				<B>cacheFile:</B> Name to be used for cache files. Two cache files will
 * 				be maitained with extensions .idx and .dat 
 * 				<BR>
 * 				<B><I>
 * 				If these values are not specified then cache files will be created in the
 * 				undelying OS specific temporary directory with OS specific temporary file
 * 				names.
 * 				</I></B>
 *			</TD>
 *     	</TR>
 * 		<TR>
 * 			<TD>forceCache</B>
 * 			<TD>
 * 				<B><I>Used only if "cacheType" is not specified.</I></B>
 * 				<BR>
 * 				This value can be true or false. If this value is true, once the XML 
 * 				resource(s) is completed, then all the data (serializable only) will 
 * 				be forced into cache and will be loaded on need-basis and cycle continues.
 * 				<BR> 
 *		 		<B><I>By default, this value is false.</I></B>
 *			</TD>
 *		</TR>
 * </TABLE>
 * 
 * <BR><BR>
 * <B>XML Resource Specific Reserve Attributes</B>
 * <P>
 * Following reserve attributes are specific to the XML Resource in which these attributes 
 * are defined. And these values will be effective only till that XML resource is loaded.
 * <B><I>Again all the attributes mentioned below should be used in root node only.</I><B>
 * </P>
 * 
 * <TABLE BORDER="1">
 * 		<TR>
 * 			<TH>Reserve Attribute</TH>
 * 			<TH>Usage</TH>
 * 		</TR>
 * 		<TR>
 * 			<TD>dateFormat</TD>
 * 			<TD>
 * 				The date format to be used to parse XML fields in this XML file. The format
 * 				being specified in this attribute should be in java.text.SimpleDateFormat
 * 				standard.
 * 				<BR>
 * 				<B><I>By default "MM/dd/yyyy" format will be used.</I></B>  
 * 			</TD>
 * 		</TR>
 * 		<TR>
 * 			<TD>managerHandler</TD>
 * 			<TD>
 * 				"Manager Hanlder" (com.ccg.manager.ManagerHandler) implementation class name 
 * 				that needs to be used to customize the XML data loading from this 
 * 				resource.
 * 			</TD>
 * 		</TR>
 * 		<TR>
 * 			<TD>methodProxy</TD>
 * 			<TD>
 * 				"Method Proxy" (com.ccg.manager.ManagerMethodProxy) implementation class name
 * 				that needs to be used for proxy defined in this XML file. Again this will 
 * 				be effective only for this XML.
 * 			</TD>
 * 		</TR>
 * </TABLE>
 * 
 * 
 * <BR><BR>
 * <B>Node Specific Reserve Attributes</B>
 * <P>
 * <TABLE BORDER="1">
 * 		<TR>
 * 			<TH>Reserve Attribute</TH>
 * 			<TH>Usage</TH>
 * 		</TR>
 * 		<TR>
 * 			<TD>beanType</TD>
 * 			<TD>
 * 				The type (class name) of the bean to be created to represent current node.<BR>
 * 				Note: the type specified by this attribute should be compatible with the type 
 * 				of the matching setter or adder.
 * 			</TD>
 * 		</TR>
 * 		<TR>
 * 			<TD>paramTypes</TD>
 * 			<TD>
 * 				<B><I>If this attribute is used, then reserve attribute "params" becomes mandatory.</I></B>
 * 				<BR> 
 * 				This attribute can be used to force the handler to use only the constructor which takes 
 * 				specified argument types.<BR>
 * 				The argument types should be separated by comma (,) and should be of XMLBeanParser supported 
 * 				attribute types.
 * 				<BR>
 * 				<B><I>By default, default constructor will be used.</I></B>
 * 			</TD>
 * 		</TR>
 * 		<TR>
 * 			<TD>params</TD>
 * 			<TD>
 * 				<B><I>Ignored if reserve attribute "paramTypes" is not used.</I></B>
 * 				<BR>
 * 				This specifies the values to be passed to the constructor. The values should be separated
 * 				by comma.
 * 			</TD>
 * 		</TR>
 * 		<TR>
 * 			<TD>expression</TD>
 * 			<TD>
 * 				It is a fully specified public static field name. The field indicated by this 
 * 				expression in the whole should be accessible statically (that is without any
 * 				need of object creation).
 * 				<BR>
 * 				For example, java.lang.System.out is an expression of type 
 * 				java.io.PrintStream.
 * 			</TD>
 * 		</TR>
 * 		<TR>
 * 			<TD>id</TD>
 * 			<TD>
 * 				This is a mandatory reserve attribute for adder based nodes. This "ID" value should
 * 				be used as parameter to the corresponding getter to fetch the bean representing the
 * 				current node.
 * 			</TD>
 *		</TR>
 * 		<TR>
 * 			<TD>default</TD>
 * 			<TD>
 * 				This value can be either true or false and can be used only with adder based nodes. If 
 * 				this value is true, the bean represnting the current node will be marked as default bean.
 * 				When the corresponding getter is called by passing null id, this value will be returned. 
 * 			</TD>
 * 		</TR>
 * </TABLE>
 * 
 * <BR><BR>
 * <B>Reserve nodes for customizing Manager Methods</B>
 * <BR><BR>
 * Other than the above specified standard methods,  the "Dynamic Configuration Manager" can have other
 * methods whose behaviour can be configured from the XML data. Basically, there are two ways of 
 * configuring custom methods:
 * 		<OL>
 * 			<LI>
 * 				<B><U>Method Mapping</U></B>:  In this methods are mapped using a nested property string. This 
 * 				customization can be done in XML using reserve node "methodMap". This reserve node takes
 * 				following three attributes:
 * 				<BR><BR>
 * 					<TABLE Border="1">
 * 						<TR>
 * 							<TH>Attribute Name</TH>
 * 							<TH>Description</TH>
 * 						</TR>
 * 						<TR>
 * 							<TD>method</TD>
 * 							<TD>
 * 								Specifies the method name to be configured. (<B>root method</B>)
 * 							</TD>
 * 						</TR>
 * 						<TR>
 * 							<TD>property</TD>
 * 							<TD>
 * 								Specifies the nested property separated by a dot. Note, here the property means getter 
 * 								names without prefix "get"/"is".<BR>
 * 								Eg: company.employee.name which represents call getCompany().getEmployee().getName()
 * 							</TD>
 * 						</TR>
 * 						<TR>
 * 							<TD>paramString</TD>
 * 							<TD>
 * 								Specifies the mapping of the main method (specified in "method" attribute) parameters
 * 								to the parameters to be passed to the properties at different levels.
 * 								<BR><BR>
 * 								The parameters (main method parameter numbers) to be passed to a method at a 
 * 								particular level is separated by comma and these parameter strings at different
 * 								levels are separated by pipe. 
 * 							</TD>
 * 						</TR>
 * 					</TABLE>
 * 				<BR>
 * 				<P>
 *				For example consider following XML data,<BR>
 *					<span align="center">
 *							&lt;ccg:methodMap method="getErrorGroup" property="bundle.resourceBundle.errorGroup" paramString="0|1|0"/&gt;
 *					</span>
 *				<BR>The above mapping means that whenever  getErrorGroup("val1","val2") is called invoke<BR> 
 *					<span align="center">
 *						getBundle("val1").getResourceBundle("val2").getErrorGroup("val1")
 *					</span>
 *				<BR>and return the resultant value.
 *				</P>
 *
 *				<P>
 *				<B>Method Matching</B>: A negative parameter index indicates presence of parameter and it can be
 *				any type. And during execution null will be passed for this parameter. If overloaded versions are
 *				available, the given specification should match one and only one method, otherwise appropriate
 *				exceptions will be thrown.
 *				<BR>
 *  			During the input XML data loading, the method map will be used to make a "compile-time validation".
 *  			That is, the given specification should match with the signature of the methods (not on run time
 *  			return values). And using this specification, a "Method Sequence" (com.ccg.util.MethodSequence)
 *  			will be built which will be used during the method calls, which in turns tunes the performance (as
 *  			the parsing, validation and method selections are performed only once and that to at the time of 
 *  			XML loading).
 *				<BR>
 *				Note: If no paramstring is specified, then parameters of "root method" will be ditributed among the methods
 *				specified in "property". First parameter at first level, second parameter at second level and
 *				so on.  
 *				</P>
 * 			</LI>
 * 			<LI>
 * 				<B><U>Proxy Method Declaration</U></B>: A method can be configured to be proxy using reserve node
 * 				"proxyMethod" which takes method name parameter in attribute "method". Note, in order to declare a
 * 				method as proxy method, "Method Proxy" should be declared in root node using reserve attribute
 * 				"methodProxy". When the configured method is called on the "Dynamic Manager", the "Method Proxy" 
 * 				execute() method will be called and the parameters of the configured method will be passed as
 * 				object array.
 * 				<BR>
 * 				Note: If specified method is not found then appropriate exception will be thrown but unlike 
 * 				"Method Matching" if overloaded versions are found no exception will be thrown. Instead all 
 * 				overloaded versions with this name will be configured as proxy methods.
 * 			</LI>
 * 		</OL> 
 * <BR><BR>
 * @author A. Kranthi Kiran
 */
public class ManagerBuilder
{
	private static Map<String,Object> nameToManager;
		/**
		 * Builds a manager using specified resource. If reload is false and the 
		 * manager with the specified name is loaded previously, then the 
		 * previous manager will be returned.
		 * @param resource XML resource name(s). This is used to open stream using 
		 * 			current class's class loader. 
		 * @param name A name for this manager. If not null, then the same manager
		 * 				can be retieved using getManager().
		 * @param reload If this is true, manager will be reloaded from resource
		 * 				irrespective of whether it is loaded previously.
		 * @return Dynamic manager built from specified XML resource(s).
		 */
		public static Object buildManager(String resource[],String name,boolean reload)
		{
				if(!reload)
				{
						if(nameToManager==null)
							return null;
						
					Object manager=nameToManager.get(name);
						if(manager!=null)
							return manager;
				}
				
			Object manager=buildManager(resource);
			
				if(name!=null)
				{
						if(nameToManager==null)
							nameToManager=new HashMap<String,Object>();
					nameToManager.put(name,manager);
				}
				
			return manager;
		}
		
		/**
		 * Builds a manager using specified resource streams. If reload is false and the 
		 * manager with the specified name is loaded previously, then the 
		 * previous manager will be returned.
		 * @param resource XML resource streams. 
		 * @param name A name for this manager. If not null, then the same manager
		 * 				can be retieved using getManager().
		 * @param reload If this is true, manager will be reloaded from resource
		 * 				irrespective of whether it is loaded previously.
		 * @return Dynamic manager built from specified XML resource(s).
		 */
		public static Object buildManager(InputStream resource[],String name,boolean reload)
		{
				if(!reload)
				{
						if(nameToManager==null)
							return null;
					
					Object manager=nameToManager.get(name);
						if(manager!=null)
							return manager;
				}
				
			Object manager=buildManager(resource);
			
				if(name!=null)
				{
						if(nameToManager==null)
							nameToManager=new HashMap<String,Object>();
					nameToManager.put(name,manager);
				}
				
			return manager;
		}
		
		/**
		 * Same as buildManager(resource,name,reload) with a difference that, the manager instance
		 * built by this method will not be kept in internal memory for future reference. 
		 * @param resource XML resource name. This is used to open stream using 
		 * 			current class's class loader. 
		 * @return Dynamic manager built from specified XML resource.
		 */
		public static Object buildManager(String resource[])
		{
			InputStream is=null;
			Object manager=null;
			ManagerParserHandler handler=new ManagerParserHandler();
			
				for(int i=0;i<resource.length;i++)
				{
					is=ManagerBuilder.class.getResourceAsStream(resource[i]);
					
						if(is==null)
							throw new ManagerException("Specified resource does not exist: "+resource[i]);
					manager=XMLBeanParser.parse(is,manager,handler);
				}
			
			handler.finalizeLoading();
			return manager;
		}
		
		/**
		 * Same as buildManager(resource,name,reload) with a difference that, the manager instance
		 * built by this method will not be kept in internal memory for future reference. 
		 * @param resource XML resource name. This is used to open stream using 
		 * 			current class's class loader. 
		 * @return Dynamic manager built from specified XML resource.
		 */
		public static Object buildManager(InputStream resource[])
		{
			Object manager=null;
			ManagerParserHandler handler=new ManagerParserHandler();
			
				for(int i=0;i<resource.length;i++)
				{
						if(resource[i]==null)
							throw new NullPointerException("Resource stream can not be null: "+i);
						
					manager=XMLBeanParser.parse(resource[i],manager,handler);
				}
			
			handler.finalizeLoading();
			return manager;
		}
		
		/**
		 * Checks whether a manager is loaded with specified name.
		 * @param name
		 * @return Manager object if loaded with specified name.
		 */
		public static Object getManager(String name)
		{	
				if(nameToManager==null)
					return null;
			return nameToManager.get(name);
		}
		
		/**
		 * Checks if a manager is loaded with specified name.
		 * @param name
		 * @return true if a manager is loaded with specified name.
		 */
		public static boolean isManagerLoaded(String name)
		{
				if(nameToManager==null)
					return false;
			return nameToManager.containsKey(name);
		}
}
