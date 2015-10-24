package com.fw.persistence.listeners;

import java.lang.reflect.Method;

/**
 * Holds listener method details for event handling
 * @author akiran
 */
public class EntityListener
{
	/**
	 * Object containing listener method
	 */
	private Object listenerContainer;
	
	/**
	 * Listener method
	 */
	private Method listenerMethod;
	
	/**
	 * Does listener method accept event object
	 */
	private boolean hasEventArg;

	public EntityListener(Object listenerContainer, Method listenerMethod, boolean hasEventArg)
	{
		this.listenerContainer = listenerContainer;
		this.listenerMethod = listenerMethod;
		this.hasEventArg = hasEventArg;
	}
	
	/**
	 * Invokes the listener method with specified event object
	 * @param e
	 */
	public void invoke(EntityEvent e)
	{
		try
		{
			if(hasEventArg)
			{
				listenerMethod.invoke(listenerContainer, e);
				return;
			}
			
			listenerMethod.invoke(listenerContainer);
		}catch(Exception ex)
		{
			throw new IllegalStateException("An error occurred while invoking ");
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder(super.toString());
		
		builder.append("[");
		builder.append(listenerContainer.getClass().getName()).append(".").append(listenerMethod.getName()).append("()");
		builder.append("]");
		
		return builder.toString();
	}

}
