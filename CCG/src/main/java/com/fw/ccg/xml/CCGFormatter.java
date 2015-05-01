package com.fw.ccg.xml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CCGFormatter implements XMLFormatter
{
	private String version;
	private String encoding;
	
		public CCGFormatter()
		{}
		
		public CCGFormatter(String version,String encoding)
		{
			this.version=version;
			this.encoding=encoding;
		}
	
		public String toString(Element doc)
		{
			ByteArrayOutputStream bos=new ByteArrayOutputStream();
				
				try
				{
					writeTo(doc,bos);
					bos.flush();
				}catch(IOException ex)
				{
					throw new XMLWriteException(null,"Error in formatting XML",ex);
				}
			
			return new String(bos.toByteArray());
		}
	
		public void writeTo(Element doc,OutputStream out) throws IOException
		{
			StringBuilder builder=new StringBuilder();
			
				if(version!=null && encoding!=null)
					builder.append("<?xml version=\""+version+"\" encoding=\""+encoding+"\"?>\n");
				
			convert(doc,0,builder);
			
			out.write(builder.toString().getBytes());
			out.flush();
		}

		private void convert(Node node,int level,StringBuilder builder)
		{
				if(node.getNodeType()==Node.TEXT_NODE)
				{
					String s=node.getNodeValue();
					
						if(s.trim().length()<=0)
							return;
						
					builder.append(s.trim());
					return;
				}
				
				if(node.getNodeType()==Node.CDATA_SECTION_NODE)
				{
					String s=node.getNodeValue();
					
						if(s.trim().length()<=0)
							return;
						
					builder.append("<![CDATA[");
					builder.append(s.trim());
					builder.append("]]>");
					return;
				}
				
				for(int i=0;i<level;i++)
					builder.append("\t");
			
			builder.append("<");
				if(node.getPrefix()!=null)
					builder.append(node.getPrefix()+":"+node.getNodeName());
				else
					builder.append(node.getNodeName());
			
				
				if(node.getAttributes()!=null && node.getAttributes().getLength()>0)
				{
					NamedNodeMap attMap=node.getAttributes();
					int len=attMap.getLength();
					Node att=null;
					
						for(int i=0;i<len;i++)
						{
							att=attMap.item(i);
								if(att.getPrefix()!=null)
									builder.append(" "+att.getPrefix()+":"+att.getNodeName());
								else
									builder.append(" "+att.getNodeName());
							
							builder.append("=\"");
							builder.append(att.getNodeValue());
							builder.append("\"");
						}
				}
				
			NodeList subnodes=node.getChildNodes();
			int len=(subnodes==null)?0:subnodes.getLength();
				if(len==0)
				{
					builder.append("/>\n");
					return;
				}
			
				if(len==1 && subnodes.item(0).getNodeType()==Node.TEXT_NODE)
				{
					builder.append(">");
						if(subnodes.item(0).getNodeValue()!=null)
							builder.append(subnodes.item(0).getNodeValue().trim());
				}
				else if(len==1 && subnodes.item(0).getNodeType()==Node.CDATA_SECTION_NODE)
				{
					builder.append(">");
						if(subnodes.item(0).getNodeValue()!=null)
						{
							builder.append("<![CDATA[");
							builder.append(subnodes.item(0).getNodeValue().trim());
							builder.append("]]>");
						}
				}
				else
				{
				
					builder.append(">\n");
			
						for(int i=0;i<len;i++)
							convert(subnodes.item(i),level+1,builder);
						
						for(int i=0;i<level;i++)
							builder.append("\t");
				}
				
				
				if(node.getPrefix()!=null)
					builder.append("</"+node.getPrefix()+":"+node.getNodeName()+">\n");
				else
					builder.append("</"+node.getNodeName()+">\n");
		}
}
