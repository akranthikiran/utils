package com.fw.ccg.beans;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;

import com.fw.ccg.core.SimpleAttributedBean;
import com.fw.ccg.core.ValidateException;
import com.fw.ccg.ds.PrimitiveIterator;
import com.fw.ccg.util.CCGUtility;

/**
 * <BR><BR>
 * <P>
 * GenericBean is a generic version of beans with genric setters, adders and getters. GenericBean's as stand alone 
 * does not provide any special functionality but in combination with GenericBeanStructure, validations can
 * be enforced on generic bean instances.
 * </P>
 * 
 * <P>
 * The relationship between generic bean sructure and generic bean is like relationship between a class
 * and its instance. Structure defines the fields generic beans can hold, thier data types and some 
 * validations on the data that can be set to these fields. Just like a class can have multiple instances,
 * a generic bean structure can be associated with multiple generic beans.
 * </P>
 * <BR>
 * <U><B>Freezing the Bean</U></B>
 * <BR><BR>
 * <P>
 * A GenericBean can be freezed by using freeze() method after which the non-updateable field
 * values cannot be modified. And also the beans which are marked read-only throw constructor, for those
 * the fields become read-only. That is, after freezeing, for read-only generic beans, field values
 * can not be updated. This is an optional measure, for application where data is expected to be 
 * read only will not get altered by mistake.
 * </P>
 * <P> 
 * Note, this freeze flag will not be transfered to the cloned 
 * objects, that is the cloned bean fields are still updateable. If the cloned objects needs to 
 * be freezed then freeze() should be called explicitly on cloned objects.
 * </P>
 * <P>
 * Even though cloned objects will not hold freezed status of source bean, serialization/deserialization 
 * process will not alter this freezed status. That is a bean after serialization and 
 * deserialization also will be in freezed status if it is before serilization and vice versa. In this way
 * generic beans which may be passed across layers will be ensured that crtitical information is not altered
 * by mistake.
 * </P>
 * <BR>
 * @author A. Kranthi Kiran
 */
public class GenericBean extends SimpleAttributedBean implements Serializable,Cloneable
{
		/**
		 * <P>
		 * A simple wrapper over the Collection object and is used to hold adder field's data
		 * of the generic bean.
		 * </P>
		 * @author A. Kranthi Kiran
		 */
		static class AdderFieldData implements Serializable,Cloneable
		{
			private static final long serialVersionUID=1L;
			
			private Collection data;
				
				/**
				 * <P>
				 * Depedning on the unique and ordered flag the collection that needs to be maintained for this 
				 * field will be determined. Following table describes how the collection object to be used
				 * is determined:
				 * </P>
				 * 	<TABLE BORDER="1">
				 * 		<TR>
				 * 			<TD>Ordered and<BR> Unique</TD>
				 * 			<TD>LinkedHashSet</TD>
				 * 		</TR>
				 * 		<TR>
				 * 			<TD>Unique</TD>
				 * 			<TD>HashSet</TD>
				 * 		</TR>
				 * 		<TR>
				 * 			<TD>Otherwise</TD>
				 * 			<TD>ArrayList</TD>
				 * 		</TR>
				 * 	</TABLE>
				 * <BR>
				 * @param unique Unique flag
				 * @param ordered Ordered flag
				 */
				public AdderFieldData(boolean unique,boolean ordered)
				{
						if(unique && ordered)
							data=new LinkedHashSet();
						else if(unique)
							data=new HashSet();
						else
							data=new ArrayList();
				}
				
				/**
				 * Used by clone()
				 */
				private AdderFieldData() 
				{}
				
				/**
				 * Adds the value to undelying collection.
				 * @param value Value to be added
				 * @return True if addition is successful
				 */
				public boolean addValue(Object value)
				{
					return data.add(value);
				}
				
				/**
				 * Remove specified value from underlying collection
				 * @param value Value to be removed.
				 * @return True if successful.
				 */
				public boolean removeValue(Object value)
				{
					return data.remove(value);
				}
				
				/**
				 * @return Values in undelying collection
				 */
				public PrimitiveIterator values()
				{
					return new PrimitiveIterator(data.iterator());
				}
				
				/**
				 * Equal to the hashCode() of undelying collection.
				 * @see java.lang.Object#hashCode()
				 */
				public int hashCode()
				{
					return data.hashCode();
				}
				
				/* (non-Javadoc)
				 * @see java.lang.Object#clone()
				 */
				public Object clone()
				{
					AdderFieldData res=new AdderFieldData();
						if(data instanceof LinkedHashSet)
							res.data=new LinkedHashSet(data);
						if(data instanceof HashSet)
							res.data=new HashSet(data);
						else
							res.data=new ArrayList(data);
					
					return res;
				}
				
				/* (non-Javadoc)
				 * @see java.lang.Object#equals(java.lang.Object)
				 */
				public boolean equals(Object other)
				{
						if(!(other instanceof AdderFieldData))
							return false;
					return data.equals(((AdderFieldData)other).data);
				}
				
				/**
				 * Appends The comma separated values of the undelying collection to the specified 
				 * buffer.
				 * @param buff
				 */
				public void toString(StringBuffer buff)
				{
					buff.append("{");
					Iterator it=data.iterator();
						while(it.hasNext())
						{
							buff.append(it.next());
								if(it.hasNext())
									buff.append(",");
						}
					buff.append("}");
				}
			
		}
		
	private static final long serialVersionUID=1L;

	/**
	 * Structure with which this bean is associated with.
	 */
	private GenericBeanStructure structure;
	
	/**
	 * Field name (wrapped using com.ccg.beans.FieldName) to Value mapping.  
	 */
	private HashMap nameToValue=new HashMap();
	
	/**
	 * Freeze flag
	 */
	private boolean freezed=false;
	
	/**
	 * HashCode of this bean. A temporary buffer which will hold caluclated hashcode for future use. This will
	 * be calcualted everytime the bean field values changes.
	 */
	private Integer hashCode=null;
	
	/**
	 * Read-only flag.
	 */
	private boolean readOnly=false;
	
		/**
		 * Creates a unstructured generic bean.
		 */
		public GenericBean()
		{}
		
		/**
		 * Creates a unstructured bean with specified readOnly flag.
		 * @param readOnly
		 */
		public GenericBean(boolean readOnly)
		{
			this.readOnly=readOnly;
		}
		
		/**
		 * Creates a bean with specified structure. If struct is not specified, then a unstructured
		 * generic bean will be created.
		 * @param struct
		 */
		public GenericBean(GenericBeanStructure struct)
		{
			structure=struct;
				if(struct!=null)
					struct.freeze(this);
		}
		
		/**
		 * Creates a bean with specified structure. If struct is not specified, then a unstructured
		 * generic bean will be created. And marks the bean with specified readOnly flag.
		 * @param struct
		 * @param readOnly
		 */
		public GenericBean(GenericBeanStructure struct,boolean readOnly)
		{
			structure=struct;
				if(struct!=null)
					struct.freeze(this);
			
			this.readOnly=readOnly;
		}
		
		/**
		 * <P>
		 * Freezes this bean instance. After this method call, fields which are marked as non-updateable 
		 * cannot be updated. Before this method call all the fields (including non-updateable marked) can 
		 * be updated (this is need for initial assignment of non-updateable/other fields).
		 * </P>
		 * <P>
		 * If the current bean is read-only, then after this method call no fields can be updated.
		 * </P>
		 */
		public void freeze()
		{
			freezed=true;
		}
		
		/**
		 * @return Whether this bean is freezed.
		 */
		public boolean isFreezed()
		{
			return freezed;
		}
		
		/**
		 * Assigns the current bean structure to the specified structure. Before changing 
		 * this bean structure to specified struture, a check is performed if this bean
		 * is compatible with specified structure. If not, IllegalArgumentException will be thrown. 
		 * Note, mandatory field check will not be performed by this method.
		 * <BR>
		 * <B>Note:</B>If successful, specified structure will get freezed and previous
		 * structure (if any) also remains in freezed status.
		 * @param structure New structure for this bean.
		 */
		public final void setStructure(GenericBeanStructure structure)
		{
				if(structure==null)
				{
					this.structure=null;
					return;
				}
				
				if(structure==this.structure)
					return;
				
				try
				{
					structure.validate(this,false,true);
				}catch(Exception ex)
				{
					throw new IllegalArgumentException("The current structure of this bean doesn't match with specified structure.",ex);
				}
			this.structure=structure;
			structure.freeze(this);
			hashCode=null;
		}
		
		/**
		 * @return Structure used by the current bean.
		 */
		public final GenericBeanStructure getStructure()
		{
			return structure;
		}
		
		/**
		 * Iterator returned by this method doesnt support remove() operation.
		 * @return Iterator of field names holded by this bean. Note, associated structure may hold 
		 * additional fields. 
		 */
		public Iterator getFieldNames()
		{
			final Iterator it=nameToValue.keySet().iterator();
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
						throw new UnsupportedOperationException("Remove() methos is not supported by generic bean iterator.");
						
					}
					
				};
			return res;
		}
		
		/**
		 * Used by GenericBeanStructure. This will return iterator of FieldName instances.
		 * @return
		 */
		Iterator getFieldList()
		{
			return nameToValue.keySet().iterator();
		}
		
		/**
		 * Retuens if the associated sructure/this bean has specified field.
		 * @param name Name of the field
		 * @return True, if the bean/structure has this field.
		 */
		public boolean isField(String name)
		{
				if(structure!=null)
					return (structure.hasField(name)!=null);
			
			return nameToValue.containsKey(new FieldName(name));
			
		}
		
		/**
		 * @param name Name of the adder field.
		 * @return Iterator of the values holded by the specified field of this bean.
		 */
		public PrimitiveIterator getAdderField(String name)
		{
			Object res=nameToValue.get(new FieldName(name));
			
				if(res==null)
					return new PrimitiveIterator();
				
				if(!(res instanceof AdderFieldData))
					throw new UnsupportedOperationException("This method can not be used by normal fields: "+name);
				
			return ((AdderFieldData)res).values(); 
		}
		
		public void setAttribute(Object id,Object value)
		{
				if(freezed && readOnly)
					throw new FieldException("Cannot perform attribute setting operation on read only bean.");
				
			super.setAttribute(id,value);
		}
		
		/**
		 * Removes specified value from the specified adder field. 
		 * <BR>
		 * <P>
		 * <B><I>Note: This operation can not be done on freezed bean, if the field is not updateable or if the bean 
		 * itself is read-only.</I></B>
		 * </P>
		 * @param field Adder field name.
		 * @param value Value to be removed.
		 * @return If remove operation was successful.
		 */
		public boolean remove(String field,Object value)
		{
				if(freezed && readOnly)
					throw new FieldException("Cannot perform remove operation on read only bean.");
			
			Object res=nameToValue.get(new FieldName(field));

				if(res==null)
					return false;
				
				if(!(res instanceof AdderFieldData))
					throw new UnsupportedOperationException("This method can be used only for adder fields: "+field);
				
			return ((AdderFieldData)res).removeValue(value); 
		}
		
		/**
		 * equivalent to remove(field,new Byte(value))
		 * @param field
		 * @param value
		 * @return
		 */
		public boolean remove(String field,byte value)
		{
			return remove(field,new Byte(value));
		}
		
		/**
		 * equivalent to remove(field,new Boolean(value))
		 * @param field
		 * @param value
		 * @return
		 */
		public boolean remove(String field,boolean value)
		{
			return remove(field,new Boolean(value));
		}
		
		/**
		 * equivalent to remove(field,new Character(value))
		 * @param field
		 * @param value
		 * @return
		 */
		public boolean remove(String field,char value)
		{
			return remove(field,new Character(value));
		}
		
		/**
		 * equivalent to remove(field,new Short(value))
		 * @param field
		 * @param value
		 * @return
		 */
		public boolean remove(String field,short value)
		{
			return remove(field,new Short(value));
		}
		
		/**
		 * equivalent to remove(field,new Integer(value))
		 * @param field
		 * @param value
		 * @return
		 */
		public boolean remove(String field,int value)
		{
			return remove(field,new Integer(value));
		}
		
		/**
		 * equivalent to remove(field,new Long(value))
		 * @param field
		 * @param value
		 * @return
		 */
		public boolean remove(String field,long value)
		{
			return remove(field,new Long(value));
		}
		
		/**
		 * equivalent to remove(field,new Float(value))
		 * @param field
		 * @param value
		 * @return
		 */
		public boolean remove(String field,float value)
		{
			return remove(field,new Float(value));
		}
		
		/**
		 * equivalent to remove(field,new Double(value))
		 * @param field
		 * @param value
		 * @return
		 */
		public boolean remove(String field,double value)
		{
			return remove(field,new Double(value));
		}
		
		/**
		 * @param field Name of the adder field.
		 * @return Whether specified field is adder field.
		 */
		public boolean isAdderField(String field)
		{
				if(structure!=null)
					return structure.isAdderField(field);
			
			Object res=nameToValue.get(new FieldName(field));
			return (res instanceof AdderFieldData);
		}
		
		/**
		 * Adds the specified value to the specified adder field.
		 * <P>
		 * <B><I>Note: This operation can not be done on freezed bean, if the field is not updateable or if the bean 
		 * itself is read-only.</I></B>
		 * </P>
		 * @param fieldName Adder field name
		 * @param value Value to be added.
		 * @return If addition operation is successful.
		 */
		public boolean add(String fieldName,Object value)
		{
				if(fieldName==null)
					throw new NullPointerException("Name of the field cannot be null.");
			
				if(freezed && readOnly)
					throw new FieldException("Cannot perform add operation on read only bean.");
			
			FieldName field=null;
				if(structure!=null)
				{
					field=structure.hasField(fieldName);
						if(field==null)
							throw new FieldException("Invalid field name specified: "+fieldName);
					
						if(freezed && !structure.isUpdateable(field.getName()))
							throw new FieldException("Specified field is not updateable: "+fieldName);
						
					fieldName=field.getName();
					structure.validateValue(this,fieldName,value);
				}
				else
					field=new FieldName(fieldName);
			
			hashCode=null;
			Object res=nameToValue.get(field);
			
				if(res==null)
				{
					boolean unique=false;
					boolean ordered=true;
						if(structure!=null)
						{
							unique=structure.isUniqueValued(fieldName);
							ordered=structure.isOrdered(fieldName);
						}
				
					res=new AdderFieldData(unique,ordered);
					nameToValue.put(field,res);
				}
				
				if(!(res instanceof AdderFieldData))
					throw new UnsupportedOperationException("This method can be used only for adder fields: "+fieldName);
			
			return ((AdderFieldData)res).addValue(value); 
		}
		
		/**
		 * Equivalent to calling add(field,new Byte(value))
		 * @param field
		 * @param value
		 * @return
		 */
		public boolean add(String field,byte value)
		{
			return add(field,new Byte(value));
		}
		
		/**
		 * Equivalent to calling add(field,new Boolean(value))
		 * @param field
		 * @param value
		 * @return
		 */
		public boolean add(String field,boolean value)
		{
			return add(field,new Boolean(value));
		}
		
		/**
		 * Equivalent to calling add(field,new Character(value))
		 * @param field
		 * @param value
		 * @return
		 */
		public boolean add(String field,char value)
		{
			return add(field,new Character(value));
		}
		
		/**
		 * Equivalent to calling add(field,new Short(value))
		 * @param field
		 * @param value
		 * @return
		 */
		public boolean add(String field,short value)
		{
			return add(field,new Short(value));
		}
		
		/**
		 * Equivalent to calling add(field,new Integer(value))
		 * @param field
		 * @param value
		 * @return
		 */
		public boolean add(String field,int value)
		{
			return add(field,new Integer(value));
		}
		
		/**
		 * Equivalent to calling add(field,new Long(value))
		 * @param field
		 * @param value
		 * @return
		 */
		public boolean add(String field,long value)
		{
			return add(field,new Long(value));
		}
		
		/**
		 * Equivalent to calling add(field,new Float(value))
		 * @param field
		 * @param value
		 * @return
		 */
		public boolean add(String field,float value)
		{
			return add(field,new Float(value));
		}
		
		/**
		 * Equivalent to calling add(field,new Double(value))
		 * @param field
		 * @param value
		 * @return
		 */
		public boolean add(String field,double value)
		{
			return add(field,new Double(value));
		}
		
		/**
		 * Removes the specified field for this bean.
		 * <P>
		 * <B><I>Note: This operation can not be done on freezed bean, if the field is not updateable or if the bean 
		 * itself is read-only.</I></B>
		 * </P>
		 * @param name Name of the field to be removed.
		 * @return The value holded by specified field. For adder fields the returned value will be PrimitiveIterator.
		 */
		public Object unsetField(String name)
		{
				if(freezed && readOnly)
					throw new FieldException("Cannot perform unset operation on read only bean.");
			
			Object res=nameToValue.remove(new FieldName(name));
			
				if(res instanceof AdderFieldData)
					res=((AdderFieldData)res).values();
			
			return res;
		}
		
		/**
		 * This method should be used only for normal fields (not for adder fields).
		 * @param name
		 * @return Value of the field having specified name.
		 */
		public Object getField(String name)
		{
			Object res=nameToValue.get(new FieldName(name));
			
				if(res instanceof AdderFieldData)
					throw new UnsupportedOperationException("This method can not be used for adder fields: "+name);
				
			return res; 
		}
		
		/**
		 * This method should be used only for normal fields (not for adder fields).
		 * @param name
		 * @return Value of the field having specified name.
		 */
		public char getCharField(String name)
		{
			Character ch=(Character)getField(name);
				if(ch==null)
					return 0;
			return ch.charValue();
		}
		
		/**
		 * This method should be used only for normal fields (not for adder fields).
		 * @param name
		 * @return Value of the field having specified name.
		 */
		public boolean getBooleanField(String name)
		{
			Boolean val=(Boolean)getField(name);
				if(val==null)
					return false;
			return val.booleanValue();
		}
		
		/**
		 * This method should be used only for normal fields (not for adder fields).
		 * @param name
		 * @return Value of the field having specified name.
		 */
		public byte getByteField(String name)
		{
			Number val=(Number)getField(name);
				if(val==null)
					return 0;
			return val.byteValue();
		}
		
		/**
		 * This method should be used only for normal fields (not for adder fields).
		 * @param name
		 * @return Value of the field having specified name.
		 */
		public int getIntField(String name)
		{
			Number val=(Number)getField(name);
				if(val==null)
					return 0;
			return val.intValue();
		}
		
		/**
		 * This method should be used only for normal fields (not for adder fields).
		 * @param name
		 * @return Value of the field having specified name.
		 */
		public long getLongField(String name)
		{
			Number val=(Number)getField(name);
				if(val==null)
					return 0;
			return val.longValue();
		}
		
		/**
		 * This method should be used only for normal fields (not for adder fields).
		 * @param name
		 * @return Value of the field having specified name.
		 */
		public short getShortField(String name)
		{
			Number val=(Number)getField(name);
				if(val==null)
					return 0;
			return val.shortValue();
		}
		
		/**
		 * This method should be used only for normal fields (not for adder fields).
		 * @param name
		 * @return Value of the field having specified name.
		 */
		public float getFloatField(String name)
		{
			Number val=(Number)getField(name);
				if(val==null)
					return 0.0f;
			return val.floatValue();
		}
		
		/**
		 * This method should be used only for normal fields (not for adder fields).
		 * @param name
		 * @return Value of the field having specified name.
		 */
		public double getDoubleField(String name)
		{
			Number val=(Number)getField(name);
				if(val==null)
					return 0.0f;
			return val.doubleValue();
		}
		
		/**
		 * Sets the field having specified name with specified value. Before setting the value
		 * a structure (if present) field validation check is performed.   
		 * <P>
		 * <B><I>Note: This operation can not be done on freezed bean, if the field is not updateable or if the bean 
		 * itself is read-only. And this method should be used only with normal fields (not with adder fields)</I></B>
		 * </P>
		 * @param name Name of the field.
		 * @param value Value for the field.
		 * @return Previous value holded by specified field (if any).
		 */
		public final Object setField(String name,Object value)
		{
				if(name==null)
					throw new NullPointerException("Name of the field cannot be null.");
				
				if(freezed && readOnly)
					throw new FieldException("Cannot perform set operation on read only bean.");
			
			FieldName field=null;
				if(structure!=null)
				{
					field=structure.hasField(name);
						if(field==null)
							throw new FieldException("Invalid field name specified: "+name);
					
						if(freezed && !structure.isUpdateable(name))
							throw new FieldException("Specified field is not updateable: "+name);
						
					name=field.getName();
					structure.validateValue(this,name,value);
				}
				else
					field=new FieldName(name);
				
			hashCode=null;
			
			Object prevValue=nameToValue.put(field,value);
			return prevValue;
		}
		
		/**
		 * Sets the field having specified name with specified value. Before setting the value
		 * a structure (if present) field validation check is performed.   
		 * <P>
		 * <B><I>Note: This operation can not be done on freezed bean, if the field is not updateable or if the bean 
		 * itself is read-only. And this method should be used only with normal fields (not with adder fields)</I></B>
		 * </P>
		 * @param name Name of the field.
		 * @param value Value for the field.
		 * @return Previous value holded by specified field (if any).
		 */
		public boolean setField(String name,boolean value)
		{
			Boolean prevVal=(Boolean)setField(name,new Boolean(value));
				if(prevVal==null)
					return false;
			return prevVal.booleanValue();
		}

		/**
		 * Sets the field having specified name with specified value. Before setting the value
		 * a structure (if present) field validation check is performed.   
		 * <P>
		 * <B><I>Note: This operation can not be done on freezed bean, if the field is not updateable or if the bean 
		 * itself is read-only. And this method should be used only with normal fields (not with adder fields)</I></B>
		 * </P>
		 * @param name Name of the field.
		 * @param value Value for the field.
		 * @return Previous value holded by specified field (if any).
		 */
		public char setField(String name,char value)
		{
			Character prevVal=(Character)setField(name,new Character(value));
				if(prevVal==null)
					return 0;
			return prevVal.charValue();
		}
		
		/**
		 * Sets the field having specified name with specified value. Before setting the value
		 * a structure (if present) field validation check is performed.   
		 * <P>
		 * <B><I>Note: This operation can not be done on freezed bean, if the field is not updateable or if the bean 
		 * itself is read-only. And this method should be used only with normal fields (not with adder fields)</I></B>
		 * </P>
		 * @param name Name of the field.
		 * @param value Value for the field.
		 * @return Previous value holded by specified field (if any).
		 */
		public short setField(String name,short value)
		{
			Short prevVal=(Short)setField(name,new Short(value));
				if(prevVal==null)
					return 0;
			return prevVal.shortValue();
		}
		
		/**
		 * Sets the field having specified name with specified value. Before setting the value
		 * a structure (if present) field validation check is performed.   
		 * <P>
		 * <B><I>Note: This operation can not be done on freezed bean, if the field is not updateable or if the bean 
		 * itself is read-only. And this method should be used only with normal fields (not with adder fields)</I></B>
		 * </P>
		 * @param name Name of the field.
		 * @param value Value for the field.
		 * @return Previous value holded by specified field (if any).
		 */
		public byte setField(String name,byte value)
		{
			Byte prevVal=(Byte)setField(name,new Byte(value));
				if(prevVal==null)
					return 0;
			return prevVal.byteValue();
		}
		
		/**
		 * Sets the field having specified name with specified value. Before setting the value
		 * a structure (if present) field validation check is performed.   
		 * <P>
		 * <B><I>Note: This operation can not be done on freezed bean, if the field is not updateable or if the bean 
		 * itself is read-only. And this method should be used only with normal fields (not with adder fields)</I></B>
		 * </P>
		 * @param name Name of the field.
		 * @param value Value for the field.
		 * @return Previous value holded by specified field (if any).
		 */
		public int setField(String name,int value)
		{
			Integer prevVal=(Integer)setField(name,new Integer(value));
				if(prevVal==null)
					return 0;
			return prevVal.intValue();
		}
		
		/**
		 * Sets the field having specified name with specified value. Before setting the value
		 * a structure (if present) field validation check is performed.   
		 * <P>
		 * <B><I>Note: This operation can not be done on freezed bean, if the field is not updateable or if the bean 
		 * itself is read-only. And this method should be used only with normal fields (not with adder fields)</I></B>
		 * </P>
		 * @param name Name of the field.
		 * @param value Value for the field.
		 * @return Previous value holded by specified field (if any).
		 */
		public long setField(String name,long value)
		{
			Long prevVal=(Long)setField(name,new Long(value));
				if(prevVal==null)
					return 0;
			return prevVal.longValue();
		}
		
		/**
		 * Sets the field having specified name with specified value. Before setting the value
		 * a structure (if present) field validation check is performed.   
		 * <P>
		 * <B><I>Note: This operation can not be done on freezed bean, if the field is not updateable or if the bean 
		 * itself is read-only. And this method should be used only with normal fields (not with adder fields)</I></B>
		 * </P>
		 * @param name Name of the field.
		 * @param value Value for the field.
		 * @return Previous value holded by specified field (if any).
		 */
		public float setField(String name,float value)
		{
			Float prevVal=(Float)setField(name,new Float(value));
				if(prevVal==null)
					return 0.0f;
			return prevVal.floatValue();
		}
		
		/**
		 * Sets the field having specified name with specified value. Before setting the value
		 * a structure (if present) field validation check is performed.   
		 * <P>
		 * <B><I>Note: This operation can not be done on freezed bean, if the field is not updateable or if the bean 
		 * itself is read-only. And this method should be used only with normal fields (not with adder fields)</I></B>
		 * </P>
		 * @param name Name of the field.
		 * @param value Value for the field.
		 * @return Previous value holded by specified field (if any).
		 */
		public double setField(String name,double value)
		{
			Double prevVal=(Double)setField(name,new Double(value));
				if(prevVal==null)
					return 0.0;
			return prevVal.doubleValue();
		}
		
		/**
		 * This method will inturn calls validate method on underlying structure (if any).
		 * @throws ValidateException
		 */
		public void validate() throws ValidateException
		{
				if(structure!=null)
					structure.validate(this,true,false);
		}

		/**
		 * Displays all field name value pairs, one per line. 
		 * @see java.lang.Object#toString()
		 */
		public String toString()
		{
			StringBuffer sb=new StringBuffer(super.toString());
				if(readOnly)
					sb.append("*\n(\n");
				else
					sb.append("\n(\n");
			Iterator keyIt=nameToValue.keySet().iterator();
			Object key=null;
			Object value=null;
				while(keyIt.hasNext())
				{
					key=keyIt.next();
					value=nameToValue.get(key);
					sb.append(key).append("=");
						if(value instanceof AdderFieldData)
						{
							((AdderFieldData)value).toString(sb);
							sb.append("\n");
						}
						else
							sb.append(value).append("\n");
				}
			sb.append(")");
			return sb.toString();
		}
		
		/**
		 * The resulting cloned object and this object will share the same structure instance. And
		 * field value pairs will be copied as a hollow shell. That is value objects 
		 * in both the cloned and current bean will refer to the same object. Note, during cloning freezed
		 * status will not be cloned.
		 * @see java.lang.Object#clone()
		 */
		public Object clone()
		{
			GenericBean res=new GenericBean();
			res.structure=structure;
			res.nameToValue=new HashMap();
			res.readOnly=readOnly;
			
			Iterator names=nameToValue.keySet().iterator();
			Object name=null;
			Object value=null;
			
				while(names.hasNext())
				{
					name=names.next();
					value=nameToValue.get(name);
					
						if(value instanceof AdderFieldData)
							value=((AdderFieldData)value).clone();
					
					res.nameToValue.put(name,value);
				}
			
			res.hashCode=hashCode;
			makeCopy(res);
			return res;
		}

		/**
		 * Checks if the specified bean has same name-value pairs and equal structure. The 
		 * structure of two beans need not be same (same reference) but they should be equal.
		 * @see java.lang.Object#equals(java.lang.Object)
		 * @return True if this bean is equal to specified bean.
		 */
		public boolean equals(Object other)
		{
				if(this==other)
					return true;
				
				//this also take cares of other==null
				if(!(other instanceof GenericBean))
					return false;
			GenericBean otherBean=(GenericBean)other;
				
				if((structure!=otherBean.structure) && 
						(structure!=null && !structure.equals(otherBean.structure)))
					return false;
				
				if(!nameToValue.equals(otherBean.nameToValue))
					return false;
				
			return true;
		}

		/**
		 * The hashcode for GenericBean is defined as the sum of the hascodes of name-value map
		 * and structure hashcode. This ensures hashCode() is equal for two beans which
		 * are equal as per equals() method. Hence satisfying hashCode() contract of Object 
		 * class.
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode()
		{
				if(hashCode!=null)
					return hashCode.intValue();
			int mapCode=nameToValue.hashCode();
			int structCode=0;
				if(structure!=null)
					structCode=structure.hashCode();
			hashCode=new Integer(mapCode+structCode);
			return hashCode.intValue();
		}
		
		
		/**
		 * <P>
		 * Loads the field values to the specified bean properties using setter methods.
		 * If mapAllProp is true and if a field in this generic bean exist
		 * in a such a way, there is no mapping setter in the specified bean, then
		 * IllegalStateException will be thrown.
		 * </P>
		 * 
		 * <P>
		 * For adder properties, the selceted method will get invoked for each value in the field.
		 * </P>
		 * 
		 * 
		 * Note: If multiple methods matches a property, all the matching methods will get invoked.
		 * <BR>   
		 * @param bean Bean to whose properties needs to be loaded. 
		 * @param mapAllProp A flag indicating that a check should be performed that all 
		 * 				the generic bean field values are getting mapped to specified bean
		 * 				properties.
		 */
		public void loadProperties(Object bean,boolean mapAllProp)
		{
			String name=null;
			Object value=null;
			Class valueType=null;
			Class cls=bean.getClass();

			Method met[]=cls.getMethods();
			Class arg[]=null;
			ArrayList unmatched=mapAllProp?new ArrayList(nameToValue.keySet()):null;
			FieldName fldName=null;
			Iterator adderValues=null;
				for(int i=0;i<met.length;i++)
				{
						if(!Modifier.isPublic(met[i].getModifiers()))
							continue;
						
					name=met[i].getName();
					
						if(name.length()<=3)
							continue;
						
						if(!name.startsWith("set") && !name.startsWith("add"))
							continue;
						
					arg=met[i].getParameterTypes();
						if(arg==null || arg.length!=1)
							continue;
						
					name=met[i].getName().substring(3);
					fldName=new FieldName(name);
						if(!nameToValue.containsKey(fldName))
							continue;
						
					value=nameToValue.get(fldName);
					
						if(!(value instanceof AdderFieldData))
						{
							valueType=value.getClass();
							
								try
								{
										if(CCGUtility.isAssignable(valueType,arg[0]))
										{
											met[i].invoke(bean,new Object[]{value});
												if(mapAllProp)
													unmatched.remove(fldName);
											continue;
										}
								}catch(Exception ex)
								{
									throw new IllegalStateException("Error in loading property: "+name,ex);
								}
						}
						else
						{
							adderValues=((AdderFieldData)value).values();
								while(adderValues.hasNext())
								{
									value=adderValues.next();
									valueType=value.getClass();
									
										try
										{
												if(CCGUtility.isAssignable(valueType,arg[0]))
												{
													met[i].invoke(bean,new Object[]{value});
													continue;
												}
										}catch(Exception ex)
										{
											throw new BeanLoadException("Error in loading property: "+name,ex);
										}
										
										if(mapAllProp)
											throw new BeanLoadException("Data type mismatch occured for adder \""+name+"\" for value: "+value);
								}
								
								if(mapAllProp)
									unmatched.remove(fldName);
						}
				}
				
				if(mapAllProp && unmatched.size()>0)
					throw new BeanLoadException("No mapping method found for property: "+unmatched.get(0));
		}
		
		/**
		 * Fetches all the properties from specified bean using getters. Fetched
		 * values will be used to populate Generic Bean and returns the same.
		 * <BR>
		 * If the specified bean is of GenericBeanWrapper type, then getGenericBean() of GenericBeanWrapper 
		 * is used to get GenericBean and the same bean is returned back.
		 * 
		 * @param bean Bean from which generic bean needs to be created.
		 * @param struct If specified, struct will be used for generic bean being created.
		 * @param adders If this flag is true, then an adder field will be created for properties
		 * 				whose type is array (primitive or Object) or Collection or Iterator. 
		 * @param readOnly Marks the created generic bean with the specified read-only flag.
		 * @return Generic bean with specified strcture (if not null) and with all the field
		 * 		values fetched using getters.
		 */
		public static GenericBean toGenericBean(Object bean,GenericBeanStructure struct,boolean adders,boolean readOnly)
		{
			Class cls=bean.getClass();
			Method met[]=cls.getMethods();
			Class retType=null;
			Class arg[]=null;
			GenericBean genBean=new GenericBean(struct,readOnly);
			String name=null;
			FieldName fld=null;
			Object value=null;
			Iterator values=null;
				try
				{
					for(int i=0;i<met.length;i++)
					{
							if(!Modifier.isPublic(met[i].getModifiers()))
								continue;
							
							if(Object.class.equals(met[i].getDeclaringClass()))
								continue;
							
						arg=met[i].getParameterTypes();
						retType=met[i].getReturnType();
							if(retType==null || void.class.equals(retType))
								continue;
							
							if(arg!=null && arg.length>0)
								continue;
						name=met[i].getName();
							if(!name.startsWith("get") && !name.startsWith("is"))
								continue;
							
							if(name.startsWith("get"))
							{
									if(name.length()==3)
										continue;
								name=name.substring(3,name.length());
							}
							
							if(name.startsWith("is"))
							{
									if(name.length()==2)
										continue;
								name=name.substring(2,name.length());
							}
						
							if(struct!=null)
							{
								fld=struct.hasField(name);
									if(fld==null)
										continue;
								name=fld.getName();
							}
						
						value=met[i].invoke(bean,(Object[])null);
							if(adders)
							{
								values=null;
									if(value instanceof Collection)
										values=((Collection)value).iterator();
									
									if(value instanceof Iterator)
										values=(Iterator)value;
										
									if(value.getClass().isArray())
										values=new PrimitiveIterator(value);
									
									if(values!=null)
									{
										Object obj=null;
											while(values.hasNext())
											{
												obj=values.next();
												genBean.add(name,obj);
											}
										continue;
									}
							}
							
						genBean.setField(name,value);
					}
				}catch(Exception ex)
				{
					throw new IllegalStateException("Error in fetching property values from the bean.",ex);
				}
			return genBean;
		}
}


