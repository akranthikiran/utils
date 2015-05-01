package com.fw.persistence.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Column
{
	public static final int DEFAULT_LENGTH = 100;
	
	public String name() default "";
	
	public int length() default DEFAULT_LENGTH;
	public boolean nullable() default true;
	public DataType type() default DataType.UNKNOWN;
}
