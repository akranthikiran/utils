package com.fw.dao.qry.impl;

import com.fw.ccg.xml.BeanNode;
import com.fw.ccg.xml.DefaultParserHandler;
import com.fw.ccg.xml.XMLAttributeMap;
import com.fw.dao.qry.Query;

class QueryXMLBeanHandler extends DefaultParserHandler
{
	public static final String RES_NODE_LINK_RES = "linkResource";
	
	private XMLQuerySource root;
	
	public QueryXMLBeanHandler(XMLQuerySource root)
	{
		this.root = root;
	}

	@Override
	public Object createBean(BeanNode node, XMLAttributeMap att)
	{
		Object parent = node.getParent();

		if(!(parent instanceof Query.NodeQueryElement))
			return super.createBean(node, att);

		String name = node.getName();
		return new Query.NodeQueryElement(name);
	}

	@Override
	public Class<?> getDynamicBeanType(BeanNode node, XMLAttributeMap att)
	{
		Object parent = node.getParent();

		if(!(parent instanceof Query.NodeQueryElement))
			return super.getDynamicBeanType(node, att);

		return Query.NodeQueryElement.class;
	}

	@Override
	public Object processReservedNode(BeanNode node, XMLAttributeMap att)
	{
		if(!RES_NODE_LINK_RES.equals(node.getName()))
		{
			return super.processReservedNode(node, att);
		}
		
		String resourceName = att.get("path", null);
		
		if(resourceName == null)
		{
			throw new IllegalArgumentException("Mandatory 'path' attribute is not specified in node <" + RES_NODE_LINK_RES + ">");
		}
		
		XMLQuerySource otherQuerySource = XMLQueryFactory.loadQuerySourceFromXML(resourceName);
		root.processLinkFrom(otherQuerySource);
		return null;
	}

}
