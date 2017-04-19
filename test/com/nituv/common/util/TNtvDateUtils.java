package com.nituv.common.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Time;
import java.time.LocalTime;
import java.util.Date;

import org.junit.Test;

import com.nituv.lbi.inf.Lg;

public class TNtvDateUtils {

	@Test
	public void testisDaysBitmaskContains_SunToThu() {
		int bitField = 31;
		assertTrue(NtvDateUtils.isDaysBitmaskContains(bitField, 1)); //à
		assertTrue(NtvDateUtils.isDaysBitmaskContains(bitField, 2)); //á
		assertTrue(NtvDateUtils.isDaysBitmaskContains(bitField, 3)); //â
		assertTrue(NtvDateUtils.isDaysBitmaskContains(bitField, 4)); //ã
		assertTrue(NtvDateUtils.isDaysBitmaskContains(bitField, 5));//ä
		assertFalse(NtvDateUtils.isDaysBitmaskContains(bitField, 6));//å 
		assertFalse(NtvDateUtils.isDaysBitmaskContains(bitField, 7));//ù 
	}
	
	@Test
	public void testisDaysBitmaskContains_FriToSat() {
		int bitField = 96;
		assertFalse(NtvDateUtils.isDaysBitmaskContains(bitField, 1)); //à
		assertFalse(NtvDateUtils.isDaysBitmaskContains(bitField, 2)); //á
		assertFalse(NtvDateUtils.isDaysBitmaskContains(bitField, 3)); //â
		assertFalse(NtvDateUtils.isDaysBitmaskContains(bitField, 4)); //ã
		assertFalse(NtvDateUtils.isDaysBitmaskContains(bitField, 5));//ä
		assertTrue(NtvDateUtils.isDaysBitmaskContains(bitField, 6));//å 
		assertTrue(NtvDateUtils.isDaysBitmaskContains(bitField, 7));//ù 
	}//isDaysBitmaskContainsToday

	@Test
	public void testIsDaysBitmaskContainsToday() {
		int bitField = 31;
		assertTrue(NtvDateUtils.isDaysBitmaskContainsToday(bitField));
	}//isDaysBitmaskContainsToday

	@Test
	public void testGetCurrentTime() 
	{
		Time t = NtvDateUtils.getCurrentTime();
		String timeStr = t.toString();
		String formatStr = NtvDateUtils.formatTimeShortString(new Date());
		assertTrue(timeStr.equals(formatStr));
	}
	
	@Test
	public void changeTimeOfDate()
	{
		Date d = null;
		try {
			d = NtvDateUtils.parseUtcDate("2016-03-29 20:00:00.0");
		} catch (Exception e) {
			fail(e.toString());
		}
		LocalTime lt = LocalTime.of(2, 0);
		Time t = new Time(lt.getSecond()*1000); //two hours
		Date result = NtvDateUtils.changeTimeOfDate(d,t);
		String resStr = NtvDateUtils.formatDateUtc(result);
		assertTrue(resStr.equals("2016-03-29 02:00:00"));
	}
	
	//getCurrentTime
	@Test
	public void getCurrentTime()
	{
		try {
			LocalTime lt = LocalTime.now();
			int secSinceMidnight = lt.toSecondOfDay();
			Time t = NtvDateUtils.getCurrentTime(-secSinceMidnight);
			assertTrue(t.toString().equals("00:00:00"));
			t = NtvDateUtils.getCurrentTime(-secSinceMidnight-1);
			assertTrue(t.toString().equals("23:59:59"));
			int secTillMidnight = (24 * 60 * 60) - secSinceMidnight;
			t = NtvDateUtils.getCurrentTime(secTillMidnight);
			assertTrue(t.toString().equals("00:00:00"));
			t = NtvDateUtils.getCurrentTime(secTillMidnight-1);
			assertTrue(t.toString().equals("23:59:59"));
			t = NtvDateUtils.getCurrentTime(secTillMidnight+1);
			assertTrue(t.toString().equals("00:00:01"));
			
		} catch (Exception e) {
			fail(e.toString());
		}
	}
	
	@Test
	public void getSecondsGapFromNow() {
		Date nowLessTwoSec = new Date();
		try {
			Thread.sleep(2000);
		} catch (Exception e) {
			fail();
		}
		int secGap = NtvDateUtils.getSecondsGapFromNow(nowLessTwoSec);
		Lg.lgr.info("secGap:{}", secGap);
		assertTrue(secGap == -2);
	}
}
