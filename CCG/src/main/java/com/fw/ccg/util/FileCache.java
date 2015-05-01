package com.fw.ccg.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;

/**
 * © Copyright 2006 IBM Corporation
 * <BR><BR>
 * <P>
 * FileCache represents cache for the objects maintained in files. <I>Only serializable 
 * objects can be used with the FileCache.</I>
 * <BR><BR>
 * FileCache uses currently two files for maintaining cache (for the sake of efficiency) 
 * - an index file and a cache file. FileCache is optimized for retrieval and storing 
 * of objects. But removal of objects may create gaps (which become useless currently) 
 * in cache file. Thus, huge number of removals may result in huge cache files with empty 
 * spaces.
 * </P> 
 * <UL>
 * 	<LI>
 * 		<B><I>Index file</I></B> can be viewed as an array of "long" values which 
 * 		represents pointers to the cache file. For example, long value at index 4 will 
 * 		represent the pointer in the cache file where object represented with id 4 is 
 * 		stored.
 * 		<BR>
 * 		When an object is written to the cache using writeObject() an index for that 
 * 		object will get generated and the same is returned. This index should be used 
 * 		for retrieving or deleting that object from the cache.
 * 	</LI>
 * 	<LI>
 * 		<B><I>CacheFile</I></B> contains the raw byte conversions of the objects on the 
 * 		cache.
 * 	</LI>
 * </UL>
 * optimize() method will remove all the unused spaces in the cache file 
 * that might have created due to deletions. Note, optimize() method call will not have 
 * any optimization on index file, the indexes which have been deleted will still be 
 * maintained. This is needed to be sync with the indexes returned by cache mechanism 
 * when writing objects. 
 * <BR><BR>
 * When all the objects are removed from the cache, this mechanism will automatically 
 * truncates the files and restarts the indexing for new objects.
 * <BR><BR>
 * <B>Note: All the methods in this cache mechanism are synchronized.</B>
 * <BR><BR>
 * @author A. Kranthi Kiran
 */
public class FileCache implements Cache
{
	private static final int LONG_SIZE=8;
	private static final int INT_SIZE=4;
	
	private String cachePath,idxPath;//paths are used to delete tmp files at closing
	private RandomAccessFile cacheFile;//to store objects
	private RandomAccessFile idxFile;//to store free memory gap pointers
	private int nextObjIdx=0;
	private int objectCount=0;
	
		/**
		 * Uses OS specific temporary files and temporary directory for indexing 
		 * and caching.
		 */
		public FileCache()
		{
			try
			{
				setFiles(File.createTempFile("ObjCacheFile",".idx"),
						File.createTempFile("ObjCacheFile",".dat"));
			}catch(Exception ex)
			{
				throw new CacheException("Error in creating cache files.",ex);
			}
		}
		
		/**
		 * Creates index and cache files with names fileName+".idx" and fileName+".dat"
		 * respectively in the specified path.
		 * <BR> 
		 * If the files already exisits with this name, they will get deleted.
		 * <BR>
		 * @param path Direcory path where cache files should be maintained.
		 * @param fileName Name of the cache files to be maitained
		 */
		public FileCache(File path,String fileName)
		{
				if(path==null)
					throw new NullPointerException("Path cannot be null.");
				
				if(!path.exists() || !path.isDirectory())
					throw new IllegalArgumentException("Specified path is not an exisiting directory: "+path.getAbsolutePath());
			
				if(fileName==null || fileName.trim().length()==0)
					throw new NullPointerException("File Name cannot be null or empty.");
			fileName=fileName.trim();
			File idxFile=new File(path,fileName+".idx");
			File cacheFile=new File(path,fileName+".dat");
			setFiles(idxFile,cacheFile);
		}
		
		/**
		 * Creates index and cache files with names fileName+".idx" and fileName+".dat"
		 * respectively in the specified path.
		 * <BR> 
		 * If the files already exists with this name, they will get deleted.
		 * <BR>
		 * @param path Direcory path where cache files should be maintained.
		 * @param fileName Name of the cache files to be maitained
		 */
		public FileCache(String path,String fileName)
		{
			this(new File(path),fileName);
		}
		
		/**
		 * Uses specified files for caching and indexing.
		 * <BR>
		 * @param idx  Index file
		 * @param cache Cache file.
		 */
		private void setFiles(File idx,File cache)
		{
				if(idx.exists())
					idx.delete();
				
				if(cache.exists())
					cache.delete();
				
				try
				{
					cachePath=cache.getAbsolutePath();
					cacheFile=new RandomAccessFile(cache,"rw");
					
					idxPath=idx.getAbsolutePath();
					idxFile=new RandomAccessFile(idx,"rw");
				}catch(Exception ex)
				{
					throw new CacheException("Error in creating cache files.",ex);
				}
		}
		
		/**
		 * Returns number of objects currently present on the cache.
		 * @return Number of objects currently present on the cache. 
		 */
		public int getObjectCount()
		{
			return objectCount;
		}
		
		/**
		 * Returns the absolute path of the index file being used by this cache.
		 * @return The absolute path of the index file being used by this cache. 
		 */
		public String getIndexFile()
		{
			return idxPath;
		}
		
		/**
		 * Returns the absolute path of the cache file being used by this cache.
		 * @return The absolute path of the cache file being used by this cache.
		 */
		public String getCacheFile()
		{
			return cachePath;
		}
		
		/**
		 * Converts an serializable object into byte array (sequence of raw bytes).
		 * <BR>
		 * @param obj Object which needs to get converted into bytes.
		 * @return Converted raw bytes of the object.
		 * @throws IOException
		 */
		private synchronized byte[] getByteDataFromObject(Object obj) throws IOException
		{
			ByteArrayOutputStream bos=new ByteArrayOutputStream();
			ObjectOutputStream oos=new ObjectOutputStream(bos);
			oos.writeObject(obj);
			oos.flush();
			return bos.toByteArray();
		}
	
		/**
		 * Reverse operation of getByteDataFromObject(). This method converts raw bytes into
		 * an object and returns the same.
		 * <BR>
		 * @param data  Raw bytes representing the object.
		 * @return Object built from the specified raw bytes.
		 * @throws ClassNotFoundException 
		 * @throws IOException
		 */
		private synchronized Object getObjectFromByteData(byte data[]) throws IOException, ClassNotFoundException
		{
			ByteArrayInputStream bos=new ByteArrayInputStream(data);
			ObjectInputStream oos=new ObjectInputStream(bos);
			return oos.readObject();
		}
		
		/**
		 * Removes object at the specified index from the cache. 
		 * <BR> 
		 * If idx is out of bounds or represents deleted index, this method will not have 
		 * any effect.
		 * <BR>
		 * @param idx Index of the object that needs to be deleted.
		 */
		public synchronized void removeObject(int idx)
		{
				if(idxFile==null)
					throw new CacheException("Cache is already closed.");
				
				if(idx<0 || idx>=nextObjIdx)
					return;
				
				try
				{
					idxFile.seek(idx*LONG_SIZE);
					idxFile.writeLong(-1);
					objectCount--;
						if(objectCount<=0)
							clear();
				}catch(Exception ex)
				{
					throw new CacheException("Error in updating index file.",ex);
				}
		}
		
		/**
		 * Writes the specified object to the cache and an index will get generated for obj  
		 * and returns the same. This index should be used to retrieve and delete obj from  
		 * the cache.
		 * <BR>
		 * The indexes being returned by sequential calls to this method may not be in 
		 * sequence. 
		 * <BR>
		 * @param obj  Objects thats needs to be kept on the cache.
		 * @return new index of the specified object.
		 */
		public synchronized int writeObject(Object obj)throws IOException
		{
				if(idxFile==null)
					throw new CacheException("Cache is already closed.");
			
			return replaceObject(-1,obj);
		}
		
		/**
		 * Replaces the object at specified index with the specified object. If idx is less
		 * than zero then this method is equivalent to calling writeObject()
		 * 
		 * @param idx Index of the object to be replaced.
		 * @param obj Replacing object.
		 * @return The index of the replacing object. If idx is within the bounds this value
		 * 			will be same as idx. 
		 */
		public synchronized int replaceObject(int idx,Object obj)throws IOException
		{
				if(idxFile==null)
					throw new CacheException("Cache is already closed.");
				
				if(obj==null)
					throw new NullPointerException("Null objects cannot be stored on cache.");
				
				if(!(obj instanceof Serializable))
					throw new IllegalArgumentException("Only serializable objects can be placed on the cache.");
				
				if(idx>=nextObjIdx)
					throw new IndexOutOfBoundsException("Specified index is out of bounds: "+idx);
			
			byte data[]=getByteDataFromObject(obj);
			
			//write the object
			long ptr=cacheFile.length();
			
			cacheFile.seek(cacheFile.length());
			cacheFile.writeInt(data.length);
			cacheFile.write(data);
				if(idx<0)
				{
					//append the pointer of the new object
					idxFile.seek(idxFile.length());
					idxFile.writeLong(ptr);
				}
				else
				{
					//replace specified index pointer with the new object pointer
					idxFile.seek(idx*LONG_SIZE);
					idxFile.writeLong(ptr);
				}
			
				if(idx>=0)
					return idx;
				
			objectCount++;
			return nextObjIdx++;
		}
		
		/**
		 * Reads the object from the cache which is present at the specified index.
		 * 
		 * @param objIdx Index of the object which needs to be read.
		 * @return Object at objIdx.
		 */
		public synchronized Object readObject(int objIdx)
		{
			if(idxFile==null)
				throw new CacheException("Cache is already closed.");
			
			if(objIdx<0 || objIdx>=nextObjIdx)
				throw new IndexOutOfBoundsException("Specified index is out of bounds: "+objIdx);
			
			try
			{
				//Fetch the pointer from the index file
				idxFile.seek(objIdx*LONG_SIZE);
				long ptr=idxFile.readLong();
					//check whether this index points to deleted object
					if(ptr==-1)
						return null;
					
				//read the object structure from the cache file
				cacheFile.seek(ptr);
				int len=cacheFile.readInt();
				byte data[]=new byte[len];
				
				read(data);
				return getObjectFromByteData(data);
			}catch(Exception ex)
			{
				throw new CacheException("Error in reading object from cache.",ex);
			}
		}
		
		/**
		 * On closing of the cache, both index and cache file gets deleted. Any method calls
		 * on this cache after this method call will throw CacheException.
		 */
		public synchronized void close()
		{
				if(idxFile==null)
					throw new CacheException("Cache is already closed.");
			
				try
				{
					idxFile.close();
					cacheFile.close();
				}catch(IOException ex)
				{
					throw new CacheException("Error in closing cache files.",ex);
				}
			
			File tmp=new File(cachePath);
			tmp.delete();
			tmp=new File(idxPath);
			tmp.delete();
			
			idxFile=null;
			cacheFile=null;
		}
		
		/**
		 * Clears all the objects in the cache and truncates both index and cache files.
		 * And restarts the object indexing.
		 */
		public synchronized void clear()
		{
				if(idxFile==null)
					throw new CacheException("Cache is already closed.");
		
				try
				{
					idxFile.setLength(0);
					cacheFile.setLength(0);
				}catch(IOException ex)
				{
					throw new CacheException("Error in truncating cache files.",ex);
				}
			objectCount=0;
			nextObjIdx=0;
		}
		
		/**
		 * Closes this cache object.
		 * @see java.lang.Object#finalize()
		 */
		protected void finalize() throws Throwable
		{
				if(idxFile!=null)
					close();
		}
		
		/**
		 * This method call clears the unused space created in cache file due to deletions.
		 * <BR>
		 * This method call first creates temporary files for (both index and cache) with 
		 * the same names as respective files but appended with text "_BK" in the same folder
		 * as the current cache files exists. Then the optimized data will be copid to these
		 * files and finally existing cache files will be replaced by this optimized cache
		 * files.
		 */
		public synchronized void optimize()
		{
				if(idxFile==null)
					throw new CacheException("Cache is already closed.");
	
				try
				{
					/*
					 * Even though in following files data is fed only in forward direction,
					 * streams are not used. This is because of the difference in format how 
					 * random access file and DataOutputStream writes primitives.   
					 */
					//create some tmp files to hold the optimizd data
					RandomAccessFile idxRAFile=new RandomAccessFile(idxPath+"_BK","rw");
					RandomAccessFile datRAFile=new RandomAccessFile(cachePath+"_BK","rw");
					
					//caluculate number of objects to be copied to tmp files
					int noOfEntries=(int)(idxFile.length()/LONG_SIZE);
					long ptr=0;
					long size=0;
					
					idxFile.seek(0);
						for(int i=0;i<noOfEntries;i++)
						{
							ptr=idxFile.readLong();
								//if object is not deleted, then write the data to the buffered cache file
								if(ptr!=-1)
								{
									cacheFile.seek(ptr);
									//read the object structure from the cache file
									int len=cacheFile.readInt();
									byte data[]=new byte[len];
									
									read(data);
									
									//write the object structure to buff file
									datRAFile.writeInt(len);
									datRAFile.write(data);
									ptr=size;
									
									//keep track of the buff file size
									size+=INT_SIZE+len;
								}
							//make new pointer entry into index file
							idxRAFile.writeLong(ptr);
						}
					
					//finalize the buffered files
					idxRAFile.close();
					datRAFile.close();
				}catch(IOException ex)
				{
					throw new CacheException("Error in creating backup cache files.",ex);
				}
				
				try
				{
					//close the currently used cache files
					idxFile.close();
					cacheFile.close();
				}catch(IOException ex)
				{
					throw new CacheException("Error in closing cache files.",ex);
				}
			
			//make the file objects both for new and old files
			File newIdxFile=new File(idxPath+"_BK");
			File newCacheFile=new File(cachePath+"_BK");
			File oldIdxFile=new File(idxPath);
			File oldCacheFile=new File(cachePath);
			
			//replace old files with new files
			oldIdxFile.delete();
			oldCacheFile.delete();
			
				if(!newIdxFile.renameTo(oldIdxFile))
					throw new CacheException("Failed to replace new idx file to old idx file: "+newIdxFile);
				
				if(!newCacheFile.renameTo(oldCacheFile))
					throw new CacheException("Failed to replace new cache file to old cache file: "+newCacheFile);
			
				try
				{
					//reopen the new created files for use as cache
					cacheFile=new RandomAccessFile(oldCacheFile,"rw");
					
					idxFile=new RandomAccessFile(oldIdxFile,"rw");
				}catch(IOException ex)
				{
					throw new CacheException("Error in reopening cache files.",ex);
				}
		}
		
		/**
		 * Reads data.length number of bytes from cache file at current position into data.
		 * @param data byte buffer where read data needs to be copied.
		 * @throws IOException
		 */
		private void read(byte data[]) throws IOException
		{
			int idx=0;
			int read=0;
			int len=data.length;
				while(idx<len)
				{
					read=cacheFile.read(data,idx,len-idx);
					idx+=read;
				}
			
		}
		
		/* (non-Javadoc)
		 * @see com.ccg.util.Cache#isClosed()
		 */
		public boolean isClosed()
		{
			return (idxFile==null);
		}
}
