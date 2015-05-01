package com.fw.ccg.manager;

import java.lang.reflect.Method;

/**
 * This interface objects is used to customize the dynamic manager methods. Whenever
 * a proxy method is invoked on the dynamic manager, execute() method od this object will
 * be called with the passed attributes. 
 * <BR><BR>
 * @author A. Kranthi Kiran
 */
public interface ManagerMethodProxy
{
	/**
	 * A constant expected to be used by execute() method.
	 */
	public Object CONTINUE_DEFAULT=new Object();
	
	/**
	 * This methoe should execute the customize behaviour of the proxy methods and return
	 * appropriate value for the proxy method.
	 * If this method returns  CONTINUE_DEFAULT the default process of invoking methods 
	 * will be performed.
	 * @param manager Manager on which proxy method is invoked.
	 * @param method Invoked proxy method.
	 * @param args Arguments passed to the proxy method.
	 * @return Return value for the proxy method.
	 */
	public Object execute(Object manager,Method method,Object args[]);
}
