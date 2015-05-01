package com.fw.ccg.xml;

import java.io.IOException;
import java.io.OutputStream;

import org.w3c.dom.Element;

public interface XMLFormatter
{
	public String toString(Element doc);
	public void writeTo(Element doc,OutputStream out) throws IOException;
}
