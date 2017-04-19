package com.nituv.lbi;

import java.sql.Time;
import java.util.Date;
import java.util.List;

import com.nituv.common.exceptions.LbiInternalException;
import com.nituv.common.exceptions.LbiNotFoundException;
import com.nituv.lbi.dbobjects.LbiRouteInvestigation;
import com.nituv.lbi.dbobjects.LbiTimeAndDaysBitmask;
import com.nituv.mot.siri.client.ILbiStationArrivalsDataResponse;

public interface ILbiLinesBehaviourIntelligenceMgrDal 
{
	public List<ILbiLineQueryParameters> getLinesToQuery() throws LbiInternalException;

	public List<Integer> getStationsCodesOfRouteId(
			int gtfsRouteId
	) throws LbiInternalException, 
		LbiNotFoundException;

	public void insertStationArrivalsDataResponse(
			int routeInvestigationId, 
			List<ILbiStationArrivalsDataResponse> arrivalsList
	) throws LbiInternalException;
	
	public List<LbiRouteInvestigation> getRelevantRouteInvestigations()
			throws LbiInternalException, 
				LbiNotFoundException;

	public void updateRouteInvestigationStatus(
			int routeInvestigationId, 
			int statusId
	) throws LbiInternalException, 
		LbiNotFoundException;

	public int getTripInvestigationCountForTimeFrame(int routeInvestigationId, Date pointInTime) throws LbiInternalException, 
		LbiNotFoundException;

	public int updateRouteInvestigationsFromRunningToNotRunning() throws LbiInternalException;

//	public Integer getLastStationCodeOfRoute(int gtfsRouteIdFk) throws LbiInternalException;

//	public Integer getFirstStationCodeOfRoute(int gtfsRouteIdFk) throws LbiInternalException;

	// returns tripInvestigationId
	public int insertTripInvestigation(
			int routeId, 
			int routeInvestigationId, 
			Date aimedDepartureTime,
			boolean busTransmittedNearFirstStation
	) throws LbiInternalException;

	public int updateTripInvestigationActualDepartureTimeAndStatusToStarted(
			int tripInvestigationId, 
			Date departureTime
	) throws LbiInternalException;

	public int updateTripInvestigationExpectedArrivalTimeAndExpectedSecondsDuration(
			int tripInvestigationId,
			Date expectedArrivalTime, 
			int secondsBetweenExpectedArrivalTimeAndDepartureTime
	) throws LbiInternalException;

	public int updateTripInvestigationActualArrivalTimeAndActualSecondsDurationAndStatusCompleted(
			int tripInvestigationId, 
			Date actualArrivalTime, 
			int actualSecondsDuration
	) throws LbiInternalException;

	public void updateTripInvestigationStatus(int tripInvestigationId, int tripInvestigationStatus) throws LbiInternalException;

	public List<Time> getRequiredTimeFrames(int routeInvestigationId) throws LbiInternalException;

	public List<LbiTimeAndDaysBitmask> getRouteDepartureTimesAndDays(int routeId) throws LbiInternalException;

	public int getGtfsTripDuration(Integer gtfsRouteIdFk, Date departureTime) 
			throws LbiInternalException, LbiNotFoundException;

	public int getGtfsRouteDuration(Integer gtfsRouteIdFk) 
			throws LbiInternalException, LbiNotFoundException;

	public int getSecondStationCodeOfRoute(Integer gtfsRouteIdFk) throws LbiInternalException, LbiNotFoundException;

	public void updateTripInvestigationBusWasDetectedNearDepartureStation(int tripInvestigationId, boolean wasDetected) throws LbiInternalException;
	
}
