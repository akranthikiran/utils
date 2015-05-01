package com.fw.ccg.beans;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.fw.ccg.core.Attributes;
import com.fw.ccg.core.SimpleAttributedBean;
import com.fw.ccg.core.ValidateException;
import com.fw.ccg.util.CCGUtility;

/**
 * <BR><BR>
 * 
 * <P>
 * GenericBeanStructure defines the structure of generic beans. The relationship between 
 * generic bean sructure and generic bean is like relationship between a class
 * and its instance. Structure defines the fields generic beans can hold, thier data types and some 
 * validations on the data that can be set to these fields. Just like a class can have multiple instances,
 * a generic bean structure can be associated with multiple generic beans. By default,following validations 
 * can be enforced using generic bean structure:
 * </P>
 * <OL>
 * 		<LI>
 * 			<B>Field List:</B> The fields that this bean can hold.
 * 		</LI>
 * 		<LI>
 * 			<B>Field Types:</B> The type of values each field can hold.
 * 		</LI>
 * 		<LI>
 * 			<B>Mandatory Fields:</B> Fields can be marked mandatory.
 * 		</LI>
 * 		<LI>
 * 			<B>Updateable:</B> Fields can be marked non-updateable. Fields of the 
 * 			generic beans, correpsonding to this structure, marked as non-updateable,
 * 			can not be modified after bean gets freezed.
 *		</LI>
 * 		<LI>
 * 			<B>Nullable Fields:</B><B><I>This flag can be used only with 
 * 			adder fields</I></B>. These fields can hold null values.
 * 		</LI>
 * 		<LI>
 * 			<B>Unique-Valued Fields:</B><B><I>This flag can be used only with 
 * 			adder fields</I></B>. These fields will not allow duiplicate values. The
 * 			add() method call of generic bean with duplicate value will return false,
 * 			indicating the addition is failed.
 * 		</LI>
 * 		<LI>
 * 			<B>Ordered Fields:</B><B><I>This flag can be used only with 
 * 			adder fields</I></B>. The data in this fields will be maintained in the
 * 			order they are added to this field.
 * 			<BR>
 * 			Note: By default, all adder fields are ordered. But, when the field is marked
 * 			as unique, for the sake of efficiency order is ignored. So if a field needs to
 * 			be unique and ordered, then these flags should be specified explicitly.
 * 		</LI>
 *
 * </OL>
 * <P>
 * <U><B>Custom Validations</U></B>
 * <BR><BR>
 * Other than the above mentioned validations, custom validations can be imposed on the 
 * fields by using com.ccg.beans.FieldValidator. Look in validate() methods documentation
 * for more details. 
 * <BR>
 *  Note: Field attrbites/flags can be used for specifying parameters for validation, which in 
 *  turn can be used in FieldValidator.
 * </P>
 * 
 * <P>
 * <U><B>Freezing the Structure</U></B>
 * <BR><BR>
 * 
 * New fields can be added or exisitng field attributes can be modified till the structure gets freezed.
 * A Structure gets freezed when at leaset one genric bean is created using this structure, it
 * can be using constructor of GenricBean or by using newInstance() of GenericBeanStructure.
 * The structure may also gets freezed when the structure instance is assigned as structure for existing
 * genric bean using setStructure() of GenericBean.
 * <BR>
 * Note, The freezed status of the structure will not be transfered to new structures during cloning()
 * or during the process of serialization/deserialization.
 * </P>
 * 
 * <P>
 * <U><B>Type of Structure</U></B><BR><BR>
 * In the constructor of GenericBeanStructure the type (actually a name) of structure can be 
 * specified. This value will not be used in any validation including equality check between
 * structures. It is just meant for reference/identity for applications using generic bean structures.
 * </P>
 * <BR>
 * @author A. Kranthi Kiran
 */
public class GenericBeanStructure implements Serializable,Cloneable
{
	/**
	 * Mandatory flag for fields. Madatory check will be peroformed only
	 * during validation of bean.  For normal fields, this flag indicates value can not
	 * be null. For adder fields, values can not be null and cannot be empty.
	 */
	public static final int MANDATORY=1;
	
	/**
	 * A non-updateable field can not be updated once the enclosing bean is freezed.
	 */
	public static final int UPDATEABLE=2;
	
	/**
	 * <B><I>This flag is applicable only for adder fields.</I></B> This flag indicates
	 * that this adder field can hold null values.
	 */
	public static final int NULLABLE=4;
	
	/**
	 * <B><I>This flag is applicable only for adder fields.</I></B> This flag indicates
	 * that this field will not contain duplicate values.
	 */
	public static final int UNIQUE_VALUED=8;
	
	/**
	 * <B><I>This flag is applicable only for adder fields.</I></B> This flag indicates 
	 * that the vlues in this field will be in the same order in which they are added.
	 */
	public static final int ORDERED=16;//indicator flag
		/**
		 * <BR>
		 * @author A. Kranthi Kiran
		 * <BR><BR>
		 * This class represents field of generic structure class.
		 */
		private static class Field implements Serializable,Cloneable
		{
			private static final long serialVersionUID=1L;
			
			Class type;
			int flags=UPDATEABLE;
			int length=-1;
			FieldName fldName;
			SimpleAttributedBean attributes;
				/**
				 * Creates a field instance with specified attributes.
				 * @param fldName
				 * @param flags
				 * @param len
				 * @param type
				 */
				public Field(FieldName fldName,int flags,int len,Class type)
				{
						if(fldName==null || fldName.getName().length()==0)
							throw new IllegalArgumentException("Field name can not be null or empty.");
						
						if(type==null)
							throw new NullPointerException("Field type cannot be null.");
						
					this.flags=flags;
					
						if(len<=0)
							len=-1;
						
						if(type!=null && type.isPrimitive())
							type=CCGUtility.getWrapperClass(type);
					this.type=type;
					this.fldName=fldName;
					this.length=len;
				}
				
				/**
				 * Used for cloning purpose. 
				 */
				protected Field()
				{}
				
				
				/**
				 * Adds attribute with speified name and value.
				 * @param name
				 * @param value
				 */
				public void addAttribute(String name,Object value)
				{
						if(attributes==null)
							attributes=new SimpleAttributedBean();
					attributes.setAttribute(name,value);
				}
				
				/**
				 * @param name
				 * @return Value of specified attribute.
				 */
				public Object getAttribute(String name)
				{
					return attributes.getAttribute(name);
				}
				
				/**
				 * @return Attributes list.
				 */
				public Attributes getAttributes()
				{
					return attributes;
				}
				
				/**
				 * Sets specified flags to this field.
				 * @param flags
				 */
				public void setFlags(int flags)
				{
					this.flags=flags;
				}
				
				/**
				 * @return Flags assigned to this field.
				 */
				public int getFlags()
				{
					return flags;
				}
				
				/**
				 * @return Whether the field is marked mandatory.
				 */
				public boolean isMandatory()
				{
					return ((flags&MANDATORY)==MANDATORY);
				}
				
				/**
				 * Sets the mandatory flag to this field as specified.
				 * @param mandatory
				 */
				public void setMandatory(boolean mandatory)
				{
						if(mandatory)
							flags=flags|MANDATORY;
						else
							flags=(flags&(~MANDATORY));
				}
				
				/**
				 * @return If this field is updateable.
				 */
				public boolean isUpdateable()
				{
					return ((flags&UPDATEABLE)==UPDATEABLE);
				}
				
				/**
				 * Sets the updateable flag to this field as specified.
				 * @param updateable
				 */
				public void setUpdateable(boolean updateable)
				{
						if(updateable)
							flags=(flags|UPDATEABLE);
						else
							flags=(flags&(~UPDATEABLE));
				}
				
				/**
				 * @return Name of this field.
				 */
				public final String getName()
				{
					return fldName.getName();
				}
				
				/**
				 * @return Type of data this field can hold.
				 */
				public Class getType()
				{
					return type;
				}

				/**
				 * Sets type of data this field can hold.
				 * @param type
				 */
				public void setType(Class type)
				{
						if(type.isPrimitive())
							type=CCGUtility.getWrapperClass(type);
					this.type=type;
				}
				
				/**
				 * Specifies maximum length for this field. This value will be used only
				 * for string fields. If len<=0 , then it will be converted into -1 and 
				 * will not be considered for validation.
				 * @param len
				 */
				public void setLength(int len)
				{
						if(len<=0)
							len=-1;
					this.length=len;
				}
				
				/**
				 * @return Max length assigned to this field.
				 */
				public int getLength()
				{
					return length;
				}
				
				/**
				 * Validates the specified value aganist the validation attributes specified
				 * on this field. 
				 * <BR>
				 * This method is called when validate methods are called on associated 
				 * GenericBeanStructure instance.
				 * @param value New value for this field.
				 */
				public void validate(Object value)
				{
						if(value==null)
							return;
						
						if(!type.isAssignableFrom(value.getClass()))
							throw new FieldException(fldName.getName(),type,value.getClass());
						
						if((value instanceof String) && length>0)
						{
							int len=((String)value).length();
								if(len>length)
									throw new FieldException("Specified value for field \""+fldName.getName()+
													"\" exceeds max length("+length+"): "+value);
						}
						
				}
				
				/**
				 * Appends string representation of this field to res. The string represntation
				 * will be in a single line.
				 * @param res String buffer to which string represntation need to be appended. 
				 */
				public void toString(StringBuffer res)
				{
					res.append(fldName.getName());
					res.append("(");
					res.append(type.getName());
					res.append(")\t");
						if(this.length>0)
						{
							res.append("(Max length: ");
							res.append(this.length);
							res.append(") ");
						}
						
						if(isMandatory())
							res.append("Mandatory,");
						
						if(isUpdateable())
							res.append("Updateable,");
					
					int len=res.length();
					//remove comma or tab at the end which might not be needed
					res.delete(len-1,len);
				}
				
				/* (non-Javadoc)
				 * @see java.lang.Object#clone()
				 */
				public Object clone()
				{
					Field res=new Field();
					res.type=type;
					res.flags=flags;
					res.length=length;
					res.fldName=fldName;
					
						if(attributes!=null)
							res.attributes=(SimpleAttributedBean)attributes.clone();
						
					return res;
				}
				
				/**
				 * Value returned by this method will be equal to the hashCode() value of
				 * field name + type + flags + attributes. And for string class, length also gets 
				 * added.
				 * @see java.lang.Object#hashCode()
				 */
				public int hashCode()
				{
					int res=fldName.hashCode()+type.hashCode()+flags;
						if(String.class.equals(type))
							res=res+length;
						
						if(attributes!=null)
							res=res+attributes.hashCode();
					return res;
				}

				/**
				 * Two fields are equal when thoer name and all the attributes are equal.
				 * Length attribute is considered only for fields of type String.
				 * @see java.lang.Object#equals(java.lang.Object)
				 */
				public boolean equals(Object other)
				{
						if(this==other)
							return true;
						
						if(!(other instanceof Field))
							return false;
					Field otherFld=(Field)other;
						if(!type.equals(otherFld.type) || flags!=otherFld.flags ||
								!fldName.equals(otherFld.fldName))
							return false;
						
						//consider length only if field type is String.
						if(String.class.equals(type) && length!=otherFld.length)
							return false;

						if(attributes!=null)
						{
							if(!attributes.equals(otherFld.attributes))
								return false;
						}
						else if(otherFld.attributes!=null)
							return false;
						
					return true;
				}
		}
		
		/**
		 * This class represents adder field of the generic class.
		 * <BR>
		 * @author A. Kranthi Kiran
		 */
		private static class AdderField extends Field
		{
			private static final long serialVersionUID=1L;
			
				/**
				 * Creates a field instance with specified attributes.
				 * @param name
				 * @param mandatory
				 * @param updateable
				 * @param len
				 * @param type
				 */
				public AdderField(FieldName fldName,int flags,int len,Class type)
				{
					super(fldName,flags,len,type);
				}
				
				/**
				 * Used for cloning purpose. 
				 */
				protected AdderField()
				{}
				
				public boolean isUniqueValued()
				{
					int flags=getFlags();
					return ((flags&UNIQUE_VALUED)==UNIQUE_VALUED);
				}
				
				public boolean isOrdered()
				{
					int flags=getFlags();
					return ((flags&ORDERED)==ORDERED);
				}
				
				public boolean isNullable()
				{
					int flags=getFlags();
					return ((flags&NULLABLE)==NULLABLE);
				}
				
				public void setNullable(boolean nullable)
				{
					int flags=getFlags();
						if(nullable)
							flags=(flags|NULLABLE);
						else
							flags=(flags&(~NULLABLE));
					setFlags(flags);
				}
				
				/**
				 * Validates the specified value aganist the validation attributes specified
				 * on this field.  For non-nullable fields value can not be null.
				 * <BR>
				 * This method is called when validate methods are called on associated 
				 * GenericBeanStructure instance.
				 * @param value New value for this field.
				 */
				public void validate(Object value)
				{
						if(value==null)
						{
								if(!isNullable())
									throw new FieldException("Null values cannot be assigned to field: "+getName());
							return;
						}
						
					Class type=getType();
						if(!type.isAssignableFrom(value.getClass()))
							throw new FieldException(getName(),type,value.getClass());
					
					int len=getLength();
						if((value instanceof String) && len>0)
						{
							int length=((String)value).length();
								if(length>len)
									throw new FieldException("Specified value for field \""+getName()+
													"\" exceeds max length("+len+"): "+value);
						}
				}
				
				/**
				 * This validation is performed at the field level. That is, for all the
				 * values this field holds. This method will be called during bean level
				 * validation.
				 * @param data Data to be validated.
				 */
				public void validateList(Object data)
				{
						if(data==null)
							return;
					
						if(!(data instanceof Iterator))
							throw new FieldException("The value for adder field should be a java.util.Iterator: "+getName());
				
					Object value=null;
					Iterator it=(Iterator)data;
					Class type=getType();
					int len=getLength();
					String name=getName();
					boolean nullable=isNullable();
						while(it.hasNext())
						{
							value=it.next();
								if(value==null && !nullable)
									throw new FieldException("Null encountered in non-nullable adder field: "+name);
								
								if(value==null)
									continue;
								
								if(!type.isAssignableFrom(value.getClass()))
									throw new FieldException(name,type,value.getClass());
					
								if((value instanceof String) && len>0)
								{
									int length=((String)value).length();
										if(length>len)
											throw new FieldException("Specified value for field \""+name+
															"\" exceeds max length("+len+"): "+value);
								}
						}
				}
				
				/**
				 * Appends string representation of this field to res. The string represntation
				 * will be in a single line.
				 * @param res String buffer to which string represntation need to be appended. 
				 */
				public void toString(StringBuffer res)
				{
					res.append(getName());
					res.append("*(");
					res.append(getType().getName());
					res.append(")\t");
					int len=getLength();
						if(len>0)
						{
							res.append("(Max length: ");
							res.append(len);
							res.append(") ");
						}
						
						if(isMandatory())
							res.append("Mandatory,");
						
						if(isUpdateable())
							res.append("Updateable,");
					
						if(isNullable())
							res.append("Nullable,");
						
					len=res.length();
					//remove comma or tab at the end which might not be needed
					res.delete(len-1,len);
				}
		}

	private static final long serialVersionUID=1L;
	
	/**
	 * Hols name to field mapping. The keys will be FieldName instances.
	 */
	private Map nameToField=new HashMap();
	
	/**
	 * Type identification of this generic bean structure.
	 */
	private String type;
	
	/**
	 * This flag will be true, if atleast one generic bean is associated with this
	 * structure.
	 */
	private transient boolean freezed=false;
	
	/**
	 * Hash code of this structure if it is already calculated. 
	 */
	private Integer hashCode=null;
	
	/**
	 * Holds alias to field name mapping. Both keys and values will be FieldName instances.
	 */
	private Map aliasToName=null;
	
	/**
	 * A Field validator for custom validations.
	 */
	private FieldValidator validator;
	
		/**
		 * Constructs generic bean structure without type string.
		 */
		public GenericBeanStructure()
		{
		}
		
		/**
		 * The type is not used anywhere not even in equals() method. Type of structure is just
		 * meant for user reference/identification. But this value cannot be changed once the 
		 * structure is created.
		 * @param type A name indicating the structure type. Can be null.
		 */
		public GenericBeanStructure(String type)
		{
			this.type=type;
		}
		
		/**
		 * Used by generic bean constructor accepting generic bean strcture, requesting to 
		 * freeze this structure. This structure will be freezed only if specified generic bean's
		 * structure is same as this structure.
		 * 
		 * @param bean
		 */
		final void freeze(GenericBean bean)
		{
				if(freezed)
					return;
				
				if(bean!=null && bean.getStructure()==this)
					freezed=true;
		}
		
		/**
		 * @return freezed status of this structure.
		 */
		public final boolean isFreezed()
		{
			return freezed;
		}
		
		/**
		 * Adds specified alias name to the specified field.
		 * <BR>
		 * This operation can not be performed on freezed structures.
		 * <BR>
		 * @param name
		 * @param alias
		 */
		public void addAlias(String name,String alias)
		{
				if(freezed)
					throw new UnsupportedOperationException("This opeation is not supported on freezed generic bean structure.");
				
			Field fld=(Field)nameToField.get(new FieldName(name));
				if(fld==null)
					throw new IllegalArgumentException("No field exist with name: "+name);
				
				if(alias==null)
					throw new NullPointerException("Alias name cannot be null.");
				
				if(name.equals(alias))
					throw new IllegalArgumentException("Alias name and field name cannot be same.");
			
			FieldName aliasName=new FieldName(alias);
			
				if(aliasToName==null)
					aliasToName=new HashMap();
				else if(aliasToName.containsKey(aliasName))
					throw new IllegalArgumentException("Duplicate alias name encountered: "+alias);
				
			aliasToName.put(aliasName,fld.fldName);
		}
		
		/**
		 * This method can be used to check whether a field exists with the specified name.
		 * This methods returns the actual referece of the mapped field name. Generic beans created
		 * by this structre are expected to use this reference, for the field
		 * names. So that all generic beans of this structure will use same field name reference.
		 * Which helps in improving memory efficiency.
		 * @param name Name of the field.
		 * @return actual reference of the field name (if exists). Returns null if no field
		 * 			exists with specified name.
		 */
		protected final FieldName hasField(String name)
		{
			FieldName fname=new FieldName(name);
			Field fld=(Field)nameToField.get(fname);
				if(fld==null)
				{
						if(aliasToName!=null)
						{
							FieldName fldName=(FieldName)aliasToName.get(fname);
								if(fldName!=null)
								{
									fld=(Field)nameToField.get(fldName);
									return fld.fldName;
								}
						}
					return null;
				}
			return fld.fldName;
		}
		
		/**
		 * @param name
		 * @return If the strucutre contains field with specified name.
		 */
		public boolean isField(String name)
		{
			return (hasField(name)!=null);
		}
		
		/**
		 * Returns Field instance corresponding to specified name.
		 * @param name
		 * @return
		 */
		private Field getField(String name)
		{
			FieldName fname=new FieldName(name);
			Field fld=(Field)nameToField.get(fname);
				if(fld==null)
				{
						if(aliasToName!=null)
						{
							FieldName fldName=(FieldName)aliasToName.get(fname);
								if(fldName!=null)
								{
									fld=(Field)nameToField.get(fldName);
									return fld;
								}
						}
					return null;
				}
			return fld;
		}
		
		/**
		 * Assigns an attribute to the speicified field with speicified value.
		 * @param field Name of the field.
		 * @param name Name of the attribute.
		 * @param value Value for the attribute.
		 */
		public void setAttribute(String field,String name,Object value)
		{
				if(freezed)
					throw new UnsupportedOperationException("This opeation is not supported on freezed generic bean structure.");
				
			Field fld=getField(field);
				if(fld==null)
					throw new IllegalArgumentException("No field found with name: "+field);
			
			fld.addAttribute(name,value);
		}
		
		/**
		 * @param field Name of the field.
		 * @param name Name of the attribute.
		 * @return Attribute value of the specified field.
		 */
		public Object getAttribute(String field,String name)
		{
			Field fld=getField(field);
				if(fld==null)
					throw new IllegalArgumentException("No field found with name: "+field);
				
			return fld.getAttribute(name);	
		}
		
		/**
		 * @param name
		 * @return True, if specified name represents adder field. 
		 */
		public boolean isAdderField(String name)
		{
			FieldName fname=new FieldName(name);
			Field fld=(Field)nameToField.get(fname);
				if(fld==null)
				{
						if(aliasToName!=null)
						{
							FieldName fldName=(FieldName)aliasToName.get(fname);
								if(fldName!=null)
								{
									fld=(Field)nameToField.get(fldName);
									return (fld instanceof AdderField);
								}
						}
					return false;
				}
			return (fld instanceof AdderField);
		}
		
		/**
		 * @return Type string of this generic bean structure.
		 */
		public String getType()
		{
			return type;
		}
		
		/**
		 * This method will throw UnsupportedOperationException if this structure is 
		 * already freezed. 
		 * <BR>
		 * Alters the type of the specified field to the specified type.
		 * @param field
		 * @param type
		 */
		public void setType(String field,Class type)
		{
				if(freezed)
					throw new UnsupportedOperationException("This opeation is not supported on freezed generic bean structure.");
				
			Field fld=getField(field);
				if(fld==null)
					throw new FieldException("Specified field not found: "+field);
				
				if(type==null)
					throw new IllegalArgumentException("Type argument cannot be null.");
			fld.setType(type);
			hashCode=null;
		}
		
		/**
		 * @param field Field name.
		 * @return Type of the specified field.
		 */
		public Class getType(String field)
		{
			Field fld=getField(field);
				if(fld==null)
					throw new FieldException("Specified field not found: "+field);
			return fld.getType();
		}
		
		/**
		 * This method will throw UnsupportedOperationException if this structure is 
		 * already freezed. 
		 * <BR>
		 * If not freezed, then this field updateable flag status will be altered 
		 * as specified.
		 * <BR>
		 * @param field Name of the existing field.
		 * @param updateable Whether this field should be updateable.
		 */
		public void setUpdateable(String field,boolean updateable)
		{
				if(freezed)
					throw new UnsupportedOperationException("This opeation is not supported on freezed generic bean structure.");
				
			Field fld=getField(field);
				if(fld==null)
					throw new FieldException("Specified field not found: "+field);
			fld.setUpdateable(updateable);
			hashCode=null;
		}
		
		/**
		 * @param field
		 * @return Updateable status of specified field.
		 */
		public boolean isUpdateable(String field)
		{
			Field fld=getField(field);
				if(fld==null)
					throw new FieldException("Specified field not found: "+field);
			return fld.isUpdateable();
		}
		
		/**
		 * Marks nullable flag of the specified field as specified. This method can be called
		 * only on adder fields.
		 * @param field Name of the field.
		 * @param nullable Nullable flag.
		 */
		public void setNullable(String field,boolean nullable)
		{
				if(freezed)
					throw new UnsupportedOperationException("This opeation is not supported on freezed generic bean structure.");
				
			Field fld=getField(field);
				if(fld==null)
					throw new FieldException("Specified field not found: "+field);
				
				if(!(fld instanceof AdderField))
					throw new FieldException("This operation can not be performed on normal field: "+field);
				
			((AdderField)fld).setNullable(nullable);
			hashCode=null;
		}
		
		/**
		 * @param field
		 * @return Nullable flag of specified field.
		 */
		public boolean isNullable(String field)
		{
			Field fld=getField(field);
				if(fld==null)
					throw new FieldException("Specified field not found: "+field);
				
				if(!(fld instanceof AdderField))
					throw new FieldException("This operation can not be performed on normal field: "+field);
			return ((AdderField)fld).isNullable();
		}
		
		/**
		 * This method will throw UnsupportedOperationException if this structure is already freezed. 
		 * <BR>
		 * If not freezed, then this field mandatory flag status will be altered as specified.
		 * @param field Exisitng field name.
		 * @param mandatory  Whether this field is mandatory.
		 */
		public void setMandatory(String field,boolean mandatory)
		{
				if(freezed)
					throw new UnsupportedOperationException("This opeation is not supported on freezed generic bean structure.");
			
			Field fld=getField(field);
				if(fld==null)
					throw new FieldException("Specified field not found: "+field);
			fld.setMandatory(mandatory);
			hashCode=null;
		}
		
		/**
		 * Unlike other methods this method will not validate specfied flags aganist the
		 * field type (normal or adder). This method is provided for the sake flag
		 * customization which inturn can be used in FieldValidator.
		 * @param field Name of the field.
		 * @param flags Flags for this field.
		 */
		public void setFlags(String field,int flags)
		{
				if(freezed)
					throw new UnsupportedOperationException("This opeation is not supported on freezed generic bean structure.");
			
			Field fld=getField(field);
				if(fld==null)
					throw new FieldException("Specified field not found: "+field);
			fld.setFlags(flags);
			hashCode=null;
		}
		
		/**
		 * @param field
		 * @return Flags assigned to specified field.
		 */
		public int getFlags(String field)
		{
			Field fld=getField(field);
				if(fld==null)
					throw new FieldException("Specified field not found: "+field);
			return fld.getFlags();
		}
		
		/**
		 * @param field
		 * @return Whether field "field" is mandatory or not.
		 */
		public boolean isMandatory(String field)
		{
			Field fld=getField(field);
				if(fld==null)
					throw new FieldException("Specified field not found: "+field);
			return fld.isMandatory();
		}
		
		/**
		 * @param field
		 * @return Whether field "field" is ordered or not.
		 */
		public boolean isOrdered(String field)
		{
			Field fld=getField(field);
				if(fld==null)
					throw new FieldException("Specified field not found: "+field);
				
				if(!(fld instanceof AdderField))
					throw new FieldException("This operation can not be performed on normal field: "+field);
			return ((AdderField)fld).isOrdered();
		}
		
		/**
		 * @param field
		 * @return Whether field "field" is unique-valued or not.
		 */
		public boolean isUniqueValued(String field)
		{
			Field fld=getField(field);
				if(fld==null)
					throw new FieldException("Specified field not found: "+field);
				
				if(!(fld instanceof AdderField))
					throw new FieldException("This operation can not be performed on normal field: "+field);
			return ((AdderField)fld).isUniqueValued();
		}
		
		/**
		 * This method will throw UnsupportedOperationException if this structure is already freezed. 
		 * <BR>
		 * If not freezed, then len will be assigned as length to the specified field. Even
		 * though no exceptions are thrown, length property is used only for String fields.
		 * Length less or equal to zero indicate infinite length and will be convered internally
		 * into -1.
		 * 
		 * @param field The existing field name.
		 * @param len The length of the field.
		 */
		public void setLength(String field,int len)
		{
				if(freezed)
					throw new UnsupportedOperationException("This opeation is not supported on freezed generic bean structure.");

			Field fld=getField(field);
				if(fld==null)
					throw new FieldException("Specified field not found: "+field);
			fld.setLength(len);
			hashCode=null;
		}
		
		/**
		 * @param field
		 * @return Length of the specified field.
		 */
		public int getLength(String field)
		{
			Field fld=getField(field);
				if(fld==null)
					throw new FieldException("Specified field not found: "+field);
			int len=fld.getLength();
				if(len>0)
					return len;
			return -1;
		}
		
		/**
		 * Sets the specified validator to this structure for sake of custom validations.
		 * @param validator
		 */
		public void setValidator(FieldValidator validator)
		{
				if(freezed)
					throw new UnsupportedOperationException("This operation can not be performed on freezed generic bean structure.");
			
			this.validator=validator;
		}
		
		/**
		 * @return Validator assigned to this structure.
		 */
		public FieldValidator getValidator()
		{
			return validator;
		}
		
		/**
		 * This method will throw UnsupportedOperationException if this structure is already freezed. 
		 * <BR>
		 * This method is used to add field to this generic bean structure.
		 * @param fld New field.
		 */
		private void addField(Field fld)
		{
				if(freezed)
					throw new UnsupportedOperationException("This opeation is not supported on freezed generic bean structure.");
			
			FieldName fldName=fld.fldName;
			
				if(nameToField.containsKey(fldName))
					throw new FieldException("Duplicate field name encountered: "+fld.getName());
			
			nameToField.put(fldName,fld);
			hashCode=null;
		}
		
		/**
		 * This method will throw UnsupportedOperationException if this structure is already freezed. 
		 * <BR>
		 * If not freezed, then a new field will be added with the specified details.
		 * 
		 * @param name Name of the field. (cannot be exiting name).
		 * @param flags ORed flags for this field.
		 * @param len Length of the field. Effective only for fields of type String. Less than
		 * 				or equal to zero idicates infinite length.
		 * @param type  Type of the field. (cannot be null)
		 */
		public void addField(String name,int flags,int len,Class type)
		{
				if(freezed)
					throw new UnsupportedOperationException("This opeation is not supported on freezed generic bean structure.");
				
				if(type==null)
					throw new FieldException("Field type cannot be null");
			addField(new Field(new FieldName(name),flags,len,type));
		}
		
		/**
		 * This method will throw UnsupportedOperationException if this structure is already freezed. 
		 * <BR>
		 * If not freezed, then a new adder field will be added with the specified details.
		 * 
		 * @param name
		 * @param flags
		 * @param len
		 * @param type
		 */
		public void addAdderField(String name,int flags,int len,Class type)
		{
				if(freezed)
					throw new UnsupportedOperationException("This opeation is not supported on freezed generic bean structure.");
				
				if(type==null)
					throw new FieldException("Field type cannot be null");
			addField(new AdderField(new FieldName(name),flags,len,type));
		}
		
		/**
		 * This method will throw UnsupportedOperationException if this structure is already freezed. 
		 * <BR>
		 * If not freezed, then a new field will be added with the specified details. Updateable
		 * flag is meaked as default.
		 * 
		 * @param name
		 * @param isMandatory
		 * @param type
		 */
		public void addField(String name,boolean isMandatory,Class type)
		{
				if(isMandatory)
					addField(name,MANDATORY | UPDATEABLE,-1,type);
				else
					addField(name,UPDATEABLE,-1,type);
		}
		
		/**
		 * This method will throw UnsupportedOperationException if this structure is already freezed. 
		 * <BR>
		 * If not freezed, then a new adder field will be added with the specified details.
		 * Updateable and nullable flags are marked by default.
		 * 
		 * @param name
		 * @param isMandatory
		 * @param type
		 */
		public void addAdderField(String name,boolean isMandatory,Class type)
		{
				if(isMandatory)
					addAdderField(name,MANDATORY | UPDATEABLE | NULLABLE,-1,type);
				else
					addAdderField(name,UPDATEABLE | NULLABLE,-1,type);
		}
		
		/**
		 * This method will throw UnsupportedOperationException if this structure is already freezed. 
		 * <BR>
		 * If not freezed, then a new adder field will be added with the specified details.
		 * Updateable flag is marked by default.
		 * 
		 * @param name
		 * @param isMandatory
		 * @param len
		 * @param type
		 */
		public void addField(String name,boolean isMandatory,int len,Class type)
		{
				if(isMandatory)
					addField(name,MANDATORY | UPDATEABLE,len,type);
				else
					addField(name,UPDATEABLE,len,type);
		}
		
		/**
		 * This method will throw UnsupportedOperationException if this structure is already freezed. 
		 * <BR>
		 * If not freezed, then a new adder field will be added with the specified details.
		 * Updateable and nullable flag are marked by default.
		 * 
		 * @param name
		 * @param isMandatory
		 * @param len
		 * @param type
		 */
		public void addAdderField(String name,boolean isMandatory,int len,Class type)
		{
				if(isMandatory)
					addAdderField(name,MANDATORY | UPDATEABLE | NULLABLE,len,type);
				else
					addAdderField(name,UPDATEABLE | NULLABLE,len,type);
		}
		
		/**
		 * This method will throw UnsupportedOperationException if this structure is already freezed. 
		 * <BR>
		 * If not freezed, then a new adder field will be added with the specified details.
		 * Updateable flag is marked by default.
		 * 
		 * @param name
		 * @param type
		 */
		public void addField(String name,Class type)
		{
			addField(name,UPDATEABLE,-1,type);
		}
		
		/**
		 * This method will throw UnsupportedOperationException if this structure is already freezed. 
		 * <BR>
		 * If not freezed, then a new adder field will be added with the specified details.
		 * Updateable and nullable flag are marked by default.
		 * 
		 * @param name
		 * @param type
		 */
		public void addAdderField(String name,Class type)
		{
			addAdderField(name,UPDATEABLE | NULLABLE,-1,type);
		}
		
		/**
		 * This method will throw UnsupportedOperationException if this structure is already freezed. 
		 * <BR>
		 * If not freezed, then a new adder field will be added with the specified details.
		 * Updateable flag is marked by default.
		 * 
		 * @param name
		 * @param len
		 * @param type
		 */
		public void addField(String name,int len,Class type)
		{
			addField(name,UPDATEABLE,len,type);
		}
		
		/**
		 * This method will throw UnsupportedOperationException if this structure is already freezed. 
		 * <BR>
		 * If not freezed, then a new adder field will be added with the specified details.
		 * Updateable and nullable flag are marked by default.
		 * 
		 * @param name
		 * @param len
		 * @param type
		 */
		public void addAdderField(String name,int len,Class type)
		{
			addAdderField(name,UPDATEABLE | NULLABLE,len,type);
		}
		
		/**
		 * Validates all the field and thier correspoding values aganist the specified
		 * constraints. Also a check for mandatory fields will be performed, if checkMandatory
		 * is true. If checkFields is true then this method method makes sure that specified
		 * bean contains only fields, that are specified by this strucure 
		 * <BR>
		 * 
		 * <B>Note:</B>This method will not check whether the generic bean instance specified
		 * is created using this structure.
		 * 
		 * <BR>
		 * For adder fields validateList() of FieldValidtor will be called and for normal
		 * fields  validate() of FieldValidator will be called.
		 * 
		 * @param bean Generic bean that needs to be validated.
		 * @param checkMandatory Specifies whether mandatory constraints on the field should be
		 * 							performed.
		 * @param checkFields Specifies whether the additional fields presence check should be 
		 * 						performed. 
		 * @throws BeanValidationException
		 */
		public void validate(GenericBean bean,boolean checkMandatory,boolean checkFields) throws ValidateException
		{
				if(bean==null)
					throw new NullPointerException("Bean cannot be null.");
			Iterator fields=nameToField.keySet().iterator();
			FieldName fieldName=null;
			Object value=null;
			Field fld=null;
			Iterator valueIterator=null;
				while(fields.hasNext())
				{
					fieldName=(FieldName)fields.next();
					fld=(Field)nameToField.get(fieldName);
					
					
						if(fld instanceof AdderField)
						{
								if(!bean.isAdderField(fld.getName()))
									throw new ValidateException("Field \""+fld.getName()+"\" is expected to be adder field.");
								
							value=bean.getAdderField(fld.getName());
							valueIterator=(Iterator)value;
							
								if(checkMandatory && fld.isMandatory() && (value==null || !valueIterator.hasNext()))
									throw new ValidateException("Mandatory adder field value is missing or empty: "+fieldName);
								
							((AdderField)fld).validateList(value);
							
								if(validator!=null)
								{
									value=bean.getAdderField(fld.getName());
									validator.validateList(bean,fieldName.getName(),
											fld.getType(),(Iterator)value,fld.getFlags(),
											fld.getLength(),fld.getAttributes());
								}
						}
						else
						{
								if(bean.isAdderField(fld.getName()))
									throw new ValidateException("Field \""+fld.getName()+"\" is expected to be normal field.");
							value=bean.getField(fld.getName());
							
								if(checkMandatory && fld.isMandatory() && value==null)
									throw new ValidateException("Mandatory field value is missing: "+fieldName);
								
							fld.validate(value);
							
								if(validator!=null)
								{
									validator.validate(bean,fieldName.getName(),fld.getType(),
												value,fld.getFlags(),fld.getLength(),
												fld.getAttributes(),(fld instanceof AdderField));
								}
						}
				}
				
				if(checkFields)
				{
					Iterator beanFields=bean.getFieldList();
					Object field=null;
						while(beanFields.hasNext())
						{
							field=beanFields.next();
								if(!nameToField.containsKey(field))
									throw new FieldException("Additional field encountered in the bean: "+field);
						}
				}
		}
		
		/**
		 * Validate the specifie value aganist the constraints placed on the specified Field.
		 * <BR>
		 * This method will call validate() method of FieldValidator.
		 * @param field
		 * @param value
		 */
		public void validateValue(GenericBean bean,String field,Object value)
		{
			Field fld=(Field)nameToField.get(new FieldName(field));
				if(fld==null)
					throw new FieldException("Invalid field name specified: "+field);

			fld.validate(value);
			
				if(validator!=null)
				{
					validator.validate(bean,field,fld.getType(),value,
							fld.getFlags(),fld.getLength(),fld.getAttributes(),(fld instanceof AdderField));
				}
		}
		
		/**
		 * A new instance of generic bean controlled by this structure will be returned.
		 * After creation of the genric bean this structure instance will gets freezed.
		 * That is, after this method call, calls to alter this structure will throw 
		 * UnsupportedOperationException, thus making this instance immutable.
		 * <BR><BR>
		 * <B>Note:</B> The instances created using this instance (by cloning or by serialization) 
		 * will not share the freezed status of this instance. 
		 * @return A generic bean controlled by this structure.
		 */
		public GenericBean newInstance()
		{
			GenericBean bean=new GenericBean(this);
			return bean;
		}

		/**
		 * @return Iterator of field names. This iterator will not support remove() operation.
		 */
		public Iterator getFieldNames()
		{
			final Iterator it=nameToField.keySet().iterator();
			Iterator res=new Iterator()
				{

					public boolean hasNext()
					{
						return it.hasNext();
					}

					public Object next()
					{
						return ((FieldName)it.next()).getName();
					}

					public void remove()
					{
						throw new UnsupportedOperationException("Remove method not supported by generic bean structure name iterator.");
					}
				};
			return res;
		}
		
		/**
		 * Returns a string in multiple lines. First line will have the type and the following 
		 * lines will have field information, one field per line.
		 * <BR>
		 * @see java.lang.Object#toString()
		 */
		public String toString()
		{
			StringBuffer res=new StringBuffer(super.toString());
			res.append("\nType: ");
			res.append(type);
			res.append("(\n");
			
			Iterator it=nameToField.keySet().iterator();
			Object name=null;
			Field fld=null;
				while(it.hasNext())
				{
					name=it.next();
					fld=(Field)nameToField.get(name);
					
					fld.toString(res);
					
						if(it.hasNext())
							res.append("\n");
				}
			res.append("\n)");
			return res.toString();
		}
		
		/**
		 * The resultant cloned object will share the same field name references.
		 * 
		 * <BR><BR>
		 * <B>Note:</B> The resulting cloned structure will not be parent structure of the
		 * beans created using this structure. Thus the cloned structure is mutable.
		 * @see java.lang.Object#clone()
		 */
		public Object clone()
		{
			GenericBeanStructure res=new GenericBeanStructure();
			Iterator it=nameToField.keySet().iterator();
			Object name=null;
			Field fld=null;
			res.type=type;
			res.hashCode=hashCode;
				while(it.hasNext())
				{
					name=it.next();
					fld=(Field)nameToField.get(name);
					
					res.nameToField.put(name,fld.clone());
				}
			
				if(aliasToName!=null)
				{
					res.aliasToName=new HashMap();
					it=aliasToName.keySet().iterator();
						while(it.hasNext())
						{
							name=it.next();
							res.aliasToName.put(name,aliasToName.get(name));
						}
				}
			return res;
		}

		/**
		 * Freezed and type string properties are not used in equality check. Two structures are
		 * equal if they have fields with same name and constraints. 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object other)
		{
				if(this==other)
					return true;
				
				//this will take care of other==null
				if(!(other instanceof GenericBeanStructure))
					return false;
				
			GenericBeanStructure otherStruct=(GenericBeanStructure)other;
			
				if(!nameToField.equals(otherStruct.nameToField))
					return false;
			return true;
		}

		/**
		 * Returns the hascode of the name-field map, thus ensuring when two structres are 
		 * equal according to equals() thier hashCodes() are also equal.
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode()
		{
				if(hashCode!=null)
					return hashCode.intValue();
			hashCode=new Integer(nameToField.hashCode());
			return hashCode.intValue();
		}
		
		/**
		 * Creates a generic bean structure with fields that maps to the setters/adders of
		 * the specified type.
		 * @param cls Type from which generic bean struture needs to be built.
		 * @return Generic bean structure that can support fields mapped from setters 
		 * 			of cls.
		 */
		public static GenericBeanStructure toGenericBeanStructure(Class cls)
		{
				if(cls==null)
					throw new NullPointerException("Class can not be null.");
				
			Method met[]=cls.getMethods();
			Class arg[]=null;
			GenericBeanStructure struct=new GenericBeanStructure(cls.getName());
			String name=null;
			boolean adder=false;
				try
				{
					for(int i=0;i<met.length;i++)
					{
						arg=met[i].getParameterTypes();
						
							if(arg==null || arg.length!=1)
								continue;
							
						name=met[i].getName();
						
							if(!name.startsWith("set") && !name.startsWith("add"))
								continue;
							
							if(name.length()==3)
								continue;
						
						adder=name.startsWith("add");
						name=name.substring(3,name.length());
						
							if(adder)
								struct.addAdderField(name,arg[0]);
							else
								struct.addField(name,arg[0]);
					}
				}catch(Exception ex)
				{
					throw new BeanLoadException("Error in fetching property types from the bean.",ex);
				}
			return struct;
		}
}
