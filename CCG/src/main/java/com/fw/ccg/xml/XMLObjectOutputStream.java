package com.fw.ccg.xml;

import java.io.OutputStream;
import java.util.LinkedList;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

public class XMLObjectOutputStream extends XMLBeanWriter
{
	private static final String ROOT_ELEMENT=new String("<[ROOT_ELEMENT]>");
	
	private LinkedList<Object> parentList=new LinkedList<Object>();
	private XMLStreamWriter xout;
	
		public XMLObjectOutputStream(OutputStream os,String rootName)
		{
			this(os,new DefaultWriterHandler(rootName));
		}
	
		public XMLObjectOutputStream(OutputStream os,WriterHandler handler)
		{
			super(handler);
			
				if(os==null)
					throw new NullPointerException("Output stream can not be null.");
				
			XMLOutputFactory factory=XMLOutputFactory.newInstance();
			
				try
				{
					xout=factory.createXMLStreamWriter(os);
					xout.writeStartDocument();
					
					xout.writeStartElement(handler.getRootName());
					xout.writeAttribute("xmlns:ccg",XMLConstants.CCG_URI);
					
					parentList.add(ROOT_ELEMENT);
				}catch(Exception ex)
				{
					throw new XMLObjectStreamException("An error occured while creating XML stream",ex);
				}
		}
		
		private void pushParent(Object parent)
		{
			parentList.add(0,parent);
		}
		
		private void finalizeTill(Object parent)
		{
			Object curPar=null;
			
				while(!parentList.isEmpty())
				{
					curPar=parentList.peek();
					
						if(curPar==parent)
							return;
						
					parentList.poll();
						
						try
						{
							xout.writeEndElement();
							xout.flush();
						}catch(Exception ex)
						{
							throw new IllegalStateException("An error occured while ending the element for object: "+curPar);
						}
				}
				
				if(parent!=null)
					throw new XMLObjectStreamException("Specified parent is not found in current hierarchy: "+parent);
		}
		
		public void addObject(Object bean,Object parent)
		{
				if(bean==null)
					throw new NullPointerException("Bean can not be null.");
				
				
				try
				{
						if(parent==null)
							parent=ROOT_ELEMENT;
						
					finalizeTill(parent);
					
					WriteableBeanNode node=new WriteableBeanNode("ccg:object",bean);
					node.setAttribute("ccg:objType",bean.getClass().getName());
					
					super.populateElement(node);
					super.getHandler().customize(node,null,getNsMap());
					
					
					node.toXMLStream(xout,super.getHandler(),super.getNsMap(),false);
					xout.flush();
					
					pushParent(bean);
				}catch(XMLWriteException ex)
				{
					throw ex;
				}catch(Exception ex)
				{
					throw new XMLWriteException(null,"Error in writing XML Document",ex);
				}
			
		}
		
		public void finalize()
		{
			finalizeTill(null);
				
				try
				{
					xout.flush();
					xout.close();
				}catch(Exception ex)
				{
					throw new IllegalStateException("An error occured while closing XML stream.",ex);
				}
		}
		
		
}
