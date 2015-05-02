package com.test.qry;

public class Application
{
	private int appNo;
	private String appName;
	private int appVersion;
	private String mainFile;

		public Application()
		{}
		
		public Application(int appNo,String appName,int appVersion)
		{
			this.appNo=appNo;
			this.appName=appName;
			this.appVersion=appVersion;
		}
	
		public Application(int appNo,String appName)
		{
			this.appNo=appNo;
			this.appName=appName;
		}
	
		public Application(int appNo,String appName,int appVersion,String mainFile)
        {
	        this.appNo=appNo;
	        this.appName=appName;
	        this.appVersion=appVersion;
	        this.mainFile=mainFile;
        }

		public int getAppNo()
		{
			return appNo;
		}
	
		public void setAppNo(int appNo)
		{
			this.appNo=appNo;
		}
	
		public String getAppName()
		{
			return appName;
		}
	
		public void setAppName(String appName)
		{
			this.appName=appName;
		}
	
		public int getAppVersion()
		{
			return appVersion;
		}
	
		public void setAppVersion(int appVersion)
		{
			this.appVersion=appVersion;
		}

		public String getMainFile()
        {
        	return mainFile;
        }

		public void setMainFile(String mainFile)
        {
        	this.mainFile=mainFile;
        }
		
		public String toString()
		{
			StringBuilder builder=new StringBuilder(super.toString());
			
			builder.append("[").append("AppNo="+appNo).append(", Name: "+appName).append(", ver="+appVersion).append("]");
			return builder.toString();
		}
}
