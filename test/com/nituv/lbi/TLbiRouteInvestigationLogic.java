package com.nituv.lbi;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;

import com.nituv.common.util.NtvDateUtils;
import com.nituv.lbi.dal.LbiLinesBehaviourIntelligenceMgrDal;
import com.nituv.lbi.inf.Lg;

public class TLbiRouteInvestigationLogic {
	LbiRouteInvestigationLogic logic;
	public TLbiRouteInvestigationLogic()
	{
		LbiRouteInvestigationLogic.setDal(new LbiLinesBehaviourIntelligenceMgrDal());
	}
	//LbiRouteInvestigationLogic

	@Test
	public void test_getNextRequiredTimeFrameStart() {
		
		try {
			Date result = LbiRouteInvestigationLogic.getNextRequiredTimeFrameStart(19);
			Lg.lgr.info("res:" + result);
			assertFalse(NtvDateUtils.isInThePast(result));
		} catch (Exception ex) {
			fail(ex.toString());
		}
	}
	
	
	@Test
	public void getSecondsToWaitForVehicleArrival() {
		try {
			LbiTripInvestigationProcessDataTest processData = 
					new LbiTripInvestigationProcessDataTest();
			int secTillExpected = 5*60;
			processData.setExpectedArrivalTime(DateUtils.addSeconds(new Date(), secTillExpected));
			int secToWaitExp = (int) (secTillExpected / 2.5);
			int secToWaitRes = LbiRouteInvestigationLogic.getSecondsToWaitForVehicleArrival(processData);
			Lg.lgr.info("secToWaitExp:{}, secToWaitRes:{}", secToWaitExp,secToWaitRes);
			assertTrue(secToWaitRes == secToWaitExp);
			
			secTillExpected = 5;
			processData.setExpectedArrivalTime(DateUtils.addSeconds(new Date(), secTillExpected));
			secToWaitExp = 30;
			secToWaitRes = LbiRouteInvestigationLogic.getSecondsToWaitForVehicleArrival(processData);
			Lg.lgr.info("secToWaitExp:{}, secToWaitRes:{}", secToWaitExp,secToWaitRes);
			assertTrue(secToWaitRes == secToWaitExp);

		} catch (Exception ex) {
			fail(ex.toString());
		}
	}
	
	//getNextTripTimeByGtfsData
	@Test
	public void test_getNextTripTimeByGtfsData() {
		
		try {
			Date result = LbiRouteInvestigationLogic.getNextTripTimeByGtfsData(2517);
			Lg.lgr.info("res:" + result);
			assertFalse(NtvDateUtils.isInThePast(result));
		} catch (Exception ex) {
			fail(ex.toString());
		}
	}
}
