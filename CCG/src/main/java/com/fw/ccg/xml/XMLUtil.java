package com.fw.ccg.xml;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import com.fw.ccg.core.UnsupportedDataTypeException;
import com.fw.ccg.util.InvalidValueException;

public class XMLUtil
{
	private static HashSet<Class<?>> supportedAttrTypes=new HashSet<Class<?>>();
	
	static
	{
		supportedAttrTypes.add(String.class);
		supportedAttrTypes.add(Date.class);
		supportedAttrTypes.add(StringBuffer.class);
		supportedAttrTypes.add(StringBuilder.class);
		supportedAttrTypes.add(Class.class);
		
		supportedAttrTypes.add(Byte.class);
		supportedAttrTypes.add(Boolean.class);
		supportedAttrTypes.add(Character.class);
		supportedAttrTypes.add(Short.class);
		supportedAttrTypes.add(Integer.class);
		supportedAttrTypes.add(Long.class);
		supportedAttrTypes.add(Float.class);
		supportedAttrTypes.add(Double.class);
		
		supportedAttrTypes.add(BigDecimal.class);
		supportedAttrTypes.add(BigInteger.class);
	}
	
	/**
	 * Determines whether specified type is supported as attributes data or for text-based
	 * node property.
	 * @param type Type that needs to be checked.
	 * @return true if and only if type is supported as attributed type.
	 */
	public static boolean isSupportedAttributeClass(Class<?> type)
	{
			if(type==null)
				throw new NullPointerException("Type can not be null.");
			
			if(type.isPrimitive())
				return true;
			
		return (supportedAttrTypes.contains(type));
	}
	
	/**
	 * Parses the given textValue into specified type. If the type is of type java.util.Date
	 * then specified dateFormat will be used to parse the date.
	 * 
	 * @param textValue Value to be parsed.
	 * @param type Type to which text needs to be parsed.
	 * @param dateFormat Date format to be used, to parse text into date object.
	 * @return Parsed Object.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Object parseAttributeObject(String textValue,Class<?> type,String dateFormat)
	{
			if(type==null)
				throw new NullPointerException("Type can not be null.");
			
			if(Object.class.equals(type))
				return textValue;
			
			if(type.isEnum())
			{
				return Enum.valueOf((Class)type, textValue);
			}
			
			if(!isSupportedAttributeClass(type))
				throw new UnsupportedDataTypeException("Specified type is not supported for attributed data:" +type.getName());
			
		return toObject(textValue,type,dateFormat);		
	}
	
	public static String formatAttributeObject(Object value,String dateFormat)
	{
			if(value==null)
				throw new NullPointerException("Value can not be null.");
			
		Class<?> type=value.getClass();
			
			if(!isSupportedAttributeClass(type))
				throw new UnsupportedDataTypeException("Specified type is not supported for attributed data:" +type.getName());
			
			if(value instanceof Date)
			{
				dateFormat=(dateFormat==null)?"MM/dd/yyyy":dateFormat;
				
					try
					{
						SimpleDateFormat format=new SimpleDateFormat(dateFormat);
						return format.format((Date)value);
					}catch(Exception ex)
					{
						throw new IllegalArgumentException("Error in formating date object with format: "+dateFormat,ex);
					}
			}
			
		return value.toString();		
	}

	private static Object toObject(String value,Class<?> type,String format)
	{
			if(type==null)
				throw new NullPointerException("Type can not be null.");
			
			if(value==null)
				throw new NullPointerException("Value can not be null.");
		
		value=value.trim();
		
			if(type.equals(String.class))
				return value;
	
			if(StringBuffer.class.equals(type))
				return new StringBuffer(value);
			
			if(StringBuilder.class.equals(type))
				return new StringBuffer(value);
			
			if(Class.class.equals(type))
			{
				try
				{
					Class<?> cls=Class.forName(value.trim());
					return cls;
				}catch(Exception ex)
				{
					throw new IllegalArgumentException("Invalid class name encountered: "+value,ex);
				}
			}
			
			if(type.equals(Character.TYPE) || type.equals(Character.class))
				return new Character(value.charAt(0));
	
			if(type.equals(Double.TYPE) || type.equals(Double.class))
				return Double.valueOf(value);
	
			if(type.equals(Float.TYPE) || type.equals(Float.class))
				return Float.valueOf(value);
	
			if(type.equals(Long.TYPE) || type.equals(Long.class))
				return Long.valueOf(value);
	
			if(type.equals(Integer.TYPE) || type.equals(Integer.class))
				return Integer.valueOf(value);
	
			if(type.equals(Short.TYPE) || type.equals(Short.class))
				return Short.valueOf(value);
	
			if(type.equals(Boolean.TYPE) || type.equals(Boolean.class))
				return Boolean.valueOf(value);
	
			if(type.equals(Byte.TYPE) || type.equals(Byte.class))
				return Byte.valueOf(value);
			
			if(type.equals(BigDecimal.class))
				return new BigDecimal(value);
			
			if(type.equals(BigInteger.class))
				return new BigInteger(value);
			
			if(type.equals(java.util.Date.class))
			{
				SimpleDateFormat frm=null;
				format=(format==null)?"MM/dd/yyyy":format;
					try
					{
						frm=new SimpleDateFormat(format);
						return frm.parse(value);
					}catch (Exception e)
					{
						throw new InvalidValueException("Invalid date value/format ("+format+") encountered: "+value,e);
					}
			}
			
		throw new IllegalStateException("Unsupported object type encountered: "+type.getName());
	}

	public static Document newDocument()
	{
		try
		{
			return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		}catch(Exception ex)
		{
			return null;
		}
	}
	
	public static Element createSubelement(String name,Element parentElem,Document doc)
	{
		Element newElem=doc.createElement(name);
		
			if(parentElem!=null)
				parentElem.appendChild(newElem);
		
		return newElem;
	}
	
	public static String toString(Element element)
	{
		return new CCGFormatter().toString(element);
	}

	private static String toText(Object obj,String defVal)
	{
			if(obj==null)
				return defVal;
			
		return obj.toString();
	}
	
	public static Element addTextNode(String name,Object value,Element parentNode,Document doc,String emptyValue)
	{
		Element newElement=doc.createElement(name);
		Text text=doc.createTextNode(toText(value,emptyValue));
		newElement.appendChild(text);
		
		parentNode.appendChild(newElement);
		return newElement;
	}
	
	public static void appendText(Object value,Element parentNode,Document doc,String emptyValue)
	{
		Text text=doc.createTextNode(toText(value,emptyValue));
		parentNode.appendChild(text);
	}
	
	public static Element addTextNode(String name,Object value,Element parentNode,Document doc)
	{
		return addTextNode(name,value,parentNode,doc,"null");
	}
	
	public static Element addCDATANode(String name,Object value,Element parentNode,Document doc,String emptyValue)
	{
		Element newElement=doc.createElement(name);
		CDATASection text=doc.createCDATASection(toText(value,emptyValue));
		newElement.appendChild(text);
		
		parentNode.appendChild(newElement);
		return newElement;
	}

	public static Element addCDATANode(String name,Object value,Element parentNode,Document doc)
	{
		return addCDATANode(name,value,parentNode,doc,"null");
	}

	public static void setTextNodeValue(Element textNode,Object value,String emptyValue)
	{
		Text text=(Text)textNode.getFirstChild();
		text.setNodeValue(toText(value,emptyValue));
	}
	
	public static void setTextNodeValue(Element textNode,Object value)
	{
		setTextNodeValue(textNode,value,"null");
	}
}
