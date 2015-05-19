/**
 * 
 */
package com.fw.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A Utility class which helps in locking objects till they are released. This class will
 * ensure an object is locked at a time by single thread. Other thread which tries to lock
 * same object will wait till lock is obtained.
 * 
 * This is done by using single ReentrantLock and pool of conditions.
 * 
 * Please note. If same thread tries to acquire lock on same object twice, there will be dead lock.
 * 
 * @author akiran
 */
public class ObjectLockManager
{
	private static Logger logger = LogManager.getLogger(ObjectLockManager.class);
	
	/**
	 * Lock to synchronize the threads will obtaining object lock
	 */
	private ReentrantLock lock = new ReentrantLock();
	
	/**
	 * Maintains the objects that are locked and their corresponding conditions. Conditions are used to 
	 * notify other waiting threads for same object lock.
	 */
	private Map<Object, Condition> objectToConditions = new HashMap<Object, Condition>();
	
	/**
	 * Pool of conditions. So that condition objects can be reused
	 */
	private List<Condition> conditionPool = new ArrayList<Condition>();
	
	/**
	 * Keeps track maximum pool size that was used during this lock manager
	 * life
	 */
	private int maxConditionsUsed = 0;
	
	public boolean isObjectLocked(Object object)
	{
		return  (objectToConditions.get(object) != null);
	}

	/**
	 * Locks the specified "object" till {@link #releaseObject(Object)} is called with same object. Other threads
	 * which tries to obtain lock on same object, will wait till lock is obtained
	 * @param object
	 * @throws InterruptedException 
	 */
	public void lockObject(Object object) throws InterruptedException
	{
		lock.lock();
		
		try
		{
			//wait till condition is present (that is locked) for the current object
			Condition condition = objectToConditions.get(object);
	
			while(condition != null)
			{
				//note different iterations may get different conditions
				// based on the number of threads running parallely on same object
				logger.debug("Waiting for lock on object - {}", object);
					
				condition.await();
				
				condition = objectToConditions.get(object);
			}
			
			//if no conditions are available in pool
			if(conditionPool.isEmpty())
			{
				//create new condition
				condition = lock.newCondition();
				
				maxConditionsUsed++;
			}
			//if pool has conditions
			else
			{
				//reuse the condition from pool
				condition = conditionPool.remove(conditionPool.size() - 1);
			}
			
			//put the condition on map locking the object
			objectToConditions.put(object, condition);
		}finally
		{
			lock.unlock();			
		}
	}
	
	/**
	 * Releases the lock obtained on specified "object" by {@link #lockObject(Object)}. This will signal
	 * all the other threads waiting for locking this object. 
	 * @param object
	 */
	public void releaseObject(Object object)
	{
		lock.lock();
		
		try
		{
			//get the condition-lock of specified object
			Condition condition = objectToConditions.get(object);
			
			//if the object was not locked
			if(condition == null)
			{
				throw new IllegalStateException("Specified object is not locked by this manager - " + object);
			}
			
			//remove the lock
			objectToConditions.remove(object);
			
			//add the condition back to pool
			conditionPool.add(condition);
			
			//signal other threads which are waiting for this object
			condition.signalAll();
		}finally
		{
			lock.unlock();
		}
	}
	
	/**
	 * Gets maxumum number of conditions used by this manager
	 * @return the maxConditionsUsed
	 */
	public int getMaxConditionsUsed()
	{
		return maxConditionsUsed;
	}
	
	/**
	 * Obtains number of locks controlled by this manager currently.
	 * @return
	 */
	public int getLockCount()
	{
		return objectToConditions.size();
	}
}
