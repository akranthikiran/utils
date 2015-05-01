package com.fw.ccg.event;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fw.ccg.util.CCGUtility;

public class EventListenerAdapter<L>
{
	private static Logger logger = LogManager.getLogger(EventListenerAdapter.class);

	private class MethodHandler implements InvocationHandler
	{
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
		{
			if(listenerLst.isEmpty())
			{
				return null;
			}

			Object retValue = null;

			for(L listener : listenerLst)
			{
				try
				{
					retValue = method.invoke(listener, args);
				}catch(Exception ex)
				{
					if(ignoreErrors)
					{
						logger.error("An error occured while invoking listener methods", ex);
					}
					else
					{
						throw ex;
					}
				}
			}

			if(retValue == null && method.getReturnType().isPrimitive())
			{
				retValue = CCGUtility.getDefaultPrimitiveValue(method.getReturnType());
			}

			return retValue;
		}

	}

	private List<L> listenerLst = new LinkedList<L>();
	private L proxy;
	private boolean ignoreErrors = true;

	@SuppressWarnings("unchecked")
	public EventListenerAdapter(Class<L> listenerType)
	{
		MethodHandler methodHandler = new MethodHandler();
		proxy = (L)Proxy.newProxyInstance(EventListenerAdapter.class.getClassLoader(), new Class<?>[]{listenerType}, methodHandler);
	}

	public L get()
	{
		return proxy;
	}

	public void addListener(L listener)
	{
		listenerLst.add(listener);
	}

	public void removeListener(L listener)
	{
		listenerLst.remove(listener);
	}

	public boolean isIgnoreErrors()
	{
		return ignoreErrors;
	}

	public void setIgnoreErrors(boolean ignoreErrors)
	{
		this.ignoreErrors = ignoreErrors;
	}

}
