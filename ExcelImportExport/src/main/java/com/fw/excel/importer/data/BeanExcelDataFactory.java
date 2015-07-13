package com.fw.excel.importer.data;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BeanExcelDataFactory<T> implements IExcelDataFactory<T>
{
	private static Logger logger = LogManager.getLogger(BeanExcelDataFactory.class);

	public static class FieldColumn extends Column
	{
		private Field field;
		
		public FieldColumn(String name, ColumnType type, Field field)
		{
			super(name, type, field.getType());
			this.field = field;
		}

		public Field getField()
		{
			return field;
		}
		
		@Override
		public String toString()
		{
			return super.toString() + "[Name: " + super.getName() + ", Field: " + field.getName() + "]";
		}
	}
	
	private Class<T> beanType;
	private Map<String, FieldColumn> columns = new HashMap<>();
	
	public BeanExcelDataFactory(Class<T> beanType)
	{
		this.beanType = beanType;
		
		Field fields[] = beanType.getDeclaredFields();
		ExcelLabel excelLabel = null;
		String label = null;
		ColumnType columnType = null;
		
		for(Field field: fields)
		{
			if(Modifier.isStatic(field.getModifiers()))
			{
				continue;
			}
			
			if(field.getAnnotation(ExcelIgnoreField.class) != null)
			{
				continue;
			}
			
			columnType = getColumnType(field.getType());
			
			if(columnType == null)
			{
				logger.debug("Ignoring field. Unsupported data-type '{}' encountered on field - {}", field.getType().getName(), field.getName());
				continue;
			}

			excelLabel = field.getAnnotation(ExcelLabel.class);
			label = field.getName();
			
			if(excelLabel != null)
			{
				label = excelLabel.value();
			}
			
			label = label.toLowerCase();
			
			//remove all non word characters and make into single word
			label = label.replaceAll("[\\W\\_]+", "");
			
			this.columns.put(label, new FieldColumn(label, columnType, field));
		}
	}
	
	private ColumnType getColumnType(Class<?> javaType)
	{
		if(Byte.class.equals(javaType) || byte.class.equals(javaType))
		{
			return ColumnType.INTEGER;
		}

		if(Short.class.equals(javaType) || short.class.equals(javaType))
		{
			return ColumnType.INTEGER;
		}

		if(Integer.class.equals(javaType) || int.class.equals(javaType))
		{
			return ColumnType.INTEGER;
		}

		if(Float.class.equals(javaType) || float.class.equals(javaType))
		{
			return ColumnType.FLOAT;
		}

		if(Double.class.equals(javaType) || double.class.equals(javaType))
		{
			return ColumnType.FLOAT;
		}

		if(String.class.equals(javaType))
		{
			return ColumnType.STRING;
		}

		if(Date.class.equals(javaType))
		{
			return ColumnType.DATE;
		}
		
		if(Enum.class.isAssignableFrom(javaType))
		{
			return ColumnType.ENUM;
		}

		return null;
	}
	
	@Override
	public Collection<FieldColumn> getColumns()
	{
		return columns.values();
	}
	
	public Class<?> getBeanType()
	{
		return beanType;
	}
	
	@Override
	public Column getColumn(String name)
	{
		return this.columns.get(name);
	}

	@Override
	public T newDataObject(Map<String, Object> valueMap)
	{
		try
		{
			T bean = beanType.newInstance();
			Object value = null;
			boolean accessible = false;
			
			for(FieldColumn column: this.columns.values())
			{
				value = valueMap.get(column.getName());
				
				if(value == null)
				{
					continue;
				}
				
				accessible = column.field.isAccessible();
				
				column.field.setAccessible(true);
				column.field.set(bean, value);
				
				column.field.setAccessible(accessible);
			}
			
			return bean;
		}catch(Exception ex)
		{
			logger.error("An error occurred while creating excel bean", ex);
			throw new IllegalStateException("An error occurred while creating excel bean", ex);
		}	
	}
}
