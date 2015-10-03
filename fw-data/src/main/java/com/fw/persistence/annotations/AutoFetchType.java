package com.fw.persistence.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * When used on entity id field, indicates whether to fetch id value (set back on input entity) 
 * after persisting the entity
 * @author akiran
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AutoFetchType
{
	/**
	 * Default true. Indicates whether id value should be fetched (set back on input entity) 
	 * after persisting the entity
	 * @return
	 */
	public boolean value() default true;
}
