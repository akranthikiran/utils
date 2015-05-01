package com.fw.ccg.ds;

import java.util.Iterator;

import com.fw.ccg.util.ArrayIterator;

public class PrimitiveIterator implements Iterator
{
	private Iterator iterator;
	
		public PrimitiveIterator()
		{
			iterator=new ArrayIterator(new Object[0]);
		}
		
		public PrimitiveIterator(Iterator it)
		{
			iterator=it;
		}
		
		public PrimitiveIterator(Object array)
		{
				if(array==null)
					throw new NullPointerException("Array can not be null.");
				
			Class cls=array.getClass();
				if(!cls.isArray())
					throw new IllegalArgumentException("Non array object encountered.");
				
			Class compType=cls.getComponentType();
			
				if(byte.class.equals(compType))
				{
					byte arr[]=(byte[])array;
					Byte obj[]=new Byte[arr.length];
						for(int i=0;i<arr.length;i++)
							obj[i]=new Byte(arr[i]);
					iterator=new ArrayIterator(obj);
				}
				else if(boolean.class.equals(compType))
				{
					boolean arr[]=(boolean[])array;
					Boolean obj[]=new Boolean[arr.length];
						for(int i=0;i<arr.length;i++)
							obj[i]=new Boolean(arr[i]);
					iterator=new ArrayIterator(obj);
				}
				else if(char.class.equals(compType))
				{
					char arr[]=(char[])array;
					Character obj[]=new Character[arr.length];
						for(int i=0;i<arr.length;i++)
							obj[i]=new Character(arr[i]);
					iterator=new ArrayIterator(obj);
				}
				else if(short.class.equals(compType))
				{
					short arr[]=(short[])array;
					Short obj[]=new Short[arr.length];
						for(int i=0;i<arr.length;i++)
							obj[i]=new Short(arr[i]);
					iterator=new ArrayIterator(obj);
				}
				else if(int.class.equals(compType))
				{
					int arr[]=(int[])array;
					Integer obj[]=new Integer[arr.length];
						for(int i=0;i<arr.length;i++)
							obj[i]=new Integer(arr[i]);
					iterator=new ArrayIterator(obj);
				}
				else if(long.class.equals(compType))
				{
					long arr[]=(long[])array;
					Long obj[]=new Long[arr.length];
						for(int i=0;i<arr.length;i++)
							obj[i]=new Long(arr[i]);
					iterator=new ArrayIterator(obj);
				}
				else if(float.class.equals(compType))
				{
					float arr[]=(float[])array;
					Float obj[]=new Float[arr.length];
						for(int i=0;i<arr.length;i++)
							obj[i]=new Float(arr[i]);
					iterator=new ArrayIterator(obj);
				}
				else if(double.class.equals(compType))
				{
					double arr[]=(double[])array;
					Double obj[]=new Double[arr.length];
						for(int i=0;i<arr.length;i++)
							obj[i]=new Double(arr[i]);
					iterator=new ArrayIterator(obj);
				}
				else
					iterator=new ArrayIterator((Object[])array);
		}
		
		public boolean hasNext()
		{
			return iterator.hasNext();
		}
	
		public Object next()
		{
			return iterator.next();
		}
		
		public byte nextByte()
		{
			return ((Byte)iterator.next()).byteValue();
		}
	
		public boolean nextBoolean()
		{
			return ((Boolean)iterator.next()).booleanValue();
		}
		
		public char nextChar()
		{
			return ((Character)iterator.next()).charValue();
		}
		
		public short nextShort()
		{
			return ((Number)iterator.next()).shortValue();
		}
		
		public int nextInt()
		{
			return ((Number)iterator.next()).intValue();
		}
		
		public long nextLong()
		{
			return ((Number)iterator.next()).longValue();
		}
		
		public float nextFloat()
		{
			return ((Number)iterator.next()).floatValue();
		}
		
		public double nextDouble()
		{
			return ((Number)iterator.next()).doubleValue();
		}
		
		public void remove()
		{
			iterator.remove();
		}
}
