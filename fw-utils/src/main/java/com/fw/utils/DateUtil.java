package com.fw.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateUtil
{
	public static final long MILL_SEC_PER_DAY=24*60*60*1000L;
	public static final long MILL_SEC_PER_HOUR=60*60*1000L;
	public static final long MILL_SEC_PER_MIN=60*1000L;
	public static final long MILL_SEC_PER_SEC=1000;
	
	private static final SimpleDateFormat DEF_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
	private static final SimpleDateFormat DEF_DATE_TIME_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	private static final SimpleDateFormat DEF_TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
	
	private static final Date TODAY = new Date();

		public static Date newDate(int date, int month, int year, int hour24, int min, int sec, int milli)
		{
			GregorianCalendar cal = new GregorianCalendar();
			cal.set(Calendar.DATE, date);
			cal.set(Calendar.MONTH, month);
			cal.set(Calendar.YEAR, year);
			
			cal.set(Calendar.HOUR_OF_DAY, hour24);
			cal.set(Calendar.MINUTE, min);
			cal.set(Calendar.SECOND, sec);
			
			cal.set(Calendar.MILLISECOND, milli);
			
			return cal.getTime();
		}
	
		public static Date newDate(int date, int month, int year)
		{
			return newDate(date, month, year, 0, 0, 0, 0);
		}
		
		public static Date addToDate(Date date, int days, int months, int years, int hours, int min, int sec)
		{
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTime(date);
			
			cal.add(Calendar.DATE, days);
			cal.add(Calendar.MONTH, months);
			cal.add(Calendar.YEAR, years);
			
			cal.add(Calendar.HOUR_OF_DAY, hours);
			cal.add(Calendar.MINUTE, min);
			cal.add(Calendar.SECOND, sec);
			
			return cal.getTime();
		}
	
		public static Date addToDate(int days, int months, int years, int hours, int min, int sec)
		{
			return addToDate(new Date(), days, months, years, hours, min, sec);
		}
		
		public static Date addToDate(int days, int hours, int min, int sec)
		{
			return addToDate(new Date(), days, 0, 0, hours, min, sec);
		}
		
		public static Date trimTime(Date date)
		{
				if(date==null)
					return null;
			GregorianCalendar cal=new GregorianCalendar();
			cal.setTime(date);
			
			cal.set(Calendar.MILLISECOND,0);
			cal.set(Calendar.SECOND,0);
			cal.set(Calendar.MINUTE,0);
			cal.set(Calendar.HOUR_OF_DAY,0);
			
			return cal.getTime();
		}
		
		public static Date trimDate(Date date)
		{
				if(date==null)
					return null;
			
			GregorianCalendar today = new GregorianCalendar();
			today.setTime(new Date());
				
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTime(date);
			
			cal.set(Calendar.DATE, today.get(Calendar.DATE));
			cal.set(Calendar.MONTH, today.get(Calendar.MONTH));
			cal.set(Calendar.YEAR, today.get(Calendar.YEAR));
			
			return cal.getTime();
		}
		
		public static Date ceilTime(Date date)
		{
				if(date==null)
					return null;
			GregorianCalendar cal=new GregorianCalendar();
			cal.setTime(date);
			
			cal.set(Calendar.MILLISECOND,0);
			cal.set(Calendar.SECOND,0);
			cal.set(Calendar.MINUTE,59);
			cal.set(Calendar.HOUR_OF_DAY,23);
			
			return cal.getTime();
		}
		
		public static Date addToToday(int days)
		{
			return addToToday(days,false);
		}
		
		public static Date addToToday(int days,boolean trimTime)
		{
			GregorianCalendar cal=new GregorianCalendar();
			cal.setTime(new Date());
			
			cal.add(Calendar.DATE,days);
			
				if(trimTime)
				{
					cal.set(Calendar.MILLISECOND,0);
					cal.set(Calendar.SECOND,0);
					cal.set(Calendar.MINUTE,0);
					cal.set(Calendar.HOUR_OF_DAY,0);
				}
			
			return cal.getTime();
			
		}

		public static int dateDiff(Date frmDate,Date toDate)
		{
			return dateDiff(frmDate,toDate,Calendar.DATE);
		}
		
		public static int dateDiff(Date frmDate,Date toDate,int field)
		{
			long frm=frmDate.getTime();
			long to=toDate.getTime();
			long diff=(to-frm);
			
				switch(field)
				{
					case Calendar.DATE:
						return (int)(diff/MILL_SEC_PER_DAY);
					case Calendar.HOUR:
						return (int)(diff/MILL_SEC_PER_HOUR);
					case Calendar.MINUTE:
						return (int)(diff/MILL_SEC_PER_MIN);
					case Calendar.SECOND:
						return (int)(diff/MILL_SEC_PER_SEC);
				}
				
			throw new IllegalArgumentException("Illegal field type is specified(Supported Fields: DATE, HOUR or MINUTE): "+field);
		}
		
		public static boolean isAfter(Date refTime)
		{
			return isAfter(refTime, new Date());
		}
		
		public static boolean isBefore(Date refTime)
		{
			return !isAfter(refTime, new Date());
		}
		
		public static boolean isBefore(Date refTime, Date time)
		{
			return !isAfter(refTime, time);
		}
		
		public static boolean isAfter(Date refTime, Date time)
		{
			long ref = refTime.getTime();
			long timeInMilli = time.getTime();
			long diff = (timeInMilli - ref);
			
			return (diff > 0);
		}
		
		public static boolean isToday(Date date)
		{
			return isSameDates(date, new Date());
		}
		
		public static boolean isSameDates(Date date1, Date date2)
		{
				if(date1 == null || date2 == null)
				{
					return false;
				}
				
			GregorianCalendar calendar1 = new GregorianCalendar();
			calendar1.setTime(date1);
			
			GregorianCalendar calendar2 = new GregorianCalendar();
			calendar2.setTime(date2);
			
			return (calendar1.get(Calendar.DATE) == calendar2.get(Calendar.DATE) &&
					calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH) &&
					calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR));
		}
		
		public static boolean isSameTime(Date date1, Date date2, boolean checkDate, boolean checkHour, boolean checkMin, boolean checkSec)
		{
			GregorianCalendar calendar1 = new GregorianCalendar();
			calendar1.setTime(date1);
			
			GregorianCalendar calendar2 = new GregorianCalendar();
			calendar2.setTime(date2);
			
				if(checkDate && (calendar1.get(Calendar.DATE) != calendar2.get(Calendar.DATE) ||
					calendar1.get(Calendar.MONTH) != calendar2.get(Calendar.MONTH) ||
					calendar1.get(Calendar.YEAR) != calendar2.get(Calendar.YEAR)))
				{
					return false;
				}
				
				if(checkHour && (calendar1.get(Calendar.HOUR) != calendar2.get(Calendar.HOUR)))
				{
					return false;
				}
				
				if(checkMin && (calendar1.get(Calendar.MINUTE) != calendar2.get(Calendar.MINUTE)))
				{
					return false;
				}

				if(checkSec && (calendar1.get(Calendar.SECOND) != calendar2.get(Calendar.SECOND)))
				{
					return false;
				}
				
			return true;
		}
		
		public static Date getDate(int afterDays, int afterHours, int afterMins)
		{
			GregorianCalendar calendar = new GregorianCalendar();
			calendar.setTime(new Date());
			
			calendar.add(Calendar.DATE, afterDays);
			calendar.add(Calendar.HOUR_OF_DAY, afterHours);
			calendar.add(Calendar.MINUTE, afterMins);
			
			return calendar.getTime();
		}
		
		public static String dateString()
		{
			return toString(TODAY, DEF_DATE_FORMAT);
		}
		
		public static String dateTimeString()
		{
			return toString(null, DEF_DATE_TIME_FORMAT);
		}
		
		public static String timeString()
		{
			return toString(null, DEF_TIME_FORMAT);
		}
		
		public static String dateString(Date date)
		{
			return toString(date, DEF_DATE_FORMAT);
		}
		
		public static String dateTimeString(Date date)
		{
			return toString(date, DEF_DATE_TIME_FORMAT);
		}
		
		public static String timeString(Date date)
		{
			return toString(date, DEF_TIME_FORMAT);
		}
		
		public static String toString(Date date, DateFormat dateFormat)
		{
				if(date == null)
				{
					date = new Date();
				}
				
			return dateFormat.format(date);
		}
}
