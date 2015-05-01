package com.fw.ccg.xml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Element;

public class DOMFormatter implements XMLFormatter
{
	private String version;
	private String encoding;
	
		public DOMFormatter()
		{}
		
		public DOMFormatter(String version,String encoding)
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
				}catch(XMLWriteException ex)
				{
					throw ex;
				}catch(IOException ex)
				{
					throw new XMLWriteException(null,"Error in formatting XML",ex);
				}
			
			return new String(bos.toByteArray());
		}
	
		public void writeTo(Element doc,OutputStream out) throws IOException
		{
				try
				{
					Transformer trans=TransformerFactory.newInstance().newTransformer();
					trans.setOutputProperty(OutputKeys.INDENT,"Yes");
					trans.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "4");
					
						if(version!=null)
							trans.setOutputProperty(OutputKeys.VERSION,version);
						
						if(encoding!=null)
							trans.setOutputProperty(OutputKeys.ENCODING,encoding);
					
					DOMSource src=new DOMSource(doc);
					StreamResult res=new StreamResult(out);
					
					trans.transform(src,res);
				}catch(TransformerException ex)
				{
					throw new XMLWriteException(null,"Error in formatting XML",ex);
				}
			
		}
}
