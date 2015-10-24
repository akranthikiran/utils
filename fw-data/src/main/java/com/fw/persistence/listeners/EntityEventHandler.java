package com.fw.persistence.listeners;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Helps in marking a method as entity event handler
 * @author akiran
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EntityEventHandler
{
	/**
	 * Type of the event target method can handle
	 * @return
	 */
	public EntityEventType eventType();
	
	/**
	 * Entity types for which this method needs to be invoked. If not specified, method will 
	 * be invoked for all entity types
	 * @return
	 */
	public Class<?>[] entityTypes() default {};
}
