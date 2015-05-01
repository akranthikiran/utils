package com.fw.persistence.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Audit
{
	public String table() default "";
	public boolean insert() default true;
	public boolean update() default true;
	public boolean delete() default true;

	public String idColumn() default "AUDIT_ID";
	public String timeColumn() default "AUDIT_CHANGE_TIME";
	public String typeColumn() default "AUDIT_CHANGE_TYPE";
	public String changedByColumn() default "AUDIT_CHANGED_BY";
}
