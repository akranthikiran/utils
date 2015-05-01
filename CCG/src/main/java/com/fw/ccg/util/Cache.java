package com.fw.ccg.util;

import java.io.IOException;


/**
 * © Copyright 2006 IBM Corporation
 * <BR><BR>
 * An interface represenitng caching mechanism based on integer indexes. In this mechanism
 * the objects are accessed using the index returned during the addition of object to the
 * caching mechanism.
 * <BR><BR>
 * @author A. Kranthi Kiran
 */
public interface Cache
{
	/**
	 * Returns number of objects currently present on the cache.
	 * @return Number of objects currently present on the cache. 
	 */
	public int getObjectCount();
	
	/**
	 * Removes object at the specified index from the cache. The future reference of this
	 * index to fetch the object will throw CacheException.
	 * <BR> 
	 * If idx is less than zero or represents deleted index, this method will not have any effect.
	 * @param idx Index of the object that needs to be deleted.
	 */
	public void removeObject(int idx);

	/**
	 * Writes the specified object to the cache and an index will get generated for obj  
	 * and returns the same. This index should be used to retrieve and delete obj from  
	 * the cache.
	 * <BR>
	 * The indexes being returned by sequential calls to this method may not be in 
	 * sequence. 
	 * <BR>
	 * @param obj  Objects that needs to be kept on the cache.
	 * @return new index of the specified object.
	 */
	public int writeObject(Object obj) throws IOException;

	/**
	 * Replaces the object at specified index with the specified object. If idx is less
	 * than zero or greater than the available indesxes then this method is equivalent to 
	 * calling writeObject().
	 * 
	 * @param idx Index of the object to be replaced.
	 * @param obj Replacing object.
	 * @return The index of the replacing object. If idx is is within the bounds, then this value
	 * 			will be same as idx. 
	 */
	public int replaceObject(int idx,Object obj) throws IOException;
	
	/**
	 * Reads the object from the cache which is present at the specified index.
	 * 
	 * @param objIdx Index of the object which needs to be read.
	 * @return Object at objIdx.
	 */
	public Object readObject(int objIdx);

	/**
	 * After closing of the cache, any method calls on this cache after this method 
	 * call will throw CacheException.
	 */
	public void close();
	
	/**
	 * @return True if the cache is already closed.
	 */
	public boolean isClosed();

	/**
	 * Clears all the objects in the cache. And restarts the object indexing.
	 */
	public void clear();

	/**
	 * This method will optimize the resources being used by this cache. This function
	 * call may be ignored by cache mechanims, which doesnt need any explicit call for 
	 * optimization of resources (self-optimized).
	 */
	public void optimize();
}
