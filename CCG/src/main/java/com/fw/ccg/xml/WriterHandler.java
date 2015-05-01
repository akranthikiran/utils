package com.fw.ccg.xml;

import java.util.Map;
import java.util.Set;

public interface WriterHandler extends XMLConstants
{
	public String getRootName();
	
	public boolean isAttribute(WriteableBeanNode node,BeanProperty prop,Object attrValue);
	public boolean isCdataNode(WriteableBeanNode parent,String name,Object value);
	public String getAttributedString(WriteableBeanNode parent,String name,Object value,boolean forAttribute);
	
	public String getIDName(WriteableBeanNode parentNode,BeanProperty prop,Object bean,Map<String,String> nsMap);
	public String getIDString(WriteableBeanNode node,Object idValue);
	
	public void customize(WriteableBeanNode node,BeanProperty property,Map<String,String> nsMap);
	
	public Set<String> getPrimaryProperties(WriteableBeanNode node);
	public boolean isWriteableProperty(WriteableBeanNode node,BeanProperty prop);
	
	public WriteableBeanNode toBeanNode(BeanProperty prop,Object value);
	
	public String getDateFormat();
}
