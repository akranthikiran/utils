package com.fw.persistence.repository.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provides a mapping from entity fields to search-result properties.
 * 
 * This will be mainly needed in cases where search-result is generic, like LOV queries
 * @author akiran
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface ResultMapping
{
	/**
	 * Entity field being mapped
	 * @return
	 */
	public String entityField();
	
	/**
	 * Result property being mapped to
	 * @return
	 */
	public String property();
}
