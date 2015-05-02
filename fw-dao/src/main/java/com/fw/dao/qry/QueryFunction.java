package com.fw.dao.qry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark static utility functions that can be invoked in the
 * queries.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryFunction
{
	/**
	 * Number of minimum arguments thats needed by this function.
	 * Rest of the arguments will be defaulted.
	 * @return Minimum number of arguments needed by current function
	 */
	public int minArgCount() default -1;
}
