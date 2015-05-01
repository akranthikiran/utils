package com.fw.persistence.repository.executors;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface QueryExecutorPattern
{
	public String[] prefixes() default {};
	public String[] excludePrefixes() default {};
	public Class<? extends Annotation> annotatedWith() default Annotation.class;
}
