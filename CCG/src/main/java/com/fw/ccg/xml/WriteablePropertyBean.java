package com.fw.ccg.xml;

import java.util.Map;


public interface WriteablePropertyBean
{
	public int WRITEABLE_PROPERTY=0;
	public int NOT_WRITEABLE_PROPERTY=1;
	public int UNKNOWN=2;
	
	public int isWriteableProperty(WriteableBeanNode node,BeanProperty prop);
	public String getIDName(WriteableBeanNode parentNode,BeanProperty prop,Object bean,Map<String,String> nsMap);
	public boolean isAttribute(WriteableBeanNode node,String propName);
}
