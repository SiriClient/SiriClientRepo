package com.nituv.lbi;

import java.util.Date;

import com.nituv.common.exceptions.LbiInternalException;
import com.nituv.mot.siri.client.response.LbiStationArrialsDataResponseTest;

public class LbiTripInvestigationProcessDataTest extends LbiTripInvestigationProcessData {

	void setExpectedArrivalTime(Date expectedArrivalTimeVal) throws LbiInternalException{
		LbiStationArrialsDataResponseTest stationArrivalDataTest = new LbiStationArrialsDataResponseTest();
		stationArrivalDataTest.setExpectedArrivalTime(expectedArrivalTimeVal);
		setStationArrivalData(stationArrivalDataTest);
	}

}
