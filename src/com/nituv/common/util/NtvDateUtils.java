package com.nituv.common.util;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class NtvDateUtils {
	private static SimpleDateFormat m_simpleDateFormatForSoapIsoDate = null;
	private static SimpleDateFormat m_simpleDateFormatForParsingSoapIsoDate = null;
	private static SimpleDateFormat m_simpleDateFormatForShortTime = new SimpleDateFormat("HH:mm:ss");
	private static SimpleDateFormat m_simpleDateFormatForParsingIsoDate = null;
	
	public static String formatSoapDateOfNow()
	{
		if (m_simpleDateFormatForSoapIsoDate == null) {
			TimeZone tz = TimeZone.getTimeZone("Israel");
			m_simpleDateFormatForSoapIsoDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SX");
			m_simpleDateFormatForSoapIsoDate.setTimeZone(tz);
		}
		return m_simpleDateFormatForSoapIsoDate.format(new Date()) + ":00";
	}

	public static Date parseDateOutOfSoapString(String dateStr) throws ParseException
	{
		m_simpleDateFormatForParsingSoapIsoDate = null;
		if (m_simpleDateFormatForParsingSoapIsoDate == null) {
			TimeZone tz = TimeZone.getTimeZone("Israel");
			m_simpleDateFormatForParsingSoapIsoDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S");
			m_simpleDateFormatForParsingSoapIsoDate.setTimeZone(tz);
		}
//		return m_simpleDateFormatForIsoDateOfNow.parse(dateStr.substring(0, 19));
		return m_simpleDateFormatForParsingSoapIsoDate.parse(dateStr);
	}
	
	public static String formatTimeShortString(Date date) {
		if (date == null) {
			return null;
		}
		return m_simpleDateFormatForShortTime.format(date);
	}
	
	public static String formatDateUtc(Date date) {
		return toUtcDateTime(date);
	}

	public static String toUtcDateTime(Date date) {
		if (date == null) {
			return null;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(date);
	}

	public static String toUtcDate(Date date) {
		if (date == null) {
			return null;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(date);
	}

	public static Date getMeanDate(Date date, Date date2) {
		if (date == null || date2 == null) {
			return null;
		}
		return new Date((date.getTime() / 2) + (date2.getTime() / 2));
	}

	public static int getSecondsGap(Date priorDate, Date laterDate)
	{
		return (int) (laterDate.getTime() - priorDate.getTime()) / 1000; 
	}

	public static int getSecondsGapFromNow(Date laterFromNow) {
		if (laterFromNow == null) {
			return -1;
		}
		return (int) (laterFromNow.getTime() - System.currentTimeMillis()) / 1000; 
	}

	// 1=Sunday etc..
	public static int getDayOfWeek(Date currentTime) {
		Calendar calendar = new GregorianCalendar();// Calendar.getInstance();
		calendar.setTime(currentTime);
		return calendar.get(Calendar.DAY_OF_WEEK);
	}
	
	public static int getDayOfWeek() {
		Calendar calendar = new GregorianCalendar();// Calendar.getInstance();
		return calendar.get(Calendar.DAY_OF_WEEK);
	}

	public static boolean isDaysBitmaskContainsToday(int daysBitmask) {
		
		int currentDay = new GregorianCalendar().get(Calendar.DAY_OF_WEEK);
		return isDaysBitmaskContains(daysBitmask, currentDay);
	}

	/**
	 * 
	 * @param daysBitmask
	 * @param searchedDay sun=1 ... sat=7
	 * @return boolean
	 */
	public static boolean isDaysBitmaskContains(int daysBitmask, int searchedDay) 
	{
		//bitOfDay should result with 1,2,4,8,16,32,64
		int bitOfDay = 0;
		if (searchedDay == 1 ) {
			bitOfDay = 1;
		} else {
			bitOfDay = (int) Math.pow(2d,(double)(searchedDay-1));
		}
		return (daysBitmask & bitOfDay) > 0;
	}

	public static Date changeTimeOfDate(Date date, Time time) 
	{
		LocalTime localTime = time.toLocalTime();// LocalTime.ofNanoOfDay(time.getTime() * 1000000); // LocalTime = 14:42:43.062
		LocalDate localDate = NtvDateConvertUtils.asLocalDate(date);
		LocalDateTime localDateTime = localDate.atTime(localTime);
		return NtvDateConvertUtils.asUtilDate(localDateTime);
	}

	public static boolean isInThePast(Date nextRequiredTimeFrameStart) {
		if (nextRequiredTimeFrameStart == null) {
			return false;
		}
		return nextRequiredTimeFrameStart.getTime() < System.currentTimeMillis();
	}

	public static Time getCurrentTime() {
		return getCurrentTime(0);
	}

	public static Time getCurrentTime(int secondsOffset) {
		LocalTime now = LocalTime.now(ZoneId.systemDefault()); // LocalTime = 14:42:43.062
		if (secondsOffset != 0) {
			now = now.plusSeconds(secondsOffset);
		}
		return Time.valueOf(now);
	}

	public static Date parseUtcDate(String string) throws ParseException {
		m_simpleDateFormatForParsingIsoDate = null;
		if (m_simpleDateFormatForParsingIsoDate == null) {
			TimeZone tz = TimeZone.getTimeZone("Israel");
			m_simpleDateFormatForParsingIsoDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
			m_simpleDateFormatForParsingIsoDate.setTimeZone(tz);
		}
//		return m_simpleDateFormatForIsoDateOfNow.parse(dateStr.substring(0, 19));
		return m_simpleDateFormatForParsingIsoDate.parse(string);
	}

}
