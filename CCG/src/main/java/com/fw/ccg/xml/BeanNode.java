package com.fw.ccg.xml;



public class BeanNode implements Cloneable
{
	public static final String SKIP_NODE_ELEMENT=new String("SKIP_NODE_ELEMENT");
	static final Object NULL_RESERVED_NODE=new Object();
	
	
	private String name;
	private String description;
	private Object bean;
	private BeanNode parentNode;
	private String nameSpace;
	private Class<?> type;
	private Class<?> actualType;
	private XMLAttributeMap attributeMap;
	private String id=null;
	private StringBuffer buff=null;
	private boolean textNode=false;
	
		public BeanNode(String name)
		{
			this(null,name,null,null);
		}
	
		public BeanNode(String nameSpace,String name)
		{
			this(nameSpace,name,null,null);
		}
	
		public BeanNode(String name,Object bean)
		{
			this(null,name,bean,null);
		}
	
		public BeanNode(String nameSpace,String name,BeanNode bean)
		{
			this(nameSpace,name,bean,null);
		}
		
		public BeanNode(String name,Object bean,BeanNode parent)
		{
			this(null,name,bean,parent);
		}
		
		public BeanNode(String nameSpace,String name,Object bean,BeanNode parent)
		{
				if(XMLConstants.CCG_URI.equals(nameSpace))
					nameSpace=XMLConstants.CCG_URI;
				
			this.nameSpace=(nameSpace==null || nameSpace.trim().length()==0)?null:nameSpace.trim();
			this.name=name;
			this.bean=bean;
			this.parentNode=parent;
		}
		
		void setBean(Object bean)
		{
				if(textNode)
					throw new IllegalStateException("Bean can not be set for text node.");
				
			this.bean=bean;
		}
	
		public Object getActualBean()
		{
				if(textNode && bean==null)
					bean=getText();
				
			return bean;
		}
		
		public String getName()
		{
			return name;
		}
		
		public Object getParent()
		{
			return parentNode.getActualBean();
		}
		
		public BeanNode getParentNode()
		{
			return parentNode;
		}
		
		void setParentNode(BeanNode parentNode)
		{
			this.parentNode=parentNode;
		}
		
		public String getNameSpace()
		{
			return nameSpace;
		}

		public Class<?> getType()
		{
			return type;
		}

		void setType(Class<?> type)
		{
			this.type=type;
		}

		public XMLAttributeMap getAttributeMap()
		{
			return attributeMap;
		}

		void setAttributeMap(XMLAttributeMap attributeMap)
		{
			this.attributeMap=attributeMap;
		}

		public String getDescription()
		{
			return description;
		}

		void setDescription(String description)
		{
			this.description=description;
		}
		
		public String getID()
		{
			return id;
		}
		
		public void setID(String id)
		{
			this.id=id;
		}
		
		void setTextNodeFlag(boolean flag)
		{
			textNode=flag;
				if(textNode==false)
					buff=null;
		}
		
		public void setText(String text)
		{
			clearText();
			appendText(text);
		}
		
		void clearText()
		{
				if(buff!=null)
					buff.setLength(0);
		}
		
		
		public void appendText(String txt)
		{
				if(buff==null)
				{
						if(txt==null || txt.trim().length()==0)
							return;
					buff=new StringBuffer();
				}
			
			buff.append(txt);
		}
		
		public boolean containsText()
		{
			return (buff!=null && buff.length()>0);
		}
		
		public String getActualText()
		{
				if(buff==null)
					return "";
				
			return buff.toString();
		}
		
		public String getText()
		{
				if(buff==null)
					return "";
				
			/*StringBuffer res=new StringBuffer();
			StringTokenizer st=new StringTokenizer(buff.toString(),"\n");
			String line=null;
			
				while(st.hasMoreTokens())
				{
					line=st.nextToken().trim();
						if(line.length()==0)
							continue;
					res.append(line);
					res.append("\n");
				}
				*/
			return buff.toString().trim();	
		}
		
		public boolean isReserved()
		{
			return (XMLConstants.CCG_URI==nameSpace);
		}
		
		public boolean isSkipNode()
		{
			return (description==SKIP_NODE_ELEMENT);
		}
		
		public boolean isReservedNullNode()
		{
			return (bean==NULL_RESERVED_NODE);
		}
		
		public boolean isTextNode()
		{
			return textNode;
		}
		
		public boolean isIDBased()
		{
			return (id!=null);
		}

		public String getNodePath()
		{
			BeanNode node=this;
			StringBuffer buff=new StringBuffer();
			
				while(node!=null)
				{
					buff.insert(0,"/");
					buff.insert(0,node);
					node=node.getParentNode();
				}
			return buff.toString();
		}
		
		public Object clone()
		{
			BeanNode newObj=new BeanNode(nameSpace,name,bean,parentNode);
			newObj.setDescription(description);
			newObj.setAttributeMap(attributeMap);
			newObj.setType(type);
			
			return newObj;
		}
		
		public String toString()
		{
			String description=(this.description==null)?name:this.description;
			
				if(nameSpace==null)
					return description;
				
			return description+"["+nameSpace+"]";
		}

		public Class<?> getActualType()
		{
			return actualType;
		}

		public void setActualType(Class<?> actualType)
		{
			this.actualType=actualType;
		}
		
		
}
