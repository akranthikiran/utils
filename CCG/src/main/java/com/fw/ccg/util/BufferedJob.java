package com.fw.ccg.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BufferedJob<T>
{
	private static Logger logger = LogManager.getLogger(BufferedJob.class);
	
	public static abstract class AbstractContext<T>
	{
		protected int count = 0;
		private HashMap<String, Object> attrMap;
		protected BufferedJob<T> job;

		protected AbstractContext(BufferedJob<T> job)
		{
			this.job = job;
		}

		public BufferedJob<T> getJob()
		{
			return job;
		}

		public int getCount()
		{
			return count;
		}

		public void addAttribute(String name, Object value)
		{
			if(attrMap == null)
				attrMap = new HashMap<String, Object>();

			attrMap.put(name, value);
		}

		public Object getAttribute(String name)
		{
			if(attrMap == null)
				return null;

			return attrMap.get(name);
		}
	}

	public static class ProviderContext<T> extends AbstractContext<T>
	{
		private ProviderContext(BufferedJob<T> job)
		{
			super(job);
		}

		public synchronized boolean addData(T data)
		{
			count++;
			return job.addData(data);
		}
	}

	public static class DigesterContext<T> extends AbstractContext<T>
	{
		private DigesterContext(BufferedJob<T> job)
		{
			super(job);
		}

		public synchronized T nextData()
		{
			count++;
			return job.nextData();
		}
	}

	public static interface DataProvider<T>
	{
		public void perform(ProviderContext<T> provContext, Object providerData);
	}

	public static interface DataDigester<T>
	{
		public void perform(DigesterContext<T> digContext, Object digesterData);
	}

	private LinkedList<T> buffer = new LinkedList<T>();
	private DataProvider<T> provider;
	private DataDigester<T> digester;

	/**
	 * When data buffer is empty, Digester is made to wait for "dataCheckPeriod" before re-checking buffer for new data.
	 */
	private int dataCheckPeriod = 100;

	/**
	 * When max number of providers are already started, background thread waits for "providerCheckPeriod" 
	 * before re-checking the active providers completion and if new provider can be started. 
	 */
	private int providerCheckPeriod = 1000;
	private int digesterCheckPeriod = 1000;

	private boolean started = false;
	private boolean errored = false;
	private boolean providersCompleted = false;
	private Set<Thread> providerThreads = new HashSet<Thread>();
	private Set<Thread> digesterThreads = new HashSet<Thread>();

	public BufferedJob(DataProvider<T> provider, DataDigester<T> digester)
	{
		this.provider = provider;
		this.digester = digester;
	}

	public int getDataCheckPeriod()
	{
		return dataCheckPeriod;
	}

	public void setDataCheckPeriod(int dataCheckPeriod)
	{
		if(dataCheckPeriod <= 0)
			throw new IllegalArgumentException("Invalid check period specified: " + dataCheckPeriod);

		this.dataCheckPeriod = dataCheckPeriod;
	}

	public int getProviderCheckPeriod()
	{
		return providerCheckPeriod;
	}

	public void setProviderCheckPeriod(int providerCheckPeriod)
	{
		if(providerCheckPeriod <= 0)
			throw new IllegalArgumentException("Invalid check period specified: " + providerCheckPeriod);

		this.providerCheckPeriod = providerCheckPeriod;
	}

	public int getDigesterCheckPeriod()
	{
		return digesterCheckPeriod;
	}

	public void setDigesterCheckPeriod(int digesterCheckPeriod)
	{
		if(digesterCheckPeriod <= 0)
			throw new IllegalArgumentException("Invalid check period specified: " + digesterCheckPeriod);

		this.digesterCheckPeriod = digesterCheckPeriod;
	}

	private synchronized void addProviderThread(Thread th)
	{
		providerThreads.add(th);
	}

	private synchronized void addDigesterThread(Thread th)
	{
		digesterThreads.add(th);
	}

	private synchronized int getActiveProvCount()
	{
		int count = 0;

		for(Thread th : providerThreads)
		{
			if(th.isAlive())
				count++;
		}

		return count;
	}

	private synchronized int getActiveDigCount()
	{
		int count = 0;

		for(Thread th : digesterThreads)
		{
			if(th.isAlive())
				count++;
		}

		return count;
	}

	private void startProvider(String mainThreadName, final Object provData, int idx)
	{
		Thread providerThread = new Thread(mainThreadName + "-BuffJob-Prov-" + idx)
		{
			public void run()
			{
				try
				{
					provider.perform(new ProviderContext<T>(BufferedJob.this), provData);
				}catch(Exception ex)
				{
					logger.error("An error occured in provider", ex);
					errored = true;
				}
			}
		};

		providerThread.start();
		addProviderThread(providerThread);
	}

	private void startDigester(String mainThreadName, final Object digData, int idx)
	{
		Thread digesterThread = new Thread(mainThreadName + "-BuffJob-Dig-" + idx)
		{
			public void run()
			{
				try
				{
					digester.perform(new DigesterContext<T>(BufferedJob.this), digData);
				}catch(Exception ex)
				{
					logger.error("An error occured in provider", ex);
					errored = true;
				}
			}
		};

		digesterThread.start();
		addDigesterThread(digesterThread);
	}

	private Thread startProviders(final Object providerData[], final int maxConcProv)
	{
		final String name = Thread.currentThread().getName();

		Thread th = new Thread(name + "-Provider-Starter")
		{
			public void run()
			{
				for(int i = 0; i < providerData.length; i++)
				{
					startProvider(name, providerData[i], i);

					if((i + 1) >= maxConcProv)
					{
						while(getActiveProvCount() >= maxConcProv)
						{
							if(errored)
								return;

							try
							{
								Thread.sleep(providerCheckPeriod);
							}catch(Exception ex)
							{}
						}
					}
				}
			}
		};

		th.start();
		return th;
	}

	private Thread startDigesters(final Object digesterData[], final int maxConcDig)
	{
		final String name = Thread.currentThread().getName();

		Thread th = new Thread(name + "-Digester-Starter")
		{
			public void run()
			{
				for(int i = 0; i < digesterData.length; i++)
				{
					startDigester(name, digesterData[i], i);

					if((i + 1) >= maxConcDig)
					{
						while(getActiveDigCount() >= maxConcDig)
						{
							if(errored)
								return;

							try
							{
								Thread.sleep(digesterCheckPeriod);
							}catch(Exception ex)
							{}
						}
					}
				}

			}
		};

		th.start();
		return th;
	}

	public void start()
	{
		startMultiple(new Object[]{null}, -1, new Object[]{null}, -1);
	}

	public void startMultipleProviders(Object providerData[], int maxConcProv)
	{
		startMultiple(providerData, maxConcProv, new Object[]{null}, -1);
	}

	public void startMultipleDigesters(Object digesterData[], int maxConcDig)
	{
		startMultiple(new Object[]{null}, -1, digesterData, maxConcDig);
	}

	private void join()
	{
		for(Thread provider : providerThreads)
		{
			if(!provider.isAlive())
				continue;

			try
			{
				provider.join();
			}catch(Exception ex)
			{}
		}

		providersCompleted = true;

		for(Thread digester : digesterThreads)
		{
			if(!digester.isAlive())
				continue;

			try
			{
				digester.join();
			}catch(Exception ex)
			{}
		}
	}

	public void startMultiple(Object providerData[], int maxConcProv, Object digesterData[], int maxConcDig)
	{
		synchronized(providerThreads)
		{
			if(providerData == null || providerData.length == 0)
				throw new NullPointerException("Provider data cannot be null or empty.");

			if(digesterData == null || providerData.length == 0)
				throw new NullPointerException("Digester data cannot be null or empty.");

			if(started)
				throw new IllegalStateException("Job is already started.");

			if(maxConcProv <= 0)
				maxConcProv = Integer.MAX_VALUE;

			if(maxConcDig <= 0)
				maxConcDig = Integer.MAX_VALUE;

			Thread digStarter = startDigesters(digesterData, maxConcDig);
			Thread provStarter = startProviders(providerData, maxConcProv);
			started = true;

			try
			{
				provStarter.join();
				digStarter.join();
			}catch(Exception ex)
			{
				ex.printStackTrace();
			}

			join();
		}
	}

	private synchronized boolean addData(T data)
	{
		if(errored)
			return false;

		if(data == null)
			return true;

		buffer.add(data);
		super.notifyAll();
		return true;
	}

	private synchronized T nextData()
	{
		if(errored)
			return null;

		if(!buffer.isEmpty())
			return buffer.remove(0);

		if(providersCompleted)
			return null;

		while(!providersCompleted)
		{
			if(!buffer.isEmpty())
				break;

			try
			{
				wait(dataCheckPeriod);
			}catch(Exception ex)
			{}
		}

		if(!buffer.isEmpty())
			return buffer.remove(0);

		return null;
	}

	public int getBufferedDataSize()
	{
		return buffer.size();
	}

	public boolean isJobCompleted()
	{
		if(!started)
			throw new IllegalStateException("Job is not yet started.");

		return getActiveDigCount() <= 0;
	}

	public boolean isErrored()
	{
		return errored;
	}
}
