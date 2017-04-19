package com.nituv.mot.siri.client;

import java.util.Date;

public interface ILbiStationArrivalsDataResponse {
	boolean isMessageValid(); 

	public Date getRecordedAtTime(); 
	public Integer getMonitoringRef(); 
	boolean hasMonitoredStopVisitData();
	
	//based on MonitoredVehicleJourney
	public Integer getLineRef();
	public Integer getDirectionRef();
	public String getPublishedLineName();
	public Integer getOperatorRef();
	public Integer getDestinationRef();
	public Date getOriginAimedDepartureTime();

	// based on VehicleLocation
	public Double getVehicleLongitude();
	public Double getVehicleLatitude();
	public Integer getStopPointRef(); 
	
	// based on MonitoredCall 
	public Date getExpectedArrivalTime(); 
	public boolean isVehicleAtStop(); 
	public Date getAimedArrivalTime(); // we get this when bus not transmitting 
	public Date getActualArrivalTime(); 
	public String getArrivalStatus();

	public boolean getIsExpectedBusOnRoute();


}
