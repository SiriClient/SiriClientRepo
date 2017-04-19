package com.nituv.mot.siri.client.response;

import java.util.Date;

import com.nituv.mot.siri.client.ILbiStationArrivalsDataResponse;

public class LbiStationArrialsDataResponseTest implements ILbiStationArrivalsDataResponse
{

	private Date expectedArrivalTime;

	@Override
	public boolean isMessageValid() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Date getRecordedAtTime() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getMonitoringRef() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasMonitoredStopVisitData() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Integer getLineRef() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getDirectionRef() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPublishedLineName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getOperatorRef() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getDestinationRef() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date getOriginAimedDepartureTime() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double getVehicleLongitude() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double getVehicleLatitude() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getStopPointRef() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date getExpectedArrivalTime() {
		// TODO Auto-generated method stub
		return expectedArrivalTime;
	}

	public void setExpectedArrivalTime(Date expectedArrivalTimeVal) {
		expectedArrivalTime = expectedArrivalTimeVal;
	}

	@Override
	public boolean isVehicleAtStop() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Date getAimedArrivalTime() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date getActualArrivalTime() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getArrivalStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean getIsExpectedBusOnRoute() {
		// TODO Auto-generated method stub
		return false;
	}
	
		
}
