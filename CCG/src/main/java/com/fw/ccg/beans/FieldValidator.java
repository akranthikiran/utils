package com.fw.ccg.beans;

import java.io.Serializable;
import java.util.Iterator;

import com.fw.ccg.core.Attributes;

/**
 * <BR>
 * <P>
 * This interface helps in adding custom validations to the generic bean structure by using
 * setValidator() of generic bean structure. validate() methods of this interface instance
 * will be called for each field, generic bean validations.  
 * </P>
 * <BR>
 * @author A. Kranthi Kiran
 */
public interface FieldValidator extends Serializable
{
	/**
	 * <P>
	 * Called for each field during validation of generic bean. For adder fields this method is
	 * called for each value in the adder field.
	 * </P> 
	 * @param bean Generic bean being validated.
	 * @param fieldName Name of the field being validated.
	 * @param type Type of the field.
	 * @param value Value of the current field.
	 * @param flags Flags assiged to current field.
	 * @param len Maximum length specified for this field.
	 * @param attributes Attributes of this field.
	 * @param adder A flag inicating whether this field is a normal one or adder field.
	 */
	public void validate(GenericBean bean,String fieldName,Class type,Object value,int flags,int len,Attributes attributes,boolean adder);
	
	/**
	 * <P>
	 * Called for each adder field during generic bean validation. Instead of passing one value
	 * in each call, in this method call, all the values are passed as an iterator to this
	 * method.
	 * <BR>
	 * <B><I>Note, the values (iterator) being passed to this method can be null.</I></B>
	 * </P>
	 * @param bean Generic Bean being validated.
	 * @param fieldName Adder field being validated.
	 * @param type Type of this adder field.
	 * @param values Values this adder field holds, in the form of iterator. This can be null.
	 * @param flags Flags assigned to this field.
	 * @param len Length attribute of this field.
	 * @param attributes Attributes assigned to this field.
	 */
	public void validateList(GenericBean bean,String fieldName,Class type,Iterator values,int flags,int len,Attributes attributes);
}
