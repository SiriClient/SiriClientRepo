package com.nituv.lbi;

import java.sql.Time;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.Level;

import com.nituv.common.exceptions.LbiInternalException;
import com.nituv.common.exceptions.LbiNotFoundException;
import com.nituv.common.util.NtvDateUtils;
import com.nituv.lbi.dbobjects.ILbiRouteInvestigation;
import com.nituv.lbi.dbobjects.LbiTimeAndDaysBitmask;
import com.nituv.lbi.inf.Lg;

class LbiRouteInvestigationLogic 
{
	private static ILbiLinesBehaviourIntelligenceMgrDal m_dal = null;
	static void setDal(ILbiLinesBehaviourIntelligenceMgrDal dal) {
		m_dal = dal;
	}

	static boolean isInvestigationNeedsMoreSamplesAtThisTimeFrame(ILbiRouteInvestigation routeInvestigation) throws LbiInternalException {
		return isInvestigationNeedsMoreSamplesAtTimeFrame(routeInvestigation, new Date());
	}

	public static boolean isInvestigationNeedsMoreSamplesAtTimeFrame(
			ILbiRouteInvestigation routeInvestigation,
			Date pointInTime
	) throws LbiInternalException {
		switch (routeInvestigation.getRouteInvestigationType())
		{
		case STATISTICAL_TRIPS_DURATIONS:
		case STATISTICAL_STATIONS_STOPS:
			try {
				int countForTimeFrame = m_dal.getTripInvestigationCountForTimeFrame(routeInvestigation.getRouteInvestigationId(), pointInTime);
				Lg.lgr.debug("tripInvestigationCountForTimeFrame={}, samplesGoalForTimeFrame={}",countForTimeFrame,routeInvestigation.getSamplesGoalForTimeFrame());
				if (countForTimeFrame < routeInvestigation.getSamplesGoalForTimeFrame()) {
					return true;
				}
			} catch (LbiNotFoundException ex) {
				throw Lg.lgr.throwing(Level.ERROR, new LbiInternalException(ex));
			}
			break;
		case ALL_TRIP_DURATIONS:
		case ALL_STATIONS_STOPS:
		case ROUTE_SNAPSHOT: 
			return true;
		default:
			Lg.lgr.warn("no handler for typeid:" + routeInvestigation.getRouteInvestigationTypeIdFk() + ". routeInvestigation:" + routeInvestigation.toString());		
		}
		return false;
	}
/*
 SELECT route_investigation_id, time_frame_id, start_time, end_time, order_number, count(*) AS trips_count 
FROM trip_investigations 
	INNER JOIN route_investigations ON route_investigation_id = route_investigation_id_fk
	INNER JOIN time_frames ON actual_departure_time::time BETWEEN start_time AND end_time
WHERE actual_departure_time IS NOT NULL AND actual_arrival_time IS NOT NULL
	AND time_frame_group_id_fk = 1
GROUP BY route_investigation_id, time_frame_id, start_time, end_time, order_number
HAVING count(*) < (SELECT samples_goal_for_time_frame FROM route_investigations as subq WHERE subq.route_investigation_id = route_investigation_id)
ORDER BY order_number
 */
	/**
	 * this function is called always from a time frame which has enough samples
	 * so the returned time frames can be either before or after current time frame
	 * @param routeInvestigationId
	 * @return nextRequiredTimeFrameStart. 
	 * 		when all time frames are full the function will return null 
	 * @throws LbiInternalException
	 */
	public static Date getNextRequiredTimeFrameStart(int routeInvestigationId) throws LbiInternalException  
	{
		//get the first start time time higher from current time 
			// add it to the current date and return it
		// if nothing found take the first one found (e.g. 00:00 after midnight
			// add it to tomorrow date and return it

		List<Time> timeFrameStartTimeList = m_dal.getRequiredTimeFrames(routeInvestigationId);
		if (timeFrameStartTimeList == null || timeFrameStartTimeList.size() == 0) 
		{
			Lg.lgr.info("all time frames for this route investigation have enough samples. routeInvestigationId=" + routeInvestigationId);
			return null;
		} 
		Time selectedTime = null;
		Time currentTime = NtvDateUtils.getCurrentTime();
		// add 5 minutes so we search for trips that start in 5 minutes or later.
		for (Time time: timeFrameStartTimeList) {
			if (time.after(currentTime)) {
				selectedTime = time;
				break;
			}
		}
		
		Date timeAndDate = new Date();
		if (selectedTime == null) {
			selectedTime = timeFrameStartTimeList.get(0);
			//MUST be after current time (this function was called after the current time frame was full
			// so the first time frame should be at tomorrow
			timeAndDate = DateUtils.addDays(timeAndDate, 1);
		}
		
		return NtvDateUtils.changeTimeOfDate(timeAndDate, selectedTime);
	}

	// is in station or passed station
	public static Date getArrivalTimeIfArrived(LbiTripInvestigationProcessData processData) 
	{
		if (processData.isTripKeyChanged()) {
			return processData.getMeanRecordedAtTime();
		} else if (processData.getStationArrivalData().isVehicleAtStop()){
//			return processData.getStationArrivalData().getRecordedAtTime();
			return processData.getLastSiriResponseTime();
		}
		return null; // not arrived
	}

	// (expected arrival time of bus / 2.5) but at least 30 seconds.
	public static int getSecondsToWaitForVehicleArrival(LbiTripInvestigationProcessData processData) {
		Date expectedArrival =  processData.getExpectedArrivalTime();
		float secondsGap = NtvDateUtils.getSecondsGapFromNow(expectedArrival);
		int secondsToWait = Math.round(secondsGap / 2.5f);
		if (secondsToWait < 30) 
			secondsToWait = 30;
		Lg.lgr.debug("waiting for bus to arrive. expectedArrival:{}, secondsGapFromNow:{}, secsToWait(/2.5):{}, processData:{}",
				NtvDateUtils.formatTimeShortString(expectedArrival),
				secondsGap,
				secondsToWait,
				processData
		);
		return secondsToWait;
	}
/*
SELECT departure_time AS "time", days_bitmask 
FROM gtfs_trips 
	INNER JOIN gtfs_calendar ON gtfs_trips.service_id = gtfs_calendar.service_id
WHERE trip_date <= CURRENT_DATE
	AND CURRENT_DATE BETWEEN start_date AND end_date
	AND route_id = 5499
ORDER BY departure_time 
 */
	public static Date getNextTripTimeByGtfsData(int routeId) throws LbiInternalException
	{
		List<LbiTimeAndDaysBitmask> timeAndDaysList =  m_dal.getRouteDepartureTimesAndDays(routeId);
		
		if (timeAndDaysList == null || timeAndDaysList.size() == 0) 
		{
			Lg.lgr.warn("no departure times where found in GTFS for this routeId:" + routeId);
			return null;
		}
			
		LbiTimeAndDaysBitmask selectedTime = null;
		// add 5 minutes so we search for trips that start in 5 minutes or later.
		Time currentTime = NtvDateUtils.getCurrentTime(5*60);
		int currentDayOfWeek = NtvDateUtils.getDayOfWeek();
		// search for today // after current time
		for (LbiTimeAndDaysBitmask timeAndDays: timeAndDaysList) {
			if (timeAndDays.getTime().after(currentTime)) 
			{
				if (NtvDateUtils.isDaysBitmaskContainsToday(timeAndDays.getDaysBitmask())) {
					selectedTime = timeAndDays;
					break;
				}
			}
		}
		if (selectedTime != null) {
			// return selected time with current date 
			return NtvDateUtils.changeTimeOfDate(new Date(), selectedTime.getTime());
		}
		
		//search for other days
		int searchedDay; 
		// daysAfterToday=1 (and not 0) because we want to start after today (see line: searchedDay = daysAfterToday + currentDayOfWeek)
		// i<=6 (and not 7) because we don't want to check for today
		for (int daysAfterToday = 1; daysAfterToday<=6;daysAfterToday++) 
		{
			searchedDay = daysAfterToday + currentDayOfWeek;
			if (searchedDay > 7) {
				searchedDay = searchedDay - 7;
			}
			for (LbiTimeAndDaysBitmask timeAndDays: timeAndDaysList) {
				if (NtvDateUtils.isDaysBitmaskContains(timeAndDays.getDaysBitmask(),searchedDay)) {
					// return selected time with current date
					Date futureDay = DateUtils.addDays(new Date(), daysAfterToday);
					return NtvDateUtils.changeTimeOfDate(futureDay, timeAndDays.getTime());
				}
			}
		}
		
		return null;
	}
	
}
