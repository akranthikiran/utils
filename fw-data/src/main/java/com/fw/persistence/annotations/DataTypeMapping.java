package com.fw.persistence.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.fw.persistence.conversion.IPersistenceConverter;

/**
 * Annotation to map field type to different data type or specify 
 * @author akiran
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DataTypeMapping
{
	/**
	 * Mapped type of the field
	 * @return
	 */
	public DataType type() default DataType.UNKNOWN;
	
	/**
	 * Converter type to be used for data conversion between java type and db-types
	 * @return
	 */
	public Class<? extends IPersistenceConverter> converterType();
}
