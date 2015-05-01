package com.fw.ccg.query;

/**
 * © Copyright 2006 IBM Corporation
 * <BR><BR>
 * This calss is generic representation of condition. Condition is divided into three parts
 * leftOperand (which can be only string), operator and the right operand (which can be any
 * object or primitive).The default operator used is "=".  
 * <BR>
 * @author A. Kranthi Kiran
 */
public class Condition
{
	private String leftOperand;
	private Object rightOperand;
	private String operator;
		/**
		 * Creates a condition object with specified operands and operator.
		 * @param left
		 * @param operator
		 * @param right
		 */
		public Condition(String left,String operator,Object right)
		{
			setLeftOperand(left);
			setOperator(operator);
			setRightOperand(right);
		}
	
		/**
		 * Creates a condition object with specified operands and operator.
		 * @param left
		 * @param operator
		 * @param right
		 */
		public Condition(String left,String operator,boolean right)
		{
			setLeftOperand(left);
			setOperator(operator);
			setRightOperand(right);
		}
		
		/**
		 * Creates a condition object with specified operands and operator.
		 * @param left
		 * @param operator
		 * @param right
		 */
		public Condition(String left,String operator,byte right)
		{
			setLeftOperand(left);
			setOperator(operator);
			setRightOperand(right);
		}
		
		/**
		 * Creates a condition object with specified operands and operator.
		 * @param left
		 * @param operator
		 * @param right
		 */
		public Condition(String left,String operator,char right)
		{
			setLeftOperand(left);
			setOperator(operator);
			setRightOperand(right);
		}
		
		/**
		 * Creates a condition object with specified operands and operator.
		 * @param left
		 * @param operator
		 * @param right
		 */
		public Condition(String left,String operator,short right)
		{
			setLeftOperand(left);
			setOperator(operator);
			setRightOperand(right);
		}
		
		/**
		 * Creates a condition object with specified operands and operator.
		 * @param left
		 * @param operator
		 * @param right
		 */
		public Condition(String left,String operator,int right)
		{
			setLeftOperand(left);
			setOperator(operator);
			setRightOperand(right);
		}
		
		/**
		 * Creates a condition object with specified operands and operator.
		 * @param left
		 * @param operator
		 * @param right
		 */
		public Condition(String left,String operator,long right)
		{
			setLeftOperand(left);
			setOperator(operator);
			setRightOperand(right);
		}
		
		/**
		 * Creates a condition object with specified operands and operator.
		 * @param left
		 * @param operator
		 * @param right
		 */
		public Condition(String left,String operator,float right)
		{
			setLeftOperand(left);
			setOperator(operator);
			setRightOperand(right);
		}
		
		/**
		 * Creates a condition object with specified operands and operator.
		 * @param left
		 * @param operator
		 * @param right
		 */
		public Condition(String left,String operator,double right)
		{
			setLeftOperand(left);
			setOperator(operator);
			setRightOperand(right);
		}
		
		/**
		 * Creates a condition object with specified operands and "=" operator.
		 * @param left
		 * @param operator
		 * @param right
		 */
		public Condition(String left,Object right)
		{
			this(left,"=",right);
		}
	
		/**
		 * Creates a condition object with specified operands and "=" operator.
		 * @param left
		 * @param operator
		 * @param right
		 */
		public Condition(String left,boolean right)
		{
			this(left,"=",right);
		}
		
		/**
		 * Creates a condition object with specified operands and "=" operator.
		 * @param left
		 * @param operator
		 * @param right
		 */
		public Condition(String left,byte right)
		{
			this(left,"=",right);
		}
		
		/**
		 * Creates a condition object with specified operands and "=" operator.
		 * @param left
		 * @param operator
		 * @param right
		 */
		public Condition(String left,char right)
		{
			this(left,"=",right);
		}
		
		/**
		 * Creates a condition object with specified operands and "=" operator.
		 * @param left
		 * @param operator
		 * @param right
		 */
		public Condition(String left,short right)
		{
			this(left,"=",right);
		}
		
		/**
		 * Creates a condition object with specified operands and "=" operator.
		 * @param left
		 * @param operator
		 * @param right
		 */
		public Condition(String left,int right)
		{
			this(left,"=",right);
		}
		
		/**
		 * Creates a condition object with specified operands and "=" operator.
		 * @param left
		 * @param operator
		 * @param right
		 */
		public Condition(String left,long right)
		{
			this(left,"=",right);
		}
		
		/**
		 * Creates a condition object with specified operands and "=" operator.
		 * @param left
		 * @param operator
		 * @param right
		 */
		public Condition(String left,float right)
		{
			this(left,"=",right);
		}
		
		/**
		 * Creates a condition object with specified operands and "=" operator.
		 * @param left
		 * @param operator
		 * @param right
		 */
		public Condition(String left,double right)
		{
			this(left,"=",right);
		}
		
		/**
		 * @return left operand
		 */
		public String getLeftOperand()
		{
			return leftOperand;
		}
		
		/**
		 * Sets specified left operand.
		 * @param leftOperand
		 */
		public void setLeftOperand(String leftOperand)
		{
				if(leftOperand==null || leftOperand.length()==0)
					throw new NullPointerException("Left operand cannot be null or zero-length.");
			this.leftOperand=leftOperand;
		}
		
		/**
		 * @return operator used.
		 */
		public String getOperator()
		{
			return operator;
		}
		
		/**
		 * Sets the operator to be used.
		 * @param operator
		 */
		public void setOperator(String operator)
		{
				if(operator==null || operator.length()==0)
					throw new NullPointerException("Operator cannot be null or zero-length.");
			this.operator=operator;
		}
		
		/**
		 * @return Right operand used.
		 */
		public Object getRightOperand()
		{
			return rightOperand;
		}
		
		/**
		 * Sets right operand to be used.
		 * @param rightOperand
		 */
		public void setRightOperand(Object rightOperand)
		{
			this.rightOperand=rightOperand;
		}
		
		/**
		 * Sets right operand to be used.
		 * @param val
		 */
		public void setRightOperand(byte val)
		{
			this.rightOperand=new Byte(val);
		}
		
		/**
		 * Sets right operand to be used.
		 * @param val
		 */
		public void setRightOperand(boolean val)
		{
			this.rightOperand=new Boolean(val);
		}
		
		/**
		 * Sets right operand to be used.
		 * @param val
		 */
		public void setRightOperand(char val)
		{
			this.rightOperand=new Character(val);
		}
		
		/**
		 * Sets right operand to be used.
		 * @param val
		 */
		public void setRightOperand(short val)
		{
			this.rightOperand=new Short(val);
		}

		/**
		 * Sets right operand to be used.
		 * @param val
		 */
		public void setRightOperand(int val)
		{
			this.rightOperand=new Integer(val);
		}
		
		/**
		 * Sets right operand to be used.
		 * @param val
		 */
		public void setRightOperand(long val)
		{
			this.rightOperand=new Long(val);
		}
		
		/**
		 * Sets right operand to be used.
		 * @param val
		 */
		public void setRightOperand(float val)
		{
			this.rightOperand=new Float(val);
		}
		
		/**
		 * Sets right operand to be used.
		 * @param val
		 */
		public void setRightOperand(double val)
		{
			this.rightOperand=new Double(val);
		}

		/**
		 * Two conditions are considered to be equal if and only if both uses equal
		 * operands and operator.
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object other)
		{
				if(this==other)
					return true;
				
				if(!(other instanceof Condition))
					return false;
			
			Condition otherCond=(Condition)other;
			
			return rightOperand.equals(otherCond.rightOperand) && 
					operator.equals(otherCond.operator) &&
					leftOperand.equals(otherCond.leftOperand);
		}

		/**
		 * Hash code for this object, is the sum of hash codes of both operands and
		 * operator. 
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode()
		{
			return rightOperand.hashCode()+operator.hashCode()+leftOperand.hashCode();
		}
		
		public String toString()
		{
			return leftOperand+" "+operator+" "+rightOperand;
		}
}
