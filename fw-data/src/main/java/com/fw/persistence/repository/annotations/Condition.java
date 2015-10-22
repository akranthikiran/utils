package com.fw.persistence.repository.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.fw.persistence.Operator;

/**
 * Marks the target parameter or field as condition
 * @author akiran
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.FIELD})
public @interface Condition
{
	/**
	 * Name of the entity field on which this condition should be applied. If the target is
	 * finder method param, this is mandatory. If this annotation is used on field, this is
	 * optional.
	 * @return
	 */
	public String value() default "";
	
	/**
	 * Operator to be used in condition
	 * @return
	 */
	public Operator op() default Operator.EQ;
}
