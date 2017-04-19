package com.nituv.lbi;

import java.util.Date;

import org.apache.logging.log4j.Level;

import com.nituv.common.exceptions.LbiInternalException;
import com.nituv.common.util.NtvDateUtils;
import com.nituv.lbi.dbobjects.LbiRouteInvestigation;
import com.nituv.lbi.inf.Lg;
import com.nituv.mot.siri.client.ILbiStationArrivalsDataResponse;

class LbiTripInvestigationProcessData {
	private LbiRouteInvestigation routeInvestigation;
	private int stationIndex = -1;
	private int stationCheckTryNumber = -1;
	private Date expectedDepartureTime = null;
	private Integer tripInvestigationId = null;
	private ILbiStationArrivalsDataResponse stationArrivalData;
//	private ILbiStationArrivalsDataResponse previourStationArrivalData;
	private boolean tripKeyChangedNow = false;
	private int networkFailureCount = 0;
	private Date actualDepartureTime;
	private boolean betweenThreads = false;
	private Date initTime;
	private Date investigationStartTime;
	private Date lastSiriResponseTime;
	private Date previuosSiriResponseTime;
	private boolean tripKeyDetectedOnLastStation = false;
	private boolean tripKeyDetectedOnLastStationNow = false;
	private boolean timeFrameCheckedIfFull = false;
	private boolean busTransmittedNearFirstStation = false;
	private boolean busTransmittedOnLastStation = false;
	private int noTransmittionCountOnLastStation = 0; 
	
	Date getExpectedDepartureTime() {
		return expectedDepartureTime;
	}

	void setExpectedDepartureTime(Date aimedDepartureTime) {
		this.expectedDepartureTime = aimedDepartureTime;
	}

	int getInvestigatedStationCode() throws LbiInternalException 
	{
		int investigatedStationCode;
		if (getRouteInvestigation().getRouteInvestigationStopcodes() == null
				|| getRouteInvestigation().getRouteInvestigationStopcodes().length == 0
		) 
		{ 
			throw Lg.lgr.throwing(Level.ERROR, new LbiInternalException("stop codes are null or empty. RouteInvestigationId:" + getRouteInvestigation().getRouteInvestigationId()));
		} else {
			if (getRouteInvestigation().getRouteInvestigationStopcodes().length < stationIndex) 
			{
				Lg.lgr.error("shouldn't be call no more stops to check. routeInvestigationId:{}, stopcodes[].length:{} < stationIndex:{}",  getRouteInvestigation().getRouteInvestigationId(), getRouteInvestigation().getRouteInvestigationStopcodes().length, stationIndex);
				throw new LbiInternalException();
			}
			investigatedStationCode = getRouteInvestigation().getRouteInvestigationStopcodes()[stationIndex-1];
		}
		return investigatedStationCode;
	}

	void setStationArrivalData(ILbiStationArrivalsDataResponse data) {
//		previourStationArrivalData = stationArrivalData;
		stationArrivalData = data;
	}
	
	Date getMeanBetweenNowAndPreviousRecordedAtTime() 
	{
		if (previuosSiriResponseTime != null) {
			return NtvDateUtils.getMeanDate(previuosSiriResponseTime, getLastSiriResponseTime());
		} else {
			Lg.lgr.warn("shouldn't be called in this case. processData:{}", this);
			return getLastSiriResponseTime();
		}
//		return NtvDateUtils.getMeanDate(previourStationArrivalData.getRecordedAtTime(), new Date());
	}

	Date getMeanRecordedAtTime() {
		//TODO - try to improve this
		// e.g. take the gap between current between the record time and reduce it from now
		// e.g. use record time only if it's not older then 2 minutes from now (decide on first use)
		return getMeanBetweenNowAndPreviousRecordedAtTime();
		/*if (getStationArrivalData() == null) {
			if (previourStationArrivalData == null) {
				return new Date();
			} else {
				return getMeanBetweenNowAndPreviousRecordedAtTime();
			}
		} else {
			if (previourStationArrivalData == null) {
				return getStationArrivalData().getRecordedAtTime();
			} else {
				return NtvDateUtils.getMeanDate(previourStationArrivalData.getRecordedAtTime(), previourStationArrivalData.getRecordedAtTime());
			}
		}*/
	}

	public boolean isFirstStationBeforeTripKeySet()
	{
		return (stationIndex == 1 && getTripKey() == null);
	}

	public int advanceToLastStation() {
		stationIndex = 2;
		stationCheckTryNumber = 0;
		return stationIndex;
	}

	public int getSecondsBetweenExpectedArrivalTimeAndDepartureTime() {
		if (expectedDepartureTime == null || getStationArrivalData().getExpectedArrivalTime() == null) {
			Lg.lgr.error("aimedDepartureTime == null || stationArrivalData.getExpectedArrivalTime() == null");
			return -1;
		}
		return NtvDateUtils.getSecondsGap(expectedDepartureTime, getStationArrivalData().getExpectedArrivalTime());
	}

	public void markIfTripKeyChanged() throws LbiInternalException 
	{
		tripKeyDetectedOnLastStationNow = false;
		tripKeyChangedNow = false;
		if (getTripKey() == null) {
			return;
//			Lg.lgr.error("getTripKey() == null. shoudln't happen!!! this is a bug. need to fix this. processData:{}", this);
//			throw new LbiInternalException();
		}
		
		if (getStationArrivalData() == null) {
			tripKeyChangedNow = true; ///TODO set this only when changed and not every time "getStationArrivalData() == null" 
			return;
		}
		
		if (getStationArrivalData().getOriginAimedDepartureTime() == null) {
			throw Lg.lgr.throwing(Level.ERROR, new LbiInternalException("stationArrivalData.getOriginAimedDepartureTime() == null."));
		}
		
		if (!getExpectedDepartureTime().equals(getStationArrivalData().getOriginAimedDepartureTime())) 
		{
			// bus has changed - we getting now the next bus
			tripKeyChangedNow = true;
			Lg.lgr.debug("key was changed now previous:{}, new:{}. processData:{}",
					NtvDateUtils.formatTimeShortString(getExpectedDepartureTime()),
					NtvDateUtils.formatTimeShortString(getStationArrivalData().getOriginAimedDepartureTime()),
					this
			);
		} else {
			if (isLastStation()) 
			{
				if (tripKeyDetectedOnLastStation == false){
					tripKeyDetectedOnLastStation = true;
					tripKeyDetectedOnLastStationNow = true;
				}
			}
//			tripKeyChangedNow = false;
		}
	}

	public void setTripKey() {
		if (isFirstStationBeforeTripKeySet()) { // first station if first check try 
			// mark the expected departure time - this is our trip key
			if (getStationArrivalData() != null) {
				setExpectedDepartureTime(getStationArrivalData().getOriginAimedDepartureTime());
			}
		}
	}

	public Date getTripKey() {
		return getExpectedDepartureTime();
	}

	public boolean isTripKeyChanged() {
		return tripKeyChangedNow;
	}

	public boolean hasNewStationArrivalDataAfterTripKeyChange()
	{
		if (!isTripKeyChanged()) {
			Lg.lgr.error("shouldn't be called if key was not changed");
			return false;
		}
		if (getStationArrivalData() == null) {
			return false;
		} else {
			return true;
		}
		
	}

	boolean isFirstStation() {
		return stationIndex == 1;
	}

	boolean isLastStation() {
		return stationIndex == 2;
	}

	boolean isStationFirstTry() {
		return stationCheckTryNumber == 1;
	}

	public boolean isVehicleAtStop() {
		if (getStationArrivalData() == null) 
			return false;
		return getStationArrivalData().isVehicleAtStop();
	}

	public void markNetworkFailure() {
		networkFailureCount++;
	}

	public boolean isBeforeTripInvestigationStart() {
		if (stationIndex <= 1 && stationCheckTryNumber <= 0)
			return true;
		else 
			return false;
	}

	public void resetOnStart(LbiRouteInvestigation routeInvestigationParam) {
		stationIndex = 1;
		stationCheckTryNumber = 0;
		setInitTime(new Date());
		setRouteInvestigation(routeInvestigationParam);
	}

	public int getStationIndex() {
		return stationIndex;
	}

	public void markStationCheckTry() {
		stationCheckTryNumber++;
	}

	public int getNetworkFailureCount() {
		return networkFailureCount;
	}

	public Integer getTripInvestigationId() {
		return tripInvestigationId;
	}

	public void setTripInvestigationId(Integer tripInvestigationId) {
		this.tripInvestigationId = tripInvestigationId;
	}

	public Date getExpectedArrivalTime() {
		if (getStationArrivalData() == null) {
			Lg.lgr.error("stationArrivalData == null");
			return null;
		}
		return getStationArrivalData().getExpectedArrivalTime();
	}

	public void setActualDepartureTime(Date departureTime) {
		actualDepartureTime = departureTime;
	}

	public LbiRouteInvestigation getRouteInvestigation() {
		return routeInvestigation;
	}

	public void setRouteInvestigation(LbiRouteInvestigation routeInvestigation) {
		this.routeInvestigation = routeInvestigation;
	}

	public ILbiStationArrivalsDataResponse getStationArrivalData() {
		return stationArrivalData;
	}
	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		if (routeInvestigation != null) {
			buf.append("routeId:"); buf.append(routeInvestigation.getGtfsRouteIdFk());
			buf.append(", routeInvstId:"); buf.append(routeInvestigation.getRouteInvestigationId());
		}
		if (expectedDepartureTime != null) {
			buf.append(", aimedDepTm:"); buf.append(NtvDateUtils.formatTimeShortString(expectedDepartureTime));
		}
		if (actualDepartureTime != null) {
			buf.append(", actualDepTm:"); buf.append(NtvDateUtils.formatTimeShortString(actualDepartureTime));
		}
		buf.append(", stnIdx:"); buf.append(stationIndex);
		buf.append(", try#:"); buf.append(stationCheckTryNumber);
		if (tripInvestigationId != null) {
			buf.append(", trpInvstId:"); buf.append(tripInvestigationId);
		}
		if (networkFailureCount > 0) {
			buf.append(", networkFailureCount:"); buf.append(networkFailureCount);
		}
		if (tripKeyChangedNow) {
			buf.append(", tripKeyChangedNow:true");
		}
		if (getInitTime() != null) {
			buf.append(", initTm:"); buf.append(NtvDateUtils.formatDateUtc(getInitTime()));
		}
		if (getInvestigationStartTime() != null) {
			buf.append(", invstStrtTm:"); buf.append(NtvDateUtils.formatDateUtc(getInvestigationStartTime()));
		}
		if (getStationArrivalData() != null) {
			buf.append(", recTm:"); buf.append(NtvDateUtils.formatTimeShortString(getStationArrivalData().getRecordedAtTime()));
		}
		if (isBusTransmitting()) {
			buf.append(", lat,lon:"); buf.append(getStationArrivalData().getVehicleLongitude()); buf.append(","); buf.append(getStationArrivalData().getVehicleLatitude());
			if (getStationArrivalData().isVehicleAtStop()) {
				buf.append(", atStp:true");
			}
		}
		if (getNoTransmittionCountOnLastStation() >= 1) {
			buf.append(", noTrnsCntLstStn:"); buf.append(getNoTransmittionCountOnLastStation());
		}
		return buf.toString();
	}
	
	public boolean equals(LbiTripInvestigationProcessData other) {
		if (other == null || other.getInitTime() == null || getInitTime() == null) {
			return false;
		}
		 
		return getInitTime().equals(other.getInitTime());
	}

	public boolean isBetweenThreads() {
		return betweenThreads;
	}

	public void setBetweenThreads(boolean betweenThreads) {
		this.betweenThreads = betweenThreads;
	}

	public Date getInvestigationStartTime() {
		return investigationStartTime;
	}

	public void setInvestigationStartTime(Date investigationStartTime) {
		this.investigationStartTime = investigationStartTime;
	}

	public Date getInitTime() {
		return initTime;
	}

	public void setInitTime(Date initTime) {
		this.initTime = initTime;
	}

	public Date getActualDepartureTime() {
		return actualDepartureTime;
	}

	public Date getLastSiriResponseTime() {
		return lastSiriResponseTime;
	}

	public void setLastSiriResponseTime(Date lastSiriResponseTime) {
		previuosSiriResponseTime = this.lastSiriResponseTime;
		this.lastSiriResponseTime = lastSiriResponseTime;
	}

	public boolean wasTripKeyDetectedOnLastStation() 
	{
		return tripKeyDetectedOnLastStation ;
	}

	public boolean isBusTransmittedNearFirstStation() {
		return busTransmittedNearFirstStation;
	}

	public boolean isBusTransmitting() {
		if (getStationArrivalData() == null) {
			return false;
		}
		if (getStationArrivalData().getVehicleLatitude() == null) {
			return false;
		}
		if (getStationArrivalData().getVehicleLongitude() == null) {
			Lg.lgr.error("lat!=null,lon==null");
			return false;
		}
		return true;
	}

	public void setBusTransmittedNearFirstStation(boolean busTransmittedNearFirstStation) {
		this.busTransmittedNearFirstStation = busTransmittedNearFirstStation;
	}

	public boolean isTripKeyDetectedOnLastStationNow() {
		return tripKeyDetectedOnLastStationNow;
	}

	public boolean isTimeFrameCheckedIfFull() {
		return timeFrameCheckedIfFull ;
	}

	public void setTimeFrameCheckedIfFull(boolean wasChecked) {
		timeFrameCheckedIfFull = wasChecked;
	}

	public boolean isBusTransmittedOnLastStation() {
		return busTransmittedOnLastStation;
	}

	public void setBusTransmittedOnLastStation(boolean busTransmittedOnLastStation) {
		this.busTransmittedOnLastStation = busTransmittedOnLastStation;
	}

	public int getNoTransmittionCountOnLastStation() {
		return noTransmittionCountOnLastStation;
	}

	public void incrementNoTransmittionCountOnLastStation() {
		noTransmittionCountOnLastStation++;
	}
}
