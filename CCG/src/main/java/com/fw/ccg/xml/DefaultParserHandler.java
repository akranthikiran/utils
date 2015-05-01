package com.fw.ccg.xml;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import com.fw.ccg.core.CCGException;
import com.fw.ccg.core.UnsupportedDataTypeException;
import com.fw.ccg.core.ValidateException;
import com.fw.ccg.core.Validateable;
import com.fw.ccg.util.CCGUtility;
import com.fw.ccg.util.StringUtil;
import com.fw.ccg.util.ValueProvider;

/**
 * © Copyright 2006 IBM Corporation
 * <BR><BR>
 * When CCG Bean parser is used to parse XML data without specifying the handler, 
 * then default handler (com.ccg.xml.ParserDefaultHandler) will be used internally. 
 * CCG XML Parser parse() method accepts root bean that represents the root node of 
 * the input XML data. If this root bean is specified and it is not null then the same 
 * bean will be used as root bean by default handler also. <B><I>Default handler does 
 * not support reserve nodes but uses some reserve attributes.</I></B>
 * <BR>
 * Following is the list of reserved attributes and thier usage:
 * <BR><BR>
 * <TABLE border="1">
 * 		<TR>
 * 			<TH>Reserve node name</TH>
 * 			<TH>Possible Occurrences</TH>
 * 			<TH>Usage</TH>
 * 		</TR>
 * 		<TR>
 * 			<TD>beanType</TD>
 * 			<TD>
 * 				In any node (root or normal).
 * 			</TD>
 * 			<TD>
 * 				The type of the bean to be created to represent current node.<BR>
 * 				Note: the type specified by this attribute should be compatible with the type 
 * 				of the matching setter or adder.
 * 			</TD>
 * 		</TR>
 * 		<TR>
 * 			<TD>paramTypes</TD>
 * 			<TD>
 * 				In any node (root or normal).If used, params argument 
 * 				becomes mandatory.
 * 			</TD>
 * 			<TD>
 * 				This attribute can be used to force the handler to use only the 
 * 				constructor which takes specified argument types.<BR>
 * 				The argument types should be separated by comma (,) and should be 
 * 				of XMLBeanParser supported attribute types.
 * 			</TD>
 * 		</TR>
 * 		<TR>
 * 			<TD>params</TD>
 * 			<TD>
 * 				In any node (root or normal). Ignored if paramTypes is not used.
 * 			</TD>
 * 			<TD>
 * 				This represents the comma (,) separated values that needs to be 
 * 				passed to the constructor.
 * 			</TD>
 * 		</TR>
 * 		<TR>
 * 			<TD>expression</TD>
 * 			<TD>
 * 				In any node other than root node. If specified, beanType and related attributes
 * 				will be ignored.
 * 			</TD>
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
 * 			<TD>dateFormat</TD>
 * 			<TD>
 * 				Only in root node.
 * 			</TD>
 * 			<TD>
 * 				Defines the date format to be used by the factory. If not 
 * 				specified “MM/dd/yyyy” format will be used.
 * 			</TD>
 * 		</TR>
 * </TABLE>
 * <BR>
 * @author A. Kranthi Kiran
 */
public class DefaultParserHandler implements ParserHandler
{
	public static final String RNODE_INCLUDE_XML_RES = "includeXmlRes";
	public static final String ATTR_PATH = "path";

	public static final String ATTR_BEAN_TYPE = "beanType";
	public static final String ATTR_DATE_FORMAT = "dateFormat";
	public static final String ATTR_PARAMTER_TYPES = "paramTypes";
	public static final String ATTR_PARAMTERS = "params";
	public static final String ATTR_BEAN_EXPRESSION = "beanExpr";
	public static final String ATTR_TRIM_LINES = "trimLines";

	public static final String RNODE_BEAN_FACTORY = "factory";
	public static final String ATTR_RNODE_TYPE = "type";
	public static final String ATTR_RNODE_NAME = "name";
	public static final String ATTR_RNODE_VALUE = "value";

	public static final String ATTR_BEAN_ID = "beanId";
	public static final String ATTR_REF_BEAN = "beanRef";

	public static final String RNODE_INIT = "init";
	public static final String RNODE_CONSTANT = "constant";

	public static final String STD_DATE_FORMAT = "MM/dd/yyyy";

	public static final String RNODE_EXPR_PATTERN = "exprPattern";
	public static final String ATTR_PATTERN = "pattern";
	public static final String ATTR_ESCAPE_PREFIX = "escapePrefix";
	public static final String ATTR_ESCAPE_REPLACE = "escapeReplace";
	public static final String ATTR_ENABLED = "enabled";

	private Object root;
	private String dateFormat = STD_DATE_FORMAT;
	private HashMap<Class<?>, Class<?>> typeToFactCls = new HashMap<Class<?>, Class<?>>();
	private HashMap<Class<?>, BeanFactory> clsToFactInst = new HashMap<Class<?>, BeanFactory>();
	private HashMap<String, Object> objIdToObj = new HashMap<String, Object>();
	private ClassLoader classLoader;

	private Map<String, String> constantMap = new HashMap<String, String>();

	private XMLBeanParser parser;

	private Pattern expressionPattern = Pattern.compile("\\$+\\{([.[^\\}]]+)\\}");
	private String escapePrefix = "$$";
	private String escapeReplace = "$";
	private boolean expressionEnabled = true;

	/**
	 * If the root is null, then reserve attribute beanType is mandatory in root node. 
	 */
	public DefaultParserHandler()
	{}

	public DefaultParserHandler(ClassLoader classLoader)
	{
		this.classLoader = classLoader;
	}

	public void setParser(XMLBeanParser parser)
	{
		this.parser = parser;
	}

	/**
	 * Loads the date format. This method is spearated so that is can be used in setRootBean()
	 * and createRootBean().
	 * @param att
	 */
	private void loadHandlerConfigurationData(XMLAttributeMap att)
	{
		if(att.containsReservedKey(ATTR_DATE_FORMAT))
		{
			try
			{
				String dateFormat = att.getReserved(ATTR_DATE_FORMAT, "");
				if(dateFormat.trim().length() > 0)
				{
					new SimpleDateFormat(dateFormat);//just to make sure format specified is proper
					this.dateFormat = dateFormat;
				}
			}catch(Exception ex)
			{
				throw new IllegalArgumentException("Invalid date format specified in " + ATTR_DATE_FORMAT + " attribute: " + dateFormat);
			}
		}
	}

	/**
	 * Sets the root bean to this handler and loads the dateFormat if specified.
	 * @see com.fw.ccg.xml.ParserHandler#setRootBean(java.lang.Object)
	 */
	public void setRootBean(BeanNode node, XMLAttributeMap att)
	{
		this.root = node.getActualBean();
		loadHandlerConfigurationData(att);
		BeanParserSession.createSession(root);
	}

	/**
	 * A bean is created using beanType. If root bean is not specified in XMLBeanParser 
	 * parse() method then beanType is mandatory argument.
	 * <BR>
	 * If paramTypes, params are specified, they will be used to make constructor 
	 * call.
	 * <BR>
	 * If dateFormat is specified, then the same will be used as date format for parsing date
	 * properties.
	 * <BR> 
	 * @see com.fw.ccg.xml.ParserHandler#createRootBean(java.lang.String, com.fw.ccg.xml.XMLAttributeMap)
	 */
	public Object createRootBean(BeanNode node, XMLAttributeMap att)
	{
		if(root == null)
		{
			String beanType = (String)att.getReserved(ATTR_BEAN_TYPE);
			if(beanType == null)
				throw new IllegalStateException("Root bean is not specified.Attribute " + ATTR_BEAN_TYPE + " is mandatory");
			String params = (String)att.getReserved(ATTR_PARAMTERS);
			String paramTypes = (String)att.getReserved(ATTR_PARAMTER_TYPES);
			root = createBean(null, beanType, paramTypes, params, dateFormat, classLoader);
		}

		loadHandlerConfigurationData(att);
		return root;
	}

	/**
	 * If beanType, paramTypes, params are specified they will be used. Otherwise 
	 * default constructor is used on the matching property type of the current 
	 * bean.
	 * <BR>
	 * If beanType is not specified and expression is specified, then expression 
	 * value is evaluated to create the bean. beanType and related parameters are
	 * ignored.
	 * @see com.fw.ccg.xml.ParserHandler#createBean(java.lang.String, java.lang.Object, java.lang.Class, com.fw.ccg.xml.XMLAttributeMap)
	 */
	public Object createBean(BeanNode node, XMLAttributeMap att)
	{
		return createBean(node, att, classLoader);
	}

	public Class<?> getDynamicBeanType(BeanNode node, XMLAttributeMap att)
	{
		String beanType = att.getReserved(ATTR_BEAN_TYPE, null);

		if(beanType == null)
			return null;

		Class<?> btype = null;
		ClassLoader loader = classLoader;

		if(loader == null)
			loader = this.getClass().getClassLoader();

		try
		{
			btype = Class.forName(beanType, true, loader);
		}catch(Exception ex)
		{
			throw new IllegalStateException("Invalid bean type specified: " + beanType);
		}

		return btype;
	}

	public Object parseTextNodeValue(BeanNode node, XMLAttributeMap att)
	{
		String expr = att.getReserved(ATTR_BEAN_EXPRESSION, null);

		//if expression is specified ignore text value
		if(expr != null)
		{
			return getExpressionObject(expr);
		}

		Object value = XMLUtil.parseAttributeObject(node.getText(), node.getType(), dateFormat);

		if(value instanceof String)
		{
			boolean trimLines = "true".equalsIgnoreCase(att.getReserved(ATTR_TRIM_LINES, null));

			if(!trimLines)
				return value;

			String str = (String)value;
			StringTokenizer st = new StringTokenizer(str, "\n");
			StringBuilder finalVal = new StringBuilder();

			while(st.hasMoreTokens())
			{
				finalVal.append(st.nextToken().trim());

				if(st.hasMoreTokens())
					finalVal.append("\n");
			}

			return finalVal.toString();
		}

		return value;
	}

	public Object createBean(BeanNode node, XMLAttributeMap att, ClassLoader loader)
	{
		String objId = att.getReserved(ATTR_REF_BEAN, null);

		if(objId != null)
		{
			Object obj = objIdToObj.get(objId);

			if(obj == null)
				throw new IllegalStateException("No ID Bean found in cache with id: " + objId);

			if(!node.getType().isAssignableFrom(obj.getClass()))
				throw new IllegalStateException("Node required type \"" + node.getType() + "\" " + "is not compatible with the specified object-id object type: " + obj.getClass().getName());

			return obj;
		}

		String beanType = att.getReserved(ATTR_BEAN_TYPE, null);
		Class<?> btype = null;
		String beanId = att.getReserved(ATTR_BEAN_ID, null);

		if(beanId != null && objIdToObj.containsKey(beanId))
		{
			throw new IllegalStateException("Duplicate bean-id encouneted: " + beanId);
		}

		if(loader == null)
			loader = this.getClass().getClassLoader();

		if(beanType != null)
		{
			try
			{
				btype = Class.forName(beanType, true, loader);
			}catch(Exception ex)
			{}
		}

		if(typeToFactCls.containsKey(node.getType()) || typeToFactCls.containsKey(btype))
		{
			Class<?> beanTypeCls = typeToFactCls.containsKey(btype)? btype: node.getType();
			BeanFactory factory = getBeanFactory(beanTypeCls);

			btype = (btype == null)? node.getType(): btype;
			Object bean = factory.buildBean(btype, node);

			if(bean != BeanFactory.SKIP_TO_NORMAL)
			{
				if(beanId != null)
				{
					objIdToObj.put(beanId, bean);
				}

				return bean;
			}
		}

		String params = att.getReserved(ATTR_PARAMTERS, null);
		String paramTypes = att.getReserved(ATTR_PARAMTER_TYPES, null);

		if(paramTypes != null && params == null)
			throw new IllegalStateException("\"" + ATTR_PARAMTERS + "\" is mandatory attribute when \"" + ATTR_PARAMTER_TYPES + "\" is used.");

		String expr = att.getReserved(ATTR_BEAN_EXPRESSION, null);
		Object bean = null;

		//if expression is specified ignore bean type
		if(expr != null)
			bean = getExpressionObject(expr);
		else
			bean = createBean(node.getType(), beanType, paramTypes, params, dateFormat, loader);

		if(beanId != null)
		{
			objIdToObj.put(beanId, bean);
		}

		return bean;
	}

	public Object createAttributeBean(BeanNode node, String attName, Class<?> type)
	{
		if(typeToFactCls.containsKey(type))
		{
			BeanFactory factory = getBeanFactory(type);
			Object bean = factory.buildAttributeBean(type, node, attName);

			if(bean != BeanFactory.SKIP_TO_NORMAL)
				return bean;
		}

		return NOT_SUPPORTED;
	}

	/**
	 * If the bean representing current node is of type com.ccg.core.Validateable, 
	 * then validate() method on that bean will be invoked. Otherwise this method 
	 * call is simply ignored.
	 * <BR>
	 * @see com.fw.ccg.xml.ParserHandler#validateBean(java.lang.String, java.lang.Object, java.lang.Object)
	 * @throws ValidateException Thrown if bean validation fails.
	 */
	public void validateBean(BeanNode node) throws ValidateException
	{
		Object bean = node.getActualBean();

		if(bean instanceof Validateable)
			((Validateable)bean).validate();

		if(bean == root)//end of processing
		{
			BeanFactory fact = null;
			Iterator<BeanFactory> it = clsToFactInst.values().iterator();
			while(it.hasNext())
			{
				fact = it.next();
				fact.finalize();
			}
			BeanParserSession.destroyCurrentSession();
		}
	}

	/**
	 * Simple returns null.
	 * <BR>
	 * @see com.fw.ccg.xml.ParserHandler#processReservedNode(java.lang.String, java.lang.Object, com.fw.ccg.xml.XMLAttributeMap)
	 */
	public Object processReservedNode(BeanNode node, XMLAttributeMap att)
	{
		if(RNODE_BEAN_FACTORY.equals(node.getName()))
		{
			String factName = att.get(ATTR_RNODE_NAME, null);
			String typeName = att.get(ATTR_RNODE_TYPE, null);
			if(factName == null)
				throw new IllegalArgumentException("Name is manadatory attribute in <factory> node.");

			if(typeName == null)
				throw new IllegalArgumentException("Type is manadatory attribute in <factory> node.");

			try
			{
				Class<?> factCls = Class.forName(factName);
				Class<?> typeCls = Class.forName(typeName);
				addBeanFactory(typeCls, factCls);
			}catch(Exception ex)
			{
				throw new IllegalStateException("Invalid types mentioned in <factory>: " + factName + "," + typeName, ex);
			}

			return null;
		}

		if(RNODE_INIT.equals(node.getName()))
		{
			String typeName = att.get(ATTR_RNODE_TYPE, null);

			if(typeName == null)
				throw new IllegalArgumentException("Type is manadatory attribute in <init> node.");

			try
			{
				Class.forName(typeName);
			}catch(Exception ex)
			{
				throw new IllegalStateException("Invalid type mentioned in <init>: " + typeName, ex);
			}

			return null;
		}

		if(RNODE_INCLUDE_XML_RES.equals(node.getName()))
		{
			String path = att.get(ATTR_PATH, null);

			if(path == null)
			{
				throw new IllegalStateException("Mandatory attribute \"path\" is missing in node: " + RNODE_INCLUDE_XML_RES);
			}

			InputStream xmlInput = DefaultParserHandler.class.getResourceAsStream(path);
			XMLBeanParser.parse(xmlInput, root);
			return null;
		}

		if(RNODE_CONSTANT.equals(node.getName()))
		{
			String name = att.get(ATTR_RNODE_NAME, null);
			String value = att.get(ATTR_RNODE_VALUE, null);

			if(name == null)
			{
				throw new IllegalStateException("Mandatory attribute \"name\" is missing in node: " + RNODE_CONSTANT);
			}

			if(value == null)
			{
				throw new IllegalStateException("Mandatory attribute \"value\" is missing in node: " + RNODE_CONSTANT);
			}

			constantMap.put(name, value);
			return null;
		}

		if(RNODE_EXPR_PATTERN.equals(node.getName()))
		{
			this.expressionEnabled = "true".equalsIgnoreCase(att.get(ATTR_ENABLED, "true"));

			if(!expressionEnabled)
			{
				return null;
			}

			expressionPattern = Pattern.compile(att.get(ATTR_PATTERN, "\\$+\\{([.[^\\}]]+)\\}"));
			escapePrefix = att.get(ATTR_ESCAPE_PREFIX, "$$");
			escapeReplace = att.get(ATTR_ESCAPE_REPLACE, "$");
			return null;
		}

		return null;
	}

	/**
	 * This will be simply return the name of the class of current bean.
	 * <BR>
	 * @see com.fw.ccg.xml.ParserHandler#getBeanDescription(java.lang.Object)
	 */
	public String getBeanDescription(Object bean)
	{
		return bean.getClass().getName();
	}

	/**
	 * This will be the name of the current node. If the current node is reserve node
	 * then SKIP_NODE_ELEMENT is returned, so that reserve node and its sib nodes get
	 * skipped.
	 * <BR>
	 * @see com.fw.ccg.xml.ParserHandler#getNodeDescription(java.lang.String, com.fw.ccg.xml.XMLAttributeMap)
	 */
	public String getNodeDescription(BeanNode node, XMLAttributeMap att)
	{
		/*
		 * There are default reserve nodes. So we  should not just skip it
		 * 
		 * And the mechanism to skip should not be based on description
		 */
		//if(node.isReserved())
		//return BeanNode.SKIP_NODE_ELEMENT;

		String firstKey = att.getFirstKey(false);

		if(firstKey == null)
			return node.getName();

		StringBuilder builder = new StringBuilder(node.getName());
		builder.append("(").append(firstKey).append("=").append(att.get(firstKey)).append(")");
		return builder.toString();
	}

	/**
	 * If dateFormat is specified in root node, that will be returned. Or default 
	 * format used is “MM/dd/yyyy”.
	 * <BR>
	 * @see com.fw.ccg.xml.ParserHandler#getDateFormat()
	 */
	public String getDateFormat()
	{
		return dateFormat;
	}

	/**
	 * Simply ignored.
	 * <BR>
	 * @see com.fw.ccg.xml.ParserHandler#processReserveNodeEnd(java.lang.String, java.lang.Object, java.lang.Object, com.fw.ccg.xml.XMLAttributeMap)
	 */
	public void processReserveNodeEnd(BeanNode node, XMLAttributeMap att)
	{}

	/**
	 * This method is expected to be used by child classes to change date format as
	 * per the needs.
	 * <BR>
	 * @param dateFormat New date format.
	 */
	public void setDateFormat(String dateFormat)
	{
		if(dateFormat == null || dateFormat.length() <= 0)
			throw new IllegalArgumentException("Date format cannot be null or empty.");
		new SimpleDateFormat(dateFormat);//just to make sure format specified is proper
		this.dateFormat = dateFormat;
	}

	public void addBeanFactory(Class<?> type, Class<?> factCls)
	{
		if(type == null)
			throw new NullPointerException("Type can not be null.");

		if(factCls == null)
		{
			typeToFactCls.remove(type);
			return;
		}

		typeToFactCls.put(type, factCls);
	}

	public BeanFactory getBeanFactory(Class<?> type)
	{
		Class<?> factCls = typeToFactCls.get(type);

		if(factCls == null)
			return null;

		BeanFactory fact = clsToFactInst.get(factCls);

		if(fact == null)
		{
			try
			{
				fact = (BeanFactory)factCls.newInstance();
				clsToFactInst.put(factCls, fact);
			}catch(Exception ex)
			{
				throw new CCGException("Error in initiating bean factory: " + factCls.getName());
			}
		}

		return fact;
	}

	public static Object createBean(BeanNode node, ClassLoader loader)
	{
		XMLAttributeMap att = node.getAttributeMap();
		String beanType = att.getReserved(ATTR_BEAN_TYPE, null);

		String params = att.getReserved(ATTR_PARAMTERS, null);
		String paramTypes = att.getReserved(ATTR_PARAMTER_TYPES, null);

		if(paramTypes != null && params == null)
			throw new IllegalStateException("\"" + ATTR_PARAMTERS + "\" is mandatory attribute when \"" + ATTR_PARAMTER_TYPES + "\" is used.");

		String expr = att.getReserved(ATTR_BEAN_EXPRESSION, null);
		//if expression is specified ignore bean type
		if(expr != null)
			return getExpressionObject(expr);
		return createBean(node.getType(), beanType, paramTypes, params, null, loader);
	}

	/**
	 * Creates a bean using specified type. If preferredType is specified and if it is assignable
	 * to type then the preferredType is used to create the object. If preferredType not assignable 
	 * to type then UnsupportedDataTypeException is thrown.
	 * <BR>
	 * 
	 * If paramTypes and paramValues are specified, then appropriate constructor is
	 * called. paramTypes should comma separated types and these types should be 
	 * XMLBeanParser supported attributed types. paramValues should be comma separated
	 * values. 
	 *    
	 * @param type  The type of Object which needs to be constructed.
	 * @param preferredType Fully specified class name which is preferred type of the object. This 
	 * should be assignable to type (if type is specified).
	 * @param paramTypes Comma separated parameter types.
	 * @param paramValues Parameters that needs to be passed to the constructor. 
	 * @param dateFormat The format to be used to convert specified paramter string into java.util.Date.
	 * 
	 * @return The constructed object.
	 */
	public static Object createBean(Class<?> type, String preferredType, String paramTypes, String paramValues, String dateFormat)
	{
		return createBean(type, preferredType, paramTypes, paramValues, dateFormat, null);
	}

	public static Object createBean(Class<?> type, String preferredType, String paramTypes, String paramValues, String dateFormat, ClassLoader loader)
	{
		if(preferredType != null)
		{
			try
			{
				Class<?> cls = null;
				if(loader != null)
					cls = Class.forName(preferredType, true, loader);
				else
					cls = Class.forName(preferredType);

				if(type != null && !type.isAssignableFrom(cls))
					throw new UnsupportedDataTypeException("Specified class \"" + cls.getName() + "\" is not assignable to \"" + type.getName() + "\"");
				type = cls;
			}catch(ClassNotFoundException ex)
			{
				throw new UnsupportedDataTypeException("Invalid class name specified: " + preferredType);
			}
		}
		else
		{
			if(type == null)
				throw new IllegalArgumentException("Either of type or preferredType is mandatory.");

			try
			{
				if(loader != null)
					type = Class.forName(type.getName(), true, loader);
			}catch(ClassNotFoundException ex)
			{
				throw new UnsupportedDataTypeException("Failed to load class: " + type.getName() + " using class loader: " + loader);
			}
		}

		if(paramTypes != null && paramTypes.length() > 0)
		{
			Constructor<?> cons = null;
			String types[] = StringUtil.tokenize(paramTypes, ",", false, true);
			Object values[] = StringUtil.tokenize(paramValues, ",", false, true);
			Object actValues[] = new Object[values.length];

			if(types.length != values.length)
				throw new IllegalStateException("Number of types and values are not matching.");

			Class<?> paramTypeCls[] = new Class[types.length];
			int i = 0;
			try
			{
				for(i = 0; i < paramTypeCls.length; i++)
				{
					paramTypeCls[i] = CCGUtility.getClass(types[i]);

					if(!XMLUtil.isSupportedAttributeClass(paramTypeCls[i]))
						throw new UnsupportedOperationException("Specified type is not suported as attribute type: " + paramTypeCls[i].getName());
				}
			}catch(UnsupportedOperationException ex)
			{
				throw ex;
			}catch(Exception ex)
			{
				throw new CCGException("Invalid parameter type specified: " + types[i], ex);
			}

			try
			{
				cons = type.getConstructor(paramTypeCls);
			}catch(Exception ex)
			{}

			if(cons == null)
				throw new IllegalStateException("No constructor found in class \"" + type.getName() + "\" with arguments: " + paramTypes);

			for(i = 0; i < values.length; i++)
				actValues[i] = XMLUtil.parseAttributeObject((String)values[i], paramTypeCls[i], dateFormat);

			try
			{
				return cons.newInstance(actValues);
			}catch(Exception ex)
			{
				throw new CCGException("Error in creating bean of type: " + type.getName() + "\nRootCause: " + ex, ex);
			}
		}

		try
		{
			return type.newInstance();
		}catch(Exception ex)
		{
			throw new CCGException("Error in creating bean of type: " + type.getName() + "\nRootCause: " + ex, ex);
		}
	}

	/**
	 * Gets the object represnted by the specified static field expression. 
	 * @param expression static field expression.
	 * @return Object represented by static field expression.
	 */
	public static Object getExpressionObject(String expression)
	{
		String tokens[] = StringUtil.tokenize(expression, ".", false, true);
		Class<?> cls = null;
		Class<?> tmpCls = null;
		String tmpExpr = null;
		int idx = 0;

		MAIN: for(; idx < tokens.length; idx++)
		{
			try
			{
				if(tmpExpr != null)
					tmpExpr += "." + tokens[idx];
				else
					tmpExpr = tokens[idx];
				/*
				 * Even if class is found after cetain tokens, we need
				 * to stillcontinue iterations in order to consider
				 * inner classes.  
				 */
				tmpCls = Class.forName(tmpExpr);
				cls = tmpCls;
			}catch(Exception ex)
			{
				if(cls != null)//if previous iteration resulted in class
				{
					Class<?> innerCls[] = cls.getClasses();

					if(innerCls == null || innerCls.length == 0)
						break;

					for(Class<?> icls : innerCls)
					{
						if(tokens[idx].equals(icls.getSimpleName()))
						{
							cls = icls;
							continue MAIN;
						}
					}

					break;
				}
			}
		}

		if(cls == null)
			throw new CCGException("Failed to evaluation static object expression: " + expression);

		if(idx >= tokens.length)
			throw new CCGException("Specified expression indicates a class not a static object: " + expression);

		Field fld = null;
		Object res = null;
		Method method = null;
		
		for(; idx < tokens.length; idx++)
		{
			try
			{
				try
				{
					fld = cls.getField(tokens[idx]);
					res = fld.get(res);
				}catch(NoSuchFieldException ex)
				{
					method = cls.getMethod(tokens[idx]);
					res = method.invoke(res);
				}
				
				cls = res.getClass();
			}catch(Exception ex)
			{
				if(res == null)
					throw new CCGException("Failed to fetch field/method \"" + tokens[idx] + "\" value from class " + cls.getName() + " for expression: " + expression, ex);
				
				throw new CCGException("Failed to fetch instance field/method \"" + tokens[idx] + "\" value from class " + cls.getName() + " for expression: : " + expression, ex);
			}
		}
		return res;
	}

	public XMLBeanParser getParser()
	{
		return parser;
	}

	public void stopProcessing()
	{
		parser.stopProcessing();
	}

	@Override
	public String getConstantValue(String name)
	{
		String value = constantMap.get(name);
		int index = 0;
		
		if((index = name.indexOf(".")) > 0)
		{
			if(name.startsWith("environment.") && name.length() > index)
			{
				name = name.substring(index + 1);
				return System.getenv(name);
			}
			
			if(name.startsWith("systemProperty.") && name.length() > index)
			{
				name = name.substring(index + 1);
				return System.getProperty(name);
			}
			
			Object valBean = getExpressionObject(name);

			if(valBean != null)
			{
				value = valBean.toString();
			}
		}

		return value;
	}

	@Override
	public String processText(final Object rootBean, String text)
	{
		if(!expressionEnabled)
		{
			return text;
		}

		ValueProvider valueProvider = new ValueProvider()
		{
			@Override
			public Object getValue(String name)
			{
				if(name.startsWith("#"))
				{
					return getConstantValue(name.substring(1));
				}

				try
				{
					return CCGUtility.invokeGetProperty(rootBean, name, null, false);
				}catch(Exception ex)
				{
					throw new IllegalStateException("An error occurred while invoking property " + name + " on bean " + rootBean.getClass().getName(), ex);
				}
			}
		};

		return StringUtil.getPatternString(text, valueProvider, expressionPattern, escapePrefix, escapeReplace);
	}
}
