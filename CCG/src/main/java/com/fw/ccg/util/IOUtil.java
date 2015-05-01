package com.fw.ccg.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

public class IOUtil
{
	public static byte[] toRawData(InputStream is)
	{
			try
			{
				ByteArrayOutputStream bos=new ByteArrayOutputStream();
				byte buff[]=new byte[1024];
				int read=0;
				
					while((read=is.read(buff))>0)
					{
						bos.write(buff,0,read);
					}
					
				return bos.toByteArray();
			}catch(Exception ex)
			{
				throw new IllegalStateException("An error occured while reading input stream",ex);
			}
	}
	
	public static String getPath(File file)
	{
			try
			{
				return file.getCanonicalPath();
			}catch(Exception ex)
			{
				return file.getAbsolutePath();
			}
	}
	
	public static void copyFile(File srcFile,File dstFile)
	{
			if(!srcFile.isFile())
				throw new IllegalArgumentException("Source file should be a file: "+srcFile.getAbsolutePath());
			
			if(dstFile.isDirectory())
				dstFile=new File(dstFile,srcFile.getName());
			
			try
			{
				FileInputStream fis=new FileInputStream(srcFile);
				FileOutputStream fos=new FileOutputStream(dstFile);
				byte buff[]=new byte[1024];
				int read=0;
					while((read=fis.read(buff))>0)
						fos.write(buff,0,read);
				
				fos.flush();
				fos.close();
				fis.close();
			}catch(IOException ex)
			{
				throw new IllegalStateException("An error occured while copying file.",ex);
			}
	}
	
	public static void copyFiles(File srcFile[],File dstFile)
	{
			if(!dstFile.isDirectory())
				throw new IllegalArgumentException("Destination file should be a directory: "+getPath(dstFile));
			
			for(int i=0;i<srcFile.length;i++)
				copyFile(srcFile[i],dstFile);
	}
	
	public static void delete(File dir)
	{
			if(!dir.isDirectory())
			{
					if(!dir.delete())
						throw new IllegalStateException("Failed to delete file: "+getPath(dir));
					
				return;
			}
			
		dir.listFiles(new FileFilter(){
			public boolean accept(File pathname)
			{
				delete(pathname);
				return false;
			}
		});
		
			if(!dir.delete())
				throw new IllegalStateException("Failed to delete directory: "+getPath(dir));
	}
	
	public static List<File> getRecursiveFileList(File dir,String... extensions)
	{
			if(!dir.exists() || !dir.isDirectory())
				throw new IllegalArgumentException("Invalid directory specified.");
			
		final ArrayList<File> lst=new ArrayList<File>();
		final HashSet<String> extSet=new HashSet<String>();
		
			if(extensions!=null)
			{
				for(int i=0;i<extensions.length;i++)
					extSet.add(extensions[i].toLowerCase());
			}
			
		FileFilter filter=new FileFilter(){
				public boolean accept(File path)
				{
						if(path.isDirectory())
							return false;
					
						if(extSet.isEmpty())
						{
							lst.add(path);
							return false;
						}
						
					String name=path.getName().toLowerCase();
					int idx=name.lastIndexOf(".");
						if(idx<0 || idx==name.length()-1)
							return false;
						
					name=name.substring(idx+1);
						if(extSet.contains(name))
							lst.add(path);
						
					return false;
				}
			};
			
		recurse(dir,filter,false);
		return lst;
	}

	public static void recurse(File root,final FileFilter filter,boolean dirAlso)
	{
			if(root==null || !root.exists() || !root.isDirectory())
				throw new IllegalArgumentException("Invalid root file specified: "+root);
			
		final Stack<File> dirStack=new Stack<File>();
		dirStack.push(root);
		
		FileFilter mainFilter=new FileFilter()
			{
				public boolean accept(File path)
				{
						if(path.isDirectory())
						{
							dirStack.push(path);
							return false  ;
						}
					filter.accept(path);
					return false;
				}
			};
			
		File file=null;
		ArrayList<File> dirList=(dirAlso)?new ArrayList<File>():null;
			
			while(!dirStack.isEmpty())
			{
				file=dirStack.pop();
				file.listFiles(mainFilter);
				
					if(dirAlso)
						dirList.add(file);
			}
			
			if(!dirAlso)
				return;

			for(int i=dirList.size()-1;i>=0;i--)
				filter.accept(dirList.get(i));
		
	}
}
