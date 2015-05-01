package com.fw.ccg.xml;

import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.AttributesImpl;

class SAXStreamBridge
{
	private ContentHandler handler;
	private XMLStreamReader reader;
	private boolean closed=false;
	
		public SAXStreamBridge(InputStream is,ContentHandler handler)
		{
				try
				{
					this.reader=XMLInputFactory.newInstance().createXMLStreamReader(is);
				}catch(Exception ex)
				{
					throw new XMLObjectStreamException("An error occured while creating XML stream",ex);
				}
				
			this.handler=handler;
		}
		
		public String getURI()
		{
			return reader.getNamespaceURI();
		}
		
		public String getName()
		{
			return reader.getLocalName();
		}
		
		public boolean parseTill(int event)
		{
				while(parseNext())
				{
					if(reader.getEventType()==event)
						return true;
				}
			
			return false;
		}
		
		public int getEvent()
		{
			return reader.getEventType();
		}
		
		public boolean parseNext()
		{
				if(closed)
					return false;
				
				try
				{
					int res=reader.next();
					
						switch(res)
						{
							case XMLStreamConstants.START_ELEMENT:
							{
								AttributesImpl attrLst=new AttributesImpl();
								int len=reader.getAttributeCount();
									for(int i=0;i<len;i++)
									{
										attrLst.addAttribute(reader.getAttributeNamespace(i),reader.getAttributeLocalName(i),
													reader.getAttributePrefix(i),reader.getAttributeType(i),
													reader.getAttributeValue(i));
									}
									
								handler.startElement(reader.getNamespaceURI(),reader.getLocalName(),
												reader.getPrefix(),attrLst);
								
								break;
							}
							case XMLStreamConstants.END_ELEMENT:
							{
								handler.endElement(reader.getNamespaceURI(),reader.getLocalName(),reader.getPrefix());
								break;
							}
							case XMLStreamConstants.CDATA:
							case XMLStreamConstants.CHARACTERS:
							{
								String txt=reader.getText();
								handler.characters(txt.toCharArray(),0,txt.length());
								break;
							}
							case XMLStreamConstants.START_DOCUMENT:
							{
								handler.startDocument();
								break;
							}
							case XMLStreamConstants.END_DOCUMENT:
							{
								handler.endDocument();
								closed=true;
								return false;
							}
							default:
								parseNext();
						}
				}catch(Exception ex)
				{
					throw new XMLObjectStreamException("An error occured while parsing XML stream",ex);
				}
			
			return true;
		}
		
}
