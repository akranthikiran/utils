package conm.fw.common.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class JsonWrapper
{
	private static ObjectMapper objectMapper = new ObjectMapper();

	private String type;
	private boolean array;
	private String value;
	private String elementTypes[];
	
	public JsonWrapper()
	{}

	public JsonWrapper(String type, String value)
	{
		this.type = type;
		this.value = value;
	}

	public JsonWrapper(String type, boolean array, String elementTypes[], String value)
	{
		this.type = type;
		this.value = value;
		this.elementTypes = elementTypes;
		this.array = array;
	}
	
	public boolean isArray()
	{
		return array;
	}
	
	@JsonIgnore
	public JavaType getJavaType(ObjectMapper objectMapper) throws ClassNotFoundException
	{
		Class<?> mainType = Class.forName(type);
		
		if(elementTypes == null)
		{
			if(array)
			{
				return objectMapper.getTypeFactory().constructArrayType(mainType);
			}
			
			return SimpleType.construct(mainType);
		}
		
		Class<?> elemTypes[] = new Class<?>[elementTypes.length];
		
		for(int i = 0; i < elementTypes.length; i++)
		{
			elemTypes[i] = Class.forName(elementTypes[i]);
		}
		
		return TypeFactory.defaultInstance().constructParametricType(mainType, elemTypes);
	}
	
	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}
	
	public String[] getElementTypes()
	{
		return elementTypes;
	}

	public void setElementTypes(String[] elementTypes)
	{
		this.elementTypes = elementTypes;
	}

	public static String format(Object value)
	{
		if(value == null)
		{
			return null;
		}

		try
		{
			String jsonValue = objectMapper.writeValueAsString(value);
			String type = value.getClass().getName();
			String elementTypes[] = null;
			boolean isArray = false;
			
			if(value.getClass().isArray())
			{
				type = value.getClass().getComponentType().getName();
				isArray = true;
			}
			else if(value instanceof List)
			{
				type = ArrayList.class.getName();
				
				if(!((List<?>)value).isEmpty())
				{
					elementTypes = new String[] {
							((List<?>)value).get(0).getClass().getName()
					};
				}
			}
			else if(value instanceof Set)
			{
				type = LinkedHashSet.class.getName();
				
				if(!((Set<?>)value).isEmpty())
				{
					elementTypes = new String[] {
							((Set<?>)value).iterator().next().getClass().getName()
					};
				}
			}
			else if(value instanceof Map)
			{
				type = LinkedHashMap.class.getName();
				
				if(!((Map<?,?>)value).isEmpty())
				{
					Map.Entry<?, ?> entry = ((Map<?, ?>)value).entrySet().iterator().next();
					
					elementTypes = new String[] {
							entry.getKey().getClass().getName(),
							entry.getValue().getClass().getName()
					};
				}
			}

			JsonWrapper wrapper = new JsonWrapper(type, isArray, elementTypes, jsonValue);
			return objectMapper.writeValueAsString(wrapper);
		}catch(JsonProcessingException ex)
		{
			throw new IllegalStateException("An error occurred while converting value into json string: " + value, ex);
		}
	}
	
	public static Object parse(String valueStr)
	{
		if(valueStr == null || valueStr.trim().length() == 0)
		{
			return null;
		}
		
		try
		{
			JsonWrapper wrapper = objectMapper.readValue(valueStr, JsonWrapper.class);
			JavaType javaType = wrapper.getJavaType(objectMapper);
			
			return objectMapper.readValue(wrapper.getValue(), javaType);
		}catch(Exception ex)
		{
			throw new IllegalStateException("An error occurred while converting json-string into value: " + valueStr, ex);
		}
	}
}
