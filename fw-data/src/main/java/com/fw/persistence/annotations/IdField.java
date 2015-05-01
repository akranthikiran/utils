package com.fw.persistence.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface IdField
{
	public AutogenerationType autogeneration() default AutogenerationType.AUTO;
	
	/**
	 * If true, id field is auto fetched after saving
	 * @return
	 */
	public boolean autofetch() default true;
	
	public String sequenceName() default "";
}
