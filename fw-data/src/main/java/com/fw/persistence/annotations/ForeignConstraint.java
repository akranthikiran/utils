package com.fw.persistence.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ForeignConstraint
{
	public String name();
	
	public Mapping[] mappings();

	public MappingCondition[] conditions() default {};
	
	public MappingCondition[] parentConditions() default {};
	
	public Class<?> foreignEntity();
	
	public String message() default "";

	/**
	 * Flag to indicate whether this constraint has to be validated
	 * before insert or update
	 * @return
	 */
	public boolean validate() default true;
	
	public DeleteCascade deleteCascade() default DeleteCascade.THROW_ERROR;
}
