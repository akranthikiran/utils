package com.fw.ccg.beans;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;

import com.fw.ccg.core.ValidateException;

/**
 * <BR>
 * <P>
 * Represents a list of similar generic beans, that is beans with same structure. This list
 * is backed by java.util.ArrayList and holds same behaviour like search efficiency 
 * and others.
 * <BR>
 * <B><I>This list does not support null values but allows duplicate beans.</I></B>
 * </P>
 * 
 * <P>
 *  Note all the beans in the list will share common structure. Thus during 
 *  serialization/deserialization only one structure instance will be written/read 
 *  respectively.
 *  </P>
 *  
 *  <P>
 *  If a bean is added with a different structure instance
 *  but equals to the structure instance this list holds, then the added bean structure will
 *  get replaced with the structure this list holds. This will ensure only one structure 
 *  instance is shared among all the beans in this list (helps in memory efficieny and 
 *  efficiency during serialization and deserialization).
 *  </P> 
 * <BR>
 * @author A. Kranthi Kiran
 */
public class GenericBeanList implements Serializable,Cloneable
{
	private static final long serialVersionUID=1L;
	
	private GenericBeanStructure struct;
	private ArrayList beans=new ArrayList();
		/**
		 * Creates  a generic bean list which can gold generic beans created using struct.
		 * @param struct Type of structure of generic beans that list should hold.
		 */
		public GenericBeanList(GenericBeanStructure struct)
		{
				if(struct==null)
					throw new NullPointerException("Structure cannot be null.");
			this.struct=struct;
		}
		
		/**
		 * @return Generic bean structure of the generic beans this list can hold.
		 */
		public GenericBeanStructure getStructure()
		{
			return struct;
		}
		
		/**
		 * Adds specified bean to the list. Null values are not allowed.
		 * <BR>
		 * Note: Specified bean structure should be same or equal with the structure of 
		 * this list. <B><I>If the specified bean structure is equal but not same, then the
		 * bean's structure will be replaced by this list structure, this will ensure all
		 * the beans in this list will share common structure instance.</I></B>
		 * <BR>
		 * @param bean Bean to be added.
		 */
		public void add(GenericBean bean)
		{
				if(bean==null)
					throw new NullPointerException("Generic bean cannot be null.");
			
				if(!struct.equals(bean.getStructure()))
					throw new IllegalArgumentException("Specified bean structure and this generic bean list structure doesnot match.");
			
			bean.setStructure(struct);
			beans.add(bean);
		}
		
		/**
		 * @param idx
		 * @return generic bean at specified index.
		 */
		public GenericBean getBean(int idx)
		{
				if(idx<0 || idx>=beans.size())
					throw new IndexOutOfBoundsException("Specified index is out of bounds: "+idx);
			return (GenericBean)beans.get(idx);
		}
		
		/**
		 * @return Iterator of generic beans this list holds.
		 */
		public Iterator getBeans()
		{
			return beans.iterator();
		}
		
		/**
		 * Removes generic bean at specified index.
		 * @param idx Index at which bean needs to be removed.
		 * @return Generic bean that is being rmoved.
		 */
		public GenericBean removeBean(int idx)
		{
				if(idx<0 || idx>=beans.size())
					throw new IndexOutOfBoundsException("Specified index is out of bounds: "+idx);
			return (GenericBean)beans.remove(idx);
		}
		
		/**
		 * Removes specified bean from the list.
		 * @param bean
		 * @return true if removal is succeful otherwise false.
		 */
		public boolean removeBean(GenericBean bean)
		{
			return beans.remove(bean);
		}
		
		/**
		 * Validates all the beans in this list. This method will call validate() on all 
		 * the beans this list holds.
		 * @throws ValidateException If validation fails on any of the bean.
		 */
		public void validate() throws ValidateException
		{
			Iterator it=beans.iterator();
			GenericBean bean=null;
				while(it.hasNext())
				{
					bean=(GenericBean)it.next();
					bean.validate();
				}
		}
		
		/**
		 * @return Size of this list.
		 */
		public int size()
		{
			return beans.size();
		}
		
		/**
		 * <P>
		 * Convers all generic beans in this list to the specified type and returns 
		 * resultant beans as an array. The returned array will be of type "type".
		 * </P>
		 * <P>
		 * For each generic bean, this bean will create on instance of type "tpye" using
		 * default constructor and calls loadProperties() on the corresponding generic bean.
		 * </P>
		 * @param type Type of the beans needs to be built from undelying generic beans.
		 * @param mapAllProp Maps all properties to specified type.
		 * @return Array of converted beans.
		 */
		public Object[] toBeanList(Class type,boolean mapAllProp)
		{
				if(type==null)
					throw new NullPointerException("Type can not be null.");
			Object res[]=(Object[])Array.newInstance(type,beans.size());
			int idx=0;
			Iterator it=beans.iterator();
			GenericBean bean=null;
				try
				{
					while(it.hasNext())
					{
						bean=(GenericBean)it.next();
						res[idx]=type.newInstance();
						bean.loadProperties(res[idx],mapAllProp);
						idx++;
					}
				}catch(Exception ex)
				{
					throw new BeanLoadException("Error in creating bean list of type: "+type.getName(),ex);
				}
			return res;
		}
		
		/**
		 * <P>
		 * Builds a generic bean list whose structrue is built using "type". 
		 * If type is null, then beans[0].getClass() is used to build generic bean 
		 * structure. All the elements of "beans" should be compatible with this 
		 * generic bean structure being built.
		 * </P> 
		 * 
		 * @param beans Beans which has to be converted into generic beans.
		 * @param adders Specifies whether adder fields should be added in the structure
		 * 					being created.
		 * @return A generic bean list built using specified beans and type.
		 */
		public static GenericBeanList buildList(Object beans[],Class type,boolean adders)
		{
				if(beans==null || beans.length<=0)
					return null;
			type=(type==null)?beans[0].getClass():type;
			GenericBeanStructure struct=GenericBeanStructure.toGenericBeanStructure(type);
			GenericBeanList lst=new GenericBeanList(struct);
			int len=beans.length;
			GenericBean genBean=null;
				for(int i=0;i<len;i++)
				{
					genBean=GenericBean.toGenericBean(beans[i],struct,adders,false);
					lst.add(genBean);
				}
			return lst;
		}

		/**
		 * Two lists are equal if undelying structure and all beans in this list are equal.
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object other)
		{	
				if(this==other)
					return true;
				
				if(!(other instanceof GenericBeanList))
					return false;
			GenericBeanList lst=(GenericBeanList)other;
			return (struct.equals(lst.struct) && beans.equals(lst.beans));
		}

		/**
		 * Hash code returned by this method is sum of hash codes of undelying structure and
		 * sum of hashcodes of beans in this list.  
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode()
		{
			return struct.hashCode()+beans.hashCode();
		}
		
		/**
		 * The cloned instance will share same structure and bean references.
		 * @see java.lang.Object#clone()
		 */
		public Object clone()
		{
			GenericBeanList res=new GenericBeanList(struct);
			res.beans=(ArrayList)beans.clone();
			return res;
		}
		
		/**
		 * A comma separated generic bean string conversions.
		 * @see java.lang.Object#toString()
		 */
		public String toString()
		{
			StringBuffer buff=new StringBuffer(super.toString());
			buff.append("[");
			Iterator it=beans.iterator();
				while(it.hasNext())
				{
					buff.append(it.next());
						if(it.hasNext())
							buff.append(",\n");
				}
			buff.append("]");
			return buff.toString();
		}
}
