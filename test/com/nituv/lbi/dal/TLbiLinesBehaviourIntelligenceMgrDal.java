package com.nituv.lbi.dal;

import static org.junit.Assert.*;
import java.sql.Time;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.nituv.lbi.dbobjects.LbiTimeAndDaysBitmask;
import com.nituv.lbi.inf.Lg;

public class TLbiLinesBehaviourIntelligenceMgrDal {
	LbiLinesBehaviourIntelligenceMgrDal dal = new LbiLinesBehaviourIntelligenceMgrDal();
	@Test
	public void test() {
		
		try {
			List<Time> frames = dal.getRequiredTimeFrames(19);
			assertNotNull(frames);
		} catch (Exception ex) {
			fail(ex.toString());
		}
	}
	
	@Test
	public void testUpdateRouteInvestigationsFromRunningToNotRunning()
	{
		try {
			int count = dal.updateRouteInvestigationsFromRunningToNotRunning();
			assertTrue(count >= 0);
		} catch (Exception ex) {
			fail(ex.toString());
		}
	}
	
	@Test
	public void testGetTripInvestigationCountForTimeFrame()
	{
		try {
			int count = dal.getTripInvestigationCountForTimeFrame(19,new Date());
			assertTrue(count >= 0);
		} catch (Exception ex) {
			fail(ex.toString());
		}
	}
	
	@Test
	public void testInsertTripInvestigation()
	{
		try {
			int createdId = dal.insertTripInvestigation(13428,19,new Date(),true);
//			int createdId = dal.insertTripInvestigation(2184,280,new Date(),true);
			Lg.lgr.info("created trip id:" + createdId);
			assertTrue(createdId > 0);
		} catch (Exception ex) {
			fail(ex.toString());
		}
	}
	
	@Test
	public void testUpdateTripInvestigationActualDepartureTime()
	{
		try {
			int effectedCount = dal.updateTripInvestigationActualDepartureTimeAndStatusToStarted(5,new Date());
			assertTrue(effectedCount == 1);
			Lg.lgr.info("trip inv was update:");
		} catch (Exception ex) {
			fail(ex.toString());
		}
	}
	
	@Test
	public void testUpdateTripInvestigationExpectedArrivalTimeAndExpectedSecondsDuration()
	{
		try {
			int effectedCount = dal.updateTripInvestigationExpectedArrivalTimeAndExpectedSecondsDuration(
					5,new Date(),14124);
			assertTrue(effectedCount == 1);
			Lg.lgr.info("trip inv was update:");
		} catch (Exception ex) {
			fail(ex.toString());
		}
	}
	
	//updateTripInvestigationActualArrivalTimeAndActualSecondsDuration
	@Test
	public void testUpdateTripInvestigationActualArrivalTimeAndActualSecondsDuration()
	{
		try {
			int effectedCount = dal.updateTripInvestigationActualArrivalTimeAndActualSecondsDurationAndStatusCompleted(
					5,new Date(),14500);
			assertTrue(effectedCount == 1);
			Lg.lgr.info("trip inv was update:");
		} catch (Exception ex) {
			fail(ex.toString());
		}
	}
	//updateTripInvestigationAsFailed
	@Test
	public void testUpdateTripInvestigationAsFailed()
	{
		try {
			dal.updateTripInvestigationStatus(5,2);
		} catch (Exception ex) {
			fail(ex.toString());
		}
	}
	
	//getRouteDepartureTimesAndDays
	@Test
	public void testGetRouteDepartureTimesAndDays()
	{
		try {
			List<LbiTimeAndDaysBitmask> list = dal.getRouteDepartureTimesAndDays(13455);
			assertTrue(list.size() > 0);
			Lg.lgr.info("list.size() = " + list.size());
		} catch (Exception ex) {
			fail(ex.toString());
		}
	}
	
	//getGtfsTripDuration
	@Test
	public void getGtfsTripDuration()
	{
		try {
			int secDuration = dal.getGtfsTripDuration(2567,new Date());
			assertTrue(true);
			Lg.lgr.info("secDuration = " + secDuration);
		} catch (Exception ex) {
			fail(ex.toString());
		}
	}
	
	//getGtfsRouteDuration
	@Test
	public void getGtfsRouteDuration()
	{
		try {
			int secDuration = dal.getGtfsRouteDuration(2567);
			assertTrue(true);
			Lg.lgr.info("secDuration = " + secDuration);
		} catch (Exception ex) {
			fail(ex.toString());
		}
	}
	
	//updateTripInvestigationBusWasDetectedNearDepartureStation
	@Test
	public void updateTripInvestigationBusWasDetectedNearDepartureStation()
	{
		try {
			dal.updateTripInvestigationBusWasDetectedNearDepartureStation(19,true);
			assertTrue(true);
			Lg.lgr.info("returned without error");
		} catch (Exception ex) {
			fail(ex.toString());
		}
	}
}
