package com.fw.ccg.ds;

public class BitsCollection
{
	public static final int BIT_CONST[]=new int[32];
	
		static
		{
			int st=1;
				for(int i=0;i<BIT_CONST.length;i++)
				{
					BIT_CONST[i]=st;
					st*=2;
				}
		}
	
	public int bits[];
	
		public BitsCollection()
		{
			this(31);
		}
	
		public BitsCollection(int len)
		{
				if(len<=0)
					throw new IllegalArgumentException("Invalid length specified: "+len);
				
			len=len/32;
			setCapacity(len);
		}
		
		public boolean get(int idx)
		{
			int mainIdx=idx/32;
			
				if(mainIdx<0 || mainIdx>=bits.length)
					return false;
				
			int bitIdx=idx%32;
			return ((bits[mainIdx]&BIT_CONST[bitIdx])==BIT_CONST[bitIdx]);
		}
		
		public void set(int idx,boolean b)
		{
				if(idx<0)
					throw new IndexOutOfBoundsException("Index is out of bounds: "+idx);
				
			int mainIdx=idx/32;
				if(mainIdx>=bits.length)
				{
					if(b)
						setCapacity(mainIdx);
					else
						return;
				}
			
			int bitIdx=idx%32;
			bits[mainIdx]=bits[mainIdx]|BIT_CONST[bitIdx];
		}
		
		private void setCapacity(int idx)
		{
				if(bits!=null && idx<bits.length)
					return;
				
			int newArr[]=new int[idx+1];
			
				if(bits!=null)
				{
					for(int i=0;i<bits.length;i++)
						newArr[i]=bits[i];
				}
				
			this.bits=newArr;
		}
}
