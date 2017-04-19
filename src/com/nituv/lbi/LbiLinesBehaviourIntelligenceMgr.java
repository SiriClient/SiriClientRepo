package com.nituv.lbi;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.DateUtils;

import com.nituv.common.exceptions.LbiAbortProcessing;
import com.nituv.common.exceptions.LbiAllReadyExecuting;
import com.nituv.common.exceptions.LbiInternalException;
import com.nituv.common.exceptions.LbiNotFoundException;
import com.nituv.common.exceptions.LbiParameterException;
import com.nituv.common.util.NtvArraysUtils;
import com.nituv.common.util.NtvDateUtils;
import com.nituv.lbi.dbobjects.LbiRouteInvestigation;
import com.nituv.lbi.inf.Lg;
import com.nituv.mot.siri.client.ILbiSiriWebServiceClient;
import com.nituv.mot.siri.client.ILbiStationArrialsDataResponseEvent;
import com.nituv.mot.siri.client.ILbiStationArrivalsDataResponse;
import com.nituv.mot.siri.client.LbiNetworkFaliureException;
import com.nituv.mot.siri.client.LbiSiriClientFactory;
import com.nituv.mot.siri.client.response.LbiSiriResponseUtils;

class LbiLinesBehaviourIntelligenceMgr implements ILbiLinesBehaviourIntelligenceMgr 
{

	private ILbiLinesBehaviourIntelligenceMgrDal m_dal;
	private boolean m_isMonitoringDbForNewInvestigations;
	private TreeMap<String,LbiTripInvestigationProcessData> activeTripInvestigationKeys = new TreeMap<String,LbiTripInvestigationProcessData>();
	private TreeMap<Integer,Integer> activeRouteInvestigationCount = new TreeMap<Integer,Integer>();
	ILbiSiriWebServiceClient siriClient;

	public static void main(String[] args) {
		try {
			LbiLinesBehaviourIntelligenceFactory.createLinesBehaviourIntelligenceMgr()
					.startDbMonitoringForNewInvestigations();
		} catch (Exception ex) {
			Lg.lgr.fatal(ex);
		}
	}
	
	
	public LbiLinesBehaviourIntelligenceMgr(
			ILbiLinesBehaviourIntelligenceMgrDal dal
	) throws LbiParameterException, LbiNetworkFaliureException
	{
		Lg.lgr.info("LbiLinesBehaviourIntelligenceMgr was created.");
		m_dal = dal;
		LbiRouteInvestigationLogic.setDal(dal);
		siriClient = LbiSiriClientFactory.createLbiSiriWebServiceClient();
		siriClient.connect();
	}

	@Override
	public void startDbMonitoringForNewInvestigations() throws LbiInternalException {
		
		if (m_isMonitoringDbForNewInvestigations)
		{
			Lg.lgr.warn("Db Monitoring For New Investigations is already running.");
			return;
		} else {
			m_isMonitoringDbForNewInvestigations = true;
		}
		Lg.lgr.info("starting DB monitoring for new Investigations.");

		try {// change status of route Investigations that were running in previous application execution to "started not running"
			int effectedCount = m_dal.updateRouteInvestigationsFromRunningToNotRunning();
			Lg.lgr.info("{} route investigation were updated from running to started not running", effectedCount);
		} catch (LbiInternalException ex) {
			Lg.lgr.error("danger! can't change status of route Investigations that were running in previous application execution to 'started not running'.",ex);
		}
		LbiScheduledThreadsPoolMgr.waitTillReady();
		LbiScheduledThreadsPoolMgr.getScheduledThreadPoolExecutor().
			scheduleWithFixedDelay(
				getRunnableForCheckDbForNewInvestigations(), 
				0, 
				1500, 
				TimeUnit.MILLISECONDS
			);
		
		//search route investigations that have enough samples for all time frames
		LbiScheduledThreadsPoolMgr.getScheduledThreadPoolExecutor().
			scheduleAtFixedRate(
				getRunnableForMarkCompletedInvestigations(), 
				5, 
				20, 
				TimeUnit.MINUTES
			);
		/*final Timer timer = new Timer("DbMonitoringForNewInvestigations",false);
		TimerTask taskForCheckDbForNewInvestigations = createTimeTaskForCheckDbForNewInvestigations();
		TimerTask taskForMarkingInvestigationsAsCompleted = createTimeTaskForMarkingInvestigationsAsCompleted();
		timer.schedule(taskForCheckDbForNewInvestigations, 0, 1000);//check every 1 second
		timer.schedule(
				taskForMarkingInvestigationsAsCompleted, 
				10 * 1000, // start in 10 seconds 
				5 * 60 * 1000 //check every 5 minutes
		);*/
		
	}

	private Runnable getRunnableForMarkCompletedInvestigations() {
		return new Runnable() 
		{ 
			@Override
			public void run() {
				if (!m_isMonitoringDbForNewInvestigations) {
					LbiScheduledThreadsPoolMgr.remove(this);
					return;
				}
				try {
					markCompletedInvestigations();
				} catch (Exception ex) {
					Lg.lgr.error(ex);
				}
			}

		};
	}
	
	//search route investigations that have enough samples for all time frames
	private void markCompletedInvestigations() throws LbiInternalException
	{
		//TODO - fix this
		if (5+3 > 0) return;
		
		// search db for running routes investigations
		List<Integer> runningRoutesInvestigations = null;
//		List<Integer> runningRoutesInvestigations = m_dal.getRunningRoutesInvestigations();
		boolean wasAnyTripInvestigationFound;
		// for each
		for (Integer routeInvestigationId : runningRoutesInvestigations) {
			wasAnyTripInvestigationFound = false;
			for (LbiTripInvestigationProcessData tripProcessData : activeTripInvestigationKeys.values()) {
				if (tripProcessData != null 
						&& tripProcessData.getRouteInvestigation() != null
						&& tripProcessData.getRouteInvestigation().getGtfsRouteIdFk() != null
				) {
					// check if has active investigations in map
					if (tripProcessData.getRouteInvestigation().getGtfsRouteIdFk().equals(routeInvestigationId)) {
						wasAnyTripInvestigationFound = true;
						// mark old investigations as failed
						if (tripProcessData.getExpectedArrivalTime() != null) {
							int gapFromNow = NtvDateUtils.getSecondsGapFromNow(tripProcessData.getExpectedArrivalTime());
							// if late more then 3 hours 
							if (gapFromNow < -3 * 3600) {
								if (tripProcessData.getTripInvestigationId()!=null) {
									m_dal.updateTripInvestigationStatus(tripProcessData.getTripInvestigationId(), ELbiTripInvestigationStatuses.TIMEOUT.getId());
									markTripInvestigationEnded(tripProcessData);
								}
							}
							
						}
					}
					// else
					if (!wasAnyTripInvestigationFound) {
						List<Time> neededTimeFrames = m_dal.getRequiredTimeFrames(routeInvestigationId);
						// check if filled all time frames
						try {
						if (neededTimeFrames.size() == 0) {
							m_dal.updateRouteInvestigationStatus(routeInvestigationId, ELbiRouteInvestigationStatuses.COMPLETED.getId());
							// mark as completed 
						// else
						} else {
							// mark as not running
							m_dal.updateRouteInvestigationStatus(routeInvestigationId, ELbiRouteInvestigationStatuses.STARTED_NOT_RUNNING.getId());
						}
						} catch (LbiNotFoundException ex) {
							Lg.lgr.error("not found routeInvestigationId:{}",routeInvestigationId,ex);
						}
					}
				}
				
			} 
		}
			
			
		
		// remove 
		
	}

	private Runnable getRunnableForCheckDbForNewInvestigations() {
		return new Runnable() 
		{ 
			@Override
			public void run() {
				if (!m_isMonitoringDbForNewInvestigations) {
					LbiScheduledThreadsPoolMgr.remove(this);
					return;
				}
				try {
					checkDbForNewInvestigations();
				} catch (Exception ex) {
					Lg.lgr.error(ex);
				}
			}
		};
	}

	@Override
	public int checkDbForNewInvestigations() throws LbiInternalException {
		try {
			List<LbiRouteInvestigation> list = m_dal.getRelevantRouteInvestigations();
			if (list.size() > 0) {
				Lg.lgr.info(list.size() + " relevant investigations were found.");
			}

			for (LbiRouteInvestigation routeInvestigation: list) {
				handleNewRouteInvestigation(routeInvestigation);
			}
			return list.size();
		} catch (LbiNotFoundException ex) {
			Lg.lgr.debug("no relevant reasults");
			return 0;
		}
	}
	
	private void handleNewRouteInvestigation(LbiRouteInvestigation routeInvestigation) throws LbiInternalException {
		Lg.lgr.info("handling. routeInvestigation:" + routeInvestigation.toString());
		if (!LbiRouteInvestigationLogic.isInvestigationNeedsMoreSamplesAtThisTimeFrame(routeInvestigation)) {
			switch (routeInvestigation.getRouteInvestigationType())
			{
			case STATISTICAL_TRIPS_DURATIONS:
				lookAndProcessNextCommingBusOnSiriResponseRehandleInvestigationOnNextTimeFrame(routeInvestigation);
				break;
			case STATISTICAL_STATIONS_STOPS:
				throw new LbiInternalException("need to imp");
			case ALL_TRIP_DURATIONS:
				throw new LbiInternalException("need to imp");
			case ALL_STATIONS_STOPS:
				throw new LbiInternalException("need to imp");
			case ROUTE_SNAPSHOT: 
				throw new LbiInternalException("need to imp");
			default:
				Lg.lgr.warn("no handler for typeid:" + routeInvestigation.getRouteInvestigationTypeIdFk() + ". routeInvestigation:" + routeInvestigation.toString());		
			}
			
			return;
		}
		switch (routeInvestigation.getRouteInvestigationType())
		{
		case STATISTICAL_TRIPS_DURATIONS:
			handleInvestigateRouteStatisticalTripsDurations(routeInvestigation);
			break;
		case STATISTICAL_STATIONS_STOPS:
			handleInvestigateRouteStatisticalStationsStops(routeInvestigation);
			break;
		case ALL_TRIP_DURATIONS:
			handleInvestigateAllTripDurations(routeInvestigation);
			break;
		case ALL_STATIONS_STOPS:
			handleInvestigateAllStationsStops(routeInvestigation);
			break;
		case ROUTE_SNAPSHOT: 
			handleInvestigateRouteSnapshot(routeInvestigation);
			break;
		default:
			Lg.lgr.warn("no handler for typeid:" + routeInvestigation.getRouteInvestigationTypeIdFk() + ". routeInvestigation:" + routeInvestigation.toString());		
		}
	}

	private void handleInvestigateRouteStatisticalTripsDurations(LbiRouteInvestigation routeInvestigation) throws LbiInternalException
	{
		//mark investigation as running
		try {
			m_dal.updateRouteInvestigationStatus(
					routeInvestigation.getRouteInvestigationId(), 
					ELbiRouteInvestigationStatuses.RUNNING.getId()
			);
		} catch (LbiNotFoundException ex) {throw Lg.lgr.throwing(new LbiInternalException(ex));}

		Lg.lgr.debug("handling now investigation:{}.", routeInvestigation);
		LbiScheduledThreadsPoolMgr.execute(getRunnableForLookAndProcessNextCommingBus(routeInvestigation));
	}

	private void rehandleInvestigateRouteStatisticalTripsDurations(LbiRouteInvestigation routeInvestigation, Date timeToRun)
	{
		Lg.lgr.debug("rehandling investigation:{}. at:{}", routeInvestigation, NtvDateUtils.toUtcDateTime(timeToRun));
		try {
			LbiScheduledThreadsPoolMgr.schedule(
					getRunnableForLookAndProcessNextCommingBus(routeInvestigation),
					timeToRun
			);
		} catch (LbiParameterException ex) {
			Lg.lgr.error(ex);
		}
	}
	
	private void continueInvestigateRouteStatisticalTripsDurations(LbiTripInvestigationProcessData processData, int secondsDelay) 
	{
		Lg.lgr.debug("continue investigation. processData:{}. in {} sec. at:{}", processData, secondsDelay, NtvDateUtils.toUtcDateTime(new Date(System.currentTimeMillis() + (secondsDelay*1000))));
		LbiScheduledThreadsPoolMgr.schedule(getRunnableForLookAndProcessNextCommingBus(processData),secondsDelay);
	}

	private void continueInvestigateRouteStatisticalTripsDurationsWaitForVehicleArrival(LbiTripInvestigationProcessData processData) {
		int secondsToWait = LbiRouteInvestigationLogic.getSecondsToWaitForVehicleArrival(processData);
		continueInvestigateRouteStatisticalTripsDurations(processData, secondsToWait);
	}
	
	private Runnable getRunnableForLookAndProcessNextCommingBus(
			LbiRouteInvestigation routeInvestigation
	) {
		LbiTripInvestigationProcessData processData = new LbiTripInvestigationProcessData();
		processData.resetOnStart(routeInvestigation);
		return getRunnableForLookAndProcessNextCommingBus(processData);
	}
	
	private Runnable getRunnableForLookAndProcessNextCommingBus(
			LbiTripInvestigationProcessData processData
	) {
		processData.setBetweenThreads(true);
		return new Runnable() {
			public void run() {
				try {
					processData.setBetweenThreads(false);
					lookAndProcessNextCommingBus(processData);
				} catch (Exception ex) {
					Lg.lgr.error(ex);					
				}
			}
		};
	}
	

	
	private void lookAndProcessNextCommingBusOnSiriResponse(
			LbiTripInvestigationProcessData processData,
			ArrayList<ArrayList<ILbiStationArrivalsDataResponse>> responseList
	) throws LbiInternalException 
	{
		processData.setBetweenThreads(false);
		try {
			processData.setLastSiriResponseTime(new Date());
			processData.markStationCheckTry();
			setStationArrivalDataBySiriResponse(processData, responseList);
			
			if (processData.isFirstStationBeforeTripKeySet()) {
				processData.setTripKey();
				if (processData.getTripKey() != null) {
					try {
						markTripInvestigationStarted(processData);
					} catch (LbiAllReadyExecuting ex) {
						Lg.lgr.error("trip is already under investigation. stopping investigation. processData:" + processData);
						return;
					}
				}
			}
			processData.markIfTripKeyChanged();
			
			
			//!need more samples!
			// if first station		
			if (processData.isFirstStation()) { // first station
				lookAndProcessNextCommingBusOnSiriResponseHandleFirstStation(processData);
			// if last station
			} else if (processData.isLastStation()) { // last station
				lookAndProcessNextCommingBusOnSiriResponseHandleLastStation(processData);
			} else {
				Lg.lgr.error("station index is not right. processData.stationIndex=" + processData.getStationIndex());
			}
		} finally {
			// if something got wrong and work was not transfered to a new thread
			// we need to mark this trip as ended.
			if (!processData.isBetweenThreads()) {
				markTripInvestigationEnded(processData,true);
			}
		}
	}


	private void setStationArrivalDataBySiriResponse(LbiTripInvestigationProcessData processData,
			ArrayList<ArrayList<ILbiStationArrivalsDataResponse>> responseList) 
	{
		ILbiStationArrivalsDataResponse relevantStationArrivalData = 
				LbiSiriResponseUtils.getRelevantStationArrivalData(
						responseList, 
						processData.getTripKey(),
						processData.getRouteInvestigation().getGtfsRouteIdFk()
				);
		if (relevantStationArrivalData != null) {
			processData.setStationArrivalData(relevantStationArrivalData);
			return;
		}
		if (relevantStationArrivalData == null && processData.getTripKey() != null) 
		{
			if (processData.isFirstStation()) {
				if (NtvArraysUtils.isEmpty(responseList)) {
					Lg.lgr.debug("No tripkey match! no SIRI data, first station with existing trip key. probebly the bus has left the station and no more trips for today. processData:{}",processData);
					//later the is a code that will forward to the last station 
					processData.setStationArrivalData(null); // relevantStationArrivalData == null
					//(need to advance to last station)
				} else {
					ILbiStationArrivalsDataResponse firstArrivalData = 
							LbiSiriResponseUtils.getFirstActualStationArrivalData(responseList,processData.getRouteInvestigation().getGtfsRouteIdFk());
					Lg.lgr.debug("No tripkey match! has SIRI data but not matching our key, first station with existing trip key. probebly the bus has left the station. firstArrivalData:{}, processData:{}",firstArrivalData,processData);
					processData.setStationArrivalData(firstArrivalData);
					//(need to advance to last station)
				}
			} else { //last station
				processData.setStationArrivalData(null); 
				if (processData.wasTripKeyDetectedOnLastStation()) {
					if (NtvArraysUtils.isEmpty(responseList)) {
						Lg.lgr.debug("No tripkey match! no SIRI data, existing trip key, trip key was detected at last station. probably bus arrived to last station. processData:{}", processData);
						//(need to mark arrival)
					} else {
						Lg.lgr.debug("No tripkey match! has SIRI data, existing trip key, trip key was detected at last station. probably bus arrived to last station. processData:{}", processData);
						//(need to mark arrival)
					}
				} else {
					if (NtvArraysUtils.isEmpty(responseList)) {
						Lg.lgr.debug("No tripkey match! no SIRI data, existing trip key, trip key was NOT detected at last station. need to wait by GTFS data. processData:{}", processData);
//								m_dal.updateTripInvestigationAsFailed(processData.getTripInvestigationId());
//								//(NEED to ask GTFS for trip duration)
					} else {
						Lg.lgr.debug("No tripkey match! has SIRI data, existing trip key, trip key was NOT detected at last station. need to wait by GTFS data. processData:{}", processData);
//								//(NEED to ask GTFS for trip duration)
					}
				}
			}
		}
	}


	private void waitForATripStartByGtfsData(LbiTripInvestigationProcessData processData) throws LbiInternalException {

		Date nextTripTimeByGtfsData;
		nextTripTimeByGtfsData = LbiRouteInvestigationLogic.getNextTripTimeByGtfsData(processData.getRouteInvestigation().getGtfsRouteIdFk());
		if (nextTripTimeByGtfsData == null) {
			//mark route as "has no future trips"
			try {
				m_dal.updateRouteInvestigationStatus(
						processData.getRouteInvestigation().getRouteInvestigationId(), 
						ELbiRouteInvestigationStatuses.NO_SCHEDULED_TRIPS_BY_GTFS_DATA.getId()
				);
			} catch (LbiNotFoundException ex) {throw Lg.lgr.throwing(new LbiInternalException(ex));}

			Lg.lgr.warn("getNextTripTimeByGtfsData retrun nothing. route without future trips. exiting.");
			return;
		}
		Date nextTripTimeByGtfsDataLessFive = DateUtils.addMinutes(nextTripTimeByGtfsData, -5);
		Lg.lgr.debug("removing 5 minutes from what we got from gtfs data. nextTripTimeByGtfsData:{}, lessFive:{}", NtvDateUtils.formatTimeShortString(nextTripTimeByGtfsData), NtvDateUtils.formatTimeShortString(nextTripTimeByGtfsDataLessFive));
		rehandleInvestigateRouteStatisticalTripsDurations(processData.getRouteInvestigation(), nextTripTimeByGtfsDataLessFive);
	}

//	private List<String> list = List<String>
	private void markTripInvestigationStarted(LbiTripInvestigationProcessData processData) 
			throws LbiAllReadyExecuting, LbiInternalException
	{
		if (!processData.isFirstStationBeforeTripKeySet()) {
			return;
		}
		processData.setInvestigationStartTime(new Date());

		Integer routeId = processData.getRouteInvestigation().getGtfsRouteIdFk();
		Date aimeDepartureTime = processData.getExpectedDepartureTime();
		if (routeId == null || aimeDepartureTime == null) {
			throw new LbiInternalException("routeId == null || aimeDepartureTime == null");
		}
		String key = routeId + "_" + NtvDateUtils.formatTimeShortString(aimeDepartureTime);

		synchronized (activeTripInvestigationKeys) 
		{
			if (activeTripInvestigationKeys.containsKey(key)) {
				LbiTripInvestigationProcessData previousRunningProcessData = activeTripInvestigationKeys.get(key);
//				Date previousThreadStartTime = ; 
				// if key exists there is another thread working on this trip. need to stop this thread.
//				final int HOURS_TO_WARN = 6;
//				final int HOURS_TO_REMOVE = 24; //TODEBUG if SIRI response can give station arrival data for more then 24 hours this me remove keys for relevant tasks. (e.g. like on friday give data for sunday)
				// need to handle this in a central place
//				if (NtvDateUtils.getSecondsGapFromNow(previousThreadStartTime) > (60 * 60) * HOURS_TO_WARN) {
//					Lg.lgr.error("previous running thread is older than {} hours. previousRunningProcessData:" + previousRunningProcessData);
//				}
//				if (NtvDateUtils.getSecondsGapFromNow(previousThreadStartTime) > (60 * 60) * HOURS_TO_REMOVE) {
//					Lg.lgr.error("previous running thread is older than {} hours. removing it from map. start time:{}", HOURS_TO_REMOVE, NtvDateUtils.formatDateUtc(previousThreadStartTime));
//					activeTripInvestigationKeys.remove(key);
//				}

				throw new LbiAllReadyExecuting("key:" + key + " alrady existes. need to stop current thread. previousRunningProcessData:" + previousRunningProcessData);
			}
			
			activeTripInvestigationKeys.put(key, processData);
			
			int newCount;
			if (activeRouteInvestigationCount.containsKey(routeId))
			{
				newCount = activeRouteInvestigationCount.get(routeId) + 1;
				if (newCount < 1) {
					Lg.lgr.error("updating(+) routeId:{} trips count:{} is below 1. setting it to 1.", routeId, newCount);
					newCount = 1;
				} else {
					Lg.lgr.trace("updating(+) routeId:{} trips count:{}", routeId, newCount);
				}
				activeRouteInvestigationCount.put(routeId,newCount);
			} else {
				Lg.lgr.trace("adding routeId:{} trips count:1", routeId);
				newCount = 1;
				activeRouteInvestigationCount.put(routeId,newCount);
			}
		}
	}

	private void markTripInvestigationEnded(
			LbiTripInvestigationProcessData processData
	) throws LbiInternalException
	{
		markTripInvestigationEnded(processData,false);
	}
	
	@SuppressWarnings("unused")
	private void markTripInvestigationEnded(
			LbiTripInvestigationProcessData processData,
			boolean isFromFinlly
	) throws LbiInternalException
	{
		Integer routeId = processData.getRouteInvestigation().getGtfsRouteIdFk();
		Date aimeDepartureTime = processData.getExpectedDepartureTime();
		if (routeId == null) {
			throw new LbiInternalException("routeId == null");
		}
		if (aimeDepartureTime == null) {
			//not trip key - probably ended before it started 
			return;
		}
		String key = routeId + "_" + NtvDateUtils.formatTimeShortString(aimeDepartureTime);

		synchronized (activeTripInvestigationKeys) 
		{
			LbiTripInvestigationProcessData processDataFromMap = null;
			processDataFromMap = activeTripInvestigationKeys.remove(key);
			if (processDataFromMap == null) {
				Lg.lgr.warn("key:{} is not in map. processData:{}", key, processData);
				return;
			}
			Date startDate = processDataFromMap.getInvestigationStartTime();
			if (processDataFromMap == null)
			{
//				Level level;
//				if (isFromFinlly) {
//					level = Level.ERROR;
//				} else {
//					level = Level.WARN;
//				}
				Lg.lgr.warn("key was not found. key:{}. processData:{}",key, processData);
				// since this is an atomic action (key and trips count for routeId)
				// so if there is no key, there's also no need to reduce count in trip count.
				// so we need to exit
				return;  
			}
			
			if (!processData.equals(processDataFromMap)) {
				Lg.lgr.warn("processData:{}, IS DIFFERENT THEN processDataFromMap:{}",processData, processDataFromMap);
			}
			
			if (isFromFinlly) {
				Lg.lgr.debug("removing key from finally block. key:{}, processData:{}, key insert time:{}",key, processData, NtvDateUtils.formatDateUtc(startDate));
			}
			
			int newCount;
			if (activeRouteInvestigationCount.containsKey(routeId))
			{
				newCount = activeRouteInvestigationCount.get(routeId) - 1;
				if (newCount < 0) {
					Lg.lgr.error("updating(-) routeid:{} trips count. value is negative:{}, setting value to zero. key insert time:{}",routeId, newCount, NtvDateUtils.formatDateUtc(startDate));
					newCount = 0;
				} else {
					Lg.lgr.trace("updating(-) routeId:{} trips count:{}, key insert time:{}", routeId, newCount, NtvDateUtils.formatDateUtc(startDate));
				}
				activeRouteInvestigationCount.put(routeId,newCount);
			} else {
				Lg.lgr.error("no key for routeId:{} in activeRouteInvestigationCount map. adding it. key insert time:{}", NtvDateUtils.formatDateUtc(startDate));
				newCount = 0;
				activeRouteInvestigationCount.put(routeId,newCount);
			}
		}
	}

	private void updateProcessDataOfTripInvestigationKey(LbiTripInvestigationProcessData processData) 
	{
		Integer routeId = processData.getRouteInvestigation().getGtfsRouteIdFk();
		Date aimeDepartureTime = processData.getExpectedDepartureTime();
		if (routeId == null || aimeDepartureTime == null) {
			Lg.lgr.error("routeId == null || aimeDepartureTime == null. processData:{}",processData);
		}
		String key = routeId + "_" + NtvDateUtils.formatTimeShortString(aimeDepartureTime);

		synchronized (activeTripInvestigationKeys) 
		{
			if (!activeTripInvestigationKeys.containsKey(key)) {
				Lg.lgr.warn("key should be here:{}. processData:{}",key,processData);
			}
			activeTripInvestigationKeys.put(key,processData);
		}
		
	}


	private boolean lookAndProcessNextCommingBusOnSiriResponseHandleTimeFrameCheckIsFull(
			LbiTripInvestigationProcessData processData
	) throws LbiInternalException 
	{
		// if route has enough samples for this time frame (count also trip investigation that are under process now -1)
		boolean retVal;
		if (!LbiRouteInvestigationLogic.isInvestigationNeedsMoreSamplesAtTimeFrame(
				processData.getRouteInvestigation(),
				processData.getStationArrivalData().getOriginAimedDepartureTime()
				))
		{
			LbiRouteInvestigation routeInvestigation = processData.getRouteInvestigation();
			lookAndProcessNextCommingBusOnSiriResponseRehandleInvestigationOnNextTimeFrame(routeInvestigation);
			retVal = true;
		} else {
			retVal = false;
		}
		processData.setTimeFrameCheckedIfFull(true);
		return retVal;
	}

	private void lookAndProcessNextCommingBusOnSiriResponseRehandleInvestigationOnNextTimeFrame(LbiRouteInvestigation routeInvestigation)
			throws LbiInternalException 
	{
		Lg.lgr.info("current time frame is full. searching for the next required time frame.");
		// check when is the next required time frame without enough samples
		Date nextRequiredTimeFrameStart = 
				LbiRouteInvestigationLogic.getNextRequiredTimeFrameStart(
						routeInvestigation.getRouteInvestigationId()
				);
		if (nextRequiredTimeFrameStart == null) {
			try {
			m_dal.updateRouteInvestigationStatus(
					routeInvestigation.getRouteInvestigationId(), 
					ELbiRouteInvestigationStatuses.COMPLETED.getId()
			);
			} catch (LbiNotFoundException ex) {Lg.lgr.error("getRouteInvestigationId:{} was not found.", routeInvestigation.getRouteInvestigationId(), ex);}
			return;
		}
		// wait for the next time frame (minus 20 seconds) and recall this function
		nextRequiredTimeFrameStart = new Date(nextRequiredTimeFrameStart.getTime() - (20 * 1000));
		if (NtvDateUtils.isInThePast(nextRequiredTimeFrameStart)) {
			Lg.lgr.warn("nextRequiredTimeFrameStart is in the past:" + NtvDateUtils.formatDateUtc(nextRequiredTimeFrameStart));
			nextRequiredTimeFrameStart = new Date();
		}
		try {
			m_dal.updateRouteInvestigationStatus(
					routeInvestigation.getRouteInvestigationId(), 
					ELbiRouteInvestigationStatuses.WAITING_TO_NEXT_TIME_FRAME.getId()
			);
		} catch (LbiNotFoundException ex) {Lg.lgr.error("getRouteInvestigationId:{} was not found.", routeInvestigation.getRouteInvestigationId(), ex);}

		rehandleInvestigateRouteStatisticalTripsDurations(routeInvestigation,nextRequiredTimeFrameStart);
	}

	private void lookAndProcessNextCommingBusOnSiriResponseHandleLastStation(
			LbiTripInvestigationProcessData processData
	) throws LbiInternalException 
	{
		
		if (!processData.wasTripKeyDetectedOnLastStation()) {
			// query gtfs trip length wait half
			waitForTripKeyOnLastStationByGtfsTripDuration(processData);
			return;
		}
		
		// if first check try 
		if (processData.isTripKeyDetectedOnLastStationNow()) {
			// update trip_investigations with expected_arrival_time, expected_seconds_duration
			Lg.lgr.debug("tripkey detected from last station. marking arrival time. processData:{}", processData);
			m_dal.updateTripInvestigationExpectedArrivalTimeAndExpectedSecondsDuration(
					processData.getTripInvestigationId(),
					processData.getStationArrivalData().getExpectedArrivalTime(),
					processData.getSecondsBetweenExpectedArrivalTimeAndDepartureTime()
			);
			updateProcessDataOfTripInvestigationKey(processData);
		}
		
		// if arrived (1.check vehicle_at_stop 2. Or the ETA is for the next bus 3. Or distance is under x meters)
		Date arrivalTime = LbiRouteInvestigationLogic.getArrivalTimeIfArrived(processData);
		if (arrivalTime != null) {
			// update trip investigation record with all data
			if (processData.isBusTransmittedOnLastStation()
					|| (!processData.isTripKeyChanged() && processData.isBusTransmitting())
			) {
				Lg.lgr.debug("ARRIVED and TRANSSMITING. marking arrival time. processData:{}", processData);
				m_dal.updateTripInvestigationActualArrivalTimeAndActualSecondsDurationAndStatusCompleted(
						processData.getTripInvestigationId(), 
						arrivalTime,
						NtvDateUtils.getSecondsGap(processData.getActualDepartureTime(), arrivalTime)
						);
			} else {
				Lg.lgr.debug("ARRIVED but NOT transsmiting. marking as failed. processData:{}", processData);
				m_dal.updateTripInvestigationStatus(
						processData.getTripInvestigationId(),
						ELbiTripInvestigationStatuses.NO_VEHICLE_TRANSMITION_ON_ARRIVAL.getId()
				);
			}
			// (no need to lunch new trip investigation for the next bus since we did this on the first station departure
			//markTripInvestigationEnded(processData); // no need will happen in finally block
		// else if not arrived
		} else { // not arrived
			if (!processData.isBusTransmitting()) {
				processData.incrementNoTransmittionCountOnLastStation();
				if (processData.getNoTransmittionCountOnLastStation() >= 3
				) {
					Lg.lgr.debug("bus is not transsmiting (from arrival station). not trying anymore. marking as failed. processData:{}", processData);
					m_dal.updateTripInvestigationStatus(
							processData.getTripInvestigationId(),
							ELbiTripInvestigationStatuses.NO_VEHICLE_TRANSMITION_ON_ARRIVAL.getId()
					);
					//markTripInvestigationEnded(processData); // no need will happen in the finally block
					return;
				} 
			}
			processData.setBusTransmittedOnLastStation(processData.isBusTransmitting());
			// recall (delay:(expected arrival time of bus / 2.5) but at least 30 seconds.)
			continueInvestigateRouteStatisticalTripsDurationsWaitForVehicleArrival(processData);
		}
	}


	private void waitForTripKeyOnLastStationByGtfsTripDuration(LbiTripInvestigationProcessData processData)
			throws LbiInternalException {
		int expTripDuration;
		try {
			expTripDuration = m_dal.getGtfsTripDuration(
					processData.getRouteInvestigation().getGtfsRouteIdFk(),
					processData.getTripKey());
		} catch (LbiNotFoundException ex) {
			try {
				Lg.lgr.info("qurying gtfs for ROUTE duration. since we didn't get from SIRI and didn't found trip GTFS data. processData:{}", processData);
				expTripDuration = m_dal.getGtfsRouteDuration(processData.getRouteInvestigation().getGtfsRouteIdFk());
			} catch (LbiNotFoundException e) {
				Lg.lgr.warn("no trip or route duration time found on GTFS data. marking as failed. processData:{}", processData);
				m_dal.updateTripInvestigationStatus(processData.getTripInvestigationId(), 
						ELbiTripInvestigationStatuses.MISSING_GTFS_DATA.getId()
				);
				return;
			}
		}
		// wait half of the remaining time of the journey something will change and we'll get SIRI data for our bus.
		int waitedTime = NtvDateUtils.getSecondsGap(processData.getActualDepartureTime(), new Date());
		int remaningSeconds = expTripDuration - waitedTime;
		int secondsToWait = remaningSeconds / 2;
		int MINIMUM_MINUTES_TO_WAIT = 5;
		if (secondsToWait * 60  >= MINIMUM_MINUTES_TO_WAIT) {
			Lg.lgr.debug("NO SIRI data on arrival station. we wated so far:{}, expTripDuration:{}, remaningSeconds:{}, waiting half of it:{} maybe something will change and we'll get SIRI data for our bus. processData:{}", 
					waitedTime,
					expTripDuration,
					remaningSeconds,
					secondsToWait,
					processData
			);
			continueInvestigateRouteStatisticalTripsDurations(processData, secondsToWait);
			return;
		}  else {
			Lg.lgr.warn("NO SIRI data on arrival station. we wated so far:{}, expTripDuration:{}, remaningSeconds:{}. secondsToWait:{}. not waiting less then:{} marking as failed. processData:{}:",
					waitedTime,
					expTripDuration,
					remaningSeconds,
					secondsToWait,
					MINIMUM_MINUTES_TO_WAIT, 
					processData
			);
			m_dal.updateTripInvestigationStatus(
					processData.getTripInvestigationId(), 
					ELbiTripInvestigationStatuses.NO_SIRI_DATA_ON_ARRIVAL_STATION.getId()
			);
			return;
		}
	}

	private void lookAndProcessNextCommingBusOnSiriResponseHandleFirstStation(
			LbiTripInvestigationProcessData processData
	) throws LbiInternalException 
	{

		// if we didn't get any trip key meaning we got no SIRI data
		if (processData.getTripKey() == null) {
			Lg.lgr.debug("we didn't get any SIRI stationArrivalData on first station / first try. quering gtfs for next trip and waiting for it. processData:{}:", processData);
			waitForATripStartByGtfsData(processData);
			return;
		}
		
		// if first station and first check try
		if (!processData.isTimeFrameCheckedIfFull())
		{
			if (lookAndProcessNextCommingBusOnSiriResponseHandleTimeFrameCheckIsFull(processData)) {
				// current time frame doesn't need more samples
				return;
			}
		}

		
		// we wan't to mark if bus transsimtter near first station
		if (processData.getTripKey() != null 
				&& !processData.isTripKeyChanged() 
				&& !processData.isBusTransmittedNearFirstStation()
		) {
			if (processData.isBusTransmitting()) {
				processData.setBusTransmittedNearFirstStation(true);
				Lg.lgr.debug("bus transsmited in first station. processData:{}",processData);
				
				if (!processData.isStationFirstTry()) {
					Lg.lgr.debug("bus transsmition identified. processData:{}. lat:{}, lon:{}, isVehicleAtStop:{}", processData, processData.getStationArrivalData().getVehicleLatitude(), processData.getStationArrivalData().getVehicleLongitude(), processData.getStationArrivalData().isVehicleAtStop());
					m_dal.updateTripInvestigationBusWasDetectedNearDepartureStation(processData.getTripInvestigationId(),true);
				}
			}
		}
		
		// if first check try 
		if (processData.isStationFirstTry()) {
			// mark the expected departure time - this is our trip key
			//processData.aimedDepartureTime = processData.stationArrivalData.getOriginAimedDepartureTime();
			// create trip investigation record
			int tripInvestigationId = m_dal.insertTripInvestigation(
					processData.getRouteInvestigation().getGtfsRouteIdFk(),
					processData.getRouteInvestigation().getRouteInvestigationId(),
					processData.getExpectedDepartureTime(), //tripkey
					processData.isBusTransmittedNearFirstStation()
				);
			processData.setTripInvestigationId(tripInvestigationId);
			updateProcessDataOfTripInvestigationKey(processData);
		}
		
		// if exited station (the ETA is for the next bus = trip key changed)
		if (processData.isTripKeyChanged()) {

			// mark departure time between now and previous vehicle record time.
			Date departureTime = processData.getMeanBetweenNowAndPreviousRecordedAtTime();
			processData.setActualDepartureTime(departureTime);
			m_dal.updateTripInvestigationActualDepartureTimeAndStatusToStarted(processData.getTripInvestigationId(), departureTime);
			updateProcessDataOfTripInvestigationKey(processData);

			if (!processData.isBusTransmittedNearFirstStation()) {
				lunchCheckSecondStationForBusTransmittion(processData);
			}
			
			// recall for the last station (delay 2 minutes)
			processData.advanceToLastStation();
			continueInvestigateRouteStatisticalTripsDurations(processData, 120);

			if (processData.hasNewStationArrivalDataAfterTripKeyChange()) {
				
				// lunch new trip investigation for the next bus. recall (delay: next bus expected arrival / 2)
				Date timeToCheckNextBus = NtvDateUtils.getMeanDate(
						processData.getStationArrivalData().getOriginAimedDepartureTime(),
						new Date()
					);
				rehandleInvestigateRouteStatisticalTripsDurations(processData.getRouteInvestigation(), timeToCheckNextBus);
			} else {
				Lg.lgr.debug("no next journy by SIRI data. quering gtfs for next trip and waiting for it. current processData:{}:", processData);
				waitForATripStartByGtfsData(processData);
			}
		}
		
		// else (key not changed) if arrived (check vehicle_at_stop)
		else if (processData.isVehicleAtStop()) { 
			// recall (30 sec delay) (wait for departure)
			continueInvestigateRouteStatisticalTripsDurations(processData, 30);
		// else not in station - not arrived yet
		} else { // not arrived
			// recall (delay:(expected arrival time of bus / 2.5) but at least 30 seconds.)
			continueInvestigateRouteStatisticalTripsDurationsWaitForVehicleArrival(processData);
		}
	}

	private void lunchCheckSecondStationForBusTransmittion(LbiTripInvestigationProcessData processData) {
		Lg.lgr.debug("lunching  checkSecondStationForBusTransmittion now. processData:{}",processData);
		LbiScheduledThreadsPoolMgr.schedule(getRunnableForCheckSecondStationForBusTransmittion(processData),0);
	}
	
	private Runnable getRunnableForCheckSecondStationForBusTransmittion(
			LbiTripInvestigationProcessData processData
	) {
		return new Runnable() {
			public void run() {
				try {
					checkSecondStationForBusTransmittion(processData);
				} catch (Exception ex) {
					Lg.lgr.error(ex);					
				}
			}
		};
	}

	private void checkSecondStationForBusTransmittion(LbiTripInvestigationProcessData processData)
		throws LbiInternalException
	{
		int investigatedStationCode;
		try {
			investigatedStationCode = m_dal
					.getSecondStationCodeOfRoute(processData.getRouteInvestigation().getGtfsRouteIdFk());
		} catch (LbiNotFoundException ex) {
			throw new LbiInternalException(ex);
		}
		
//		try {
//			siriClient.connect();
//		} catch (LbiNetworkFaliureException ex) {
//			throw Lg.lgr.throwing(new LbiInternalException(ex));
//		}
		//for (Integer stationCode:stationCodes){
		siriClient.requestStationArrivalsData(
				investigatedStationCode,
				processData.getRouteInvestigation().getGtfsRouteIdFk(),
				1,  
				new ILbiStationArrialsDataResponseEvent() {
					@Override
					public void onFailure(Exception ex) {
						processData.markNetworkFailure();
						Lg.lgr.warn("failed on:routeInvestigationId:{}, stationCode:{}, failCount:{}", processData.getRouteInvestigation().getRouteInvestigationId(), investigatedStationCode, processData.getNetworkFailureCount(), ex);
					}
					
					@Override
					public void onComplete(ArrayList<ArrayList<ILbiStationArrivalsDataResponse>> responseList) {
						try {
							checkSecondStationForBusTransmittionOnComplete(processData, responseList);
						} catch (Exception ex) {
							Lg.lgr.error("failed to process SIRI response. processData:{}, stationCode:{}", 
									processData,
									investigatedStationCode, 
									ex
								);
						}
					}
				}
		);
	}

	private void checkSecondStationForBusTransmittionOnComplete(
			LbiTripInvestigationProcessData processData,
			ArrayList<ArrayList<ILbiStationArrivalsDataResponse>> responseList
	) throws LbiInternalException 
	{
		// find bus on responseList
		if (processData.getTripKey() == null) {
			Lg.lgr.error("shouldn't be called with no tripkey. processData:{}", processData);
			return;
		}
		ILbiStationArrivalsDataResponse stationArrivalData = LbiSiriResponseUtils.getRelevantStationArrivalData(responseList, processData.getTripKey(), processData.getRouteInvestigation().getGtfsRouteIdFk());
		// if found
		if (stationArrivalData != null) {
		// check if transsmitting
			if (stationArrivalData.getVehicleLatitude() != null) {
				Lg.lgr.debug("bus IS transmitting (checked from second station), processData:{}, stationArrivalData:{}", processData,  stationArrivalData);
				m_dal.updateTripInvestigationBusWasDetectedNearDepartureStation(processData.getTripInvestigationId(),true);
			} else {
				Lg.lgr.debug("bus is NOT transmitting (checked from second station). clearing actual departure time. processData:{}, stationArrivalData:{}", processData,  stationArrivalData);
				m_dal.updateTripInvestigationActualDepartureTimeAndStatusToStarted(processData.getTripInvestigationId(), null);
				m_dal.updateTripInvestigationBusWasDetectedNearDepartureStation(processData.getTripInvestigationId(),false);
			}
		} else {
			Lg.lgr.debug("NO ETA for bus from second station. processData:{}, stationArrivalData:null", processData);
			m_dal.updateTripInvestigationBusWasDetectedNearDepartureStation(processData.getTripInvestigationId(),false);
		}
		//updateProcessDataOfTripInvestigationKey(processData); //process data doesn't hold "vehicle detection on first stop" 

	}

	private void lookAndProcessNextCommingBus(LbiTripInvestigationProcessData processData) throws LbiInternalException 
	{
		// Determine stationCode for SIRI input
		// if first station
			// SIRI-> look for the next 2 coming departures
		// if last station
			// SIRI-> look for all coming arrivals
			// Determine the relevant arrival by the "trip key" (aimed departure time)  
		
		// if first station and first check try
			// mark the aimed departure time - this is our trip key 
			// if route has enough samples for this time frame (count also trip investigation that are under process now -1)
				// check when is the next required time frame without enough samples
				// recall (delay till the next time frame (minus 20 seconds))
				// return
		//!need more samples!
		// if first station
			// if first check try 
				// create trip investigation record
			// if in station (check vehicle_at_stop)
				// marked "last time bus was at departure station"
				// recall (30 sec delay) (wait for departure)
			// else not in station - not arrived yet
				// recall (delay:(expected arrival time of bus / 2.5) but at least 30 seconds.)
			// if exited station (the ETA is for the next bus = trip key changed)
				// mark departure time between now and "last time bus was at departure station"
				// recall for the last station (delay 1 minute)
				// recall- lunch new trip investigation for the next bus (delay: next bus expected arrival / 2) 
		// if last station
			// if first check try 
				// update trip_investigations with expected_arrival_time, expected_seconds_duration
			// if arrived (1.check vehicle_at_stop 2. Or the ETA is for the next bus 3. Or distance is under x meters)
				// update trip investigation record with all data
				// (no need to lunch new trip investigation for the next bus since we did this on the first station departure 
			// else if not arrived
				// recall (delay:(expected arrival time of bus / 2.5) but at least 30 seconds.)
  

		//get the required stationCode to check 
		int investigatedStationCode = processData.getInvestigatedStationCode();
		
//		ILbiSiriWebServiceClient siriClient = LbiSiriClientFactory.createLbiSiriWebServiceAsyncClient();
//		try {
//			siriClient.connect();
//		} catch (LbiNetworkFaliureException ex) {
//			throw Lg.lgr.throwing(new LbiInternalException(ex));
//		}
		//for (Integer stationCode:stationCodes){
		processData.setBetweenThreads(true);
		siriClient.requestStationArrivalsData(
				investigatedStationCode,
				processData.getRouteInvestigation().getGtfsRouteIdFk(),
				(processData.isFirstStation() ?
						1 // for first station we are looking for what is coming next 
						: 
						99 // for last station we look for all of the next coming buses 
				), 
				new ILbiStationArrialsDataResponseEvent() {
					@Override
					public void onFailure(Exception ex) {
						processData.setBetweenThreads(false);
						processData.markNetworkFailure();
						Lg.lgr.warn("failed on:routeInvestigationId:{}, stationCode:{}, failCount:{}", processData.getRouteInvestigation().getRouteInvestigationId(), investigatedStationCode, processData.getNetworkFailureCount(), ex);
						try {
							int secondsToWaitOnNetwrokFailure = getSecondsToWaitOnNetwrokFailure(processData);
							continueInvestigateRouteStatisticalTripsDurations(processData, secondsToWaitOnNetwrokFailure);
						} catch (LbiAbortProcessing ex2) {
							Lg.lgr.error("canceling this trip investigation after networkfailure. routeInvestigationId:{}, stationCode:{}, failCount:{}, tripInvestigationId:{}", 
										processData.getRouteInvestigation().getRouteInvestigationId(), 
										investigatedStationCode, 
										processData.getNetworkFailureCount(), 
										processData.getTripInvestigationId(), 
										ex2
									);
							if (processData.getTripInvestigationId() != null) {
								try {
									Lg.lgr.fatal("process aborted. processData:{}", processData);
									m_dal.updateTripInvestigationStatus(
											processData.getTripInvestigationId(),
											ELbiTripInvestigationStatuses.ABORTED.getId()
									);
								} catch (Exception ex3) {
									Lg.lgr.error("failed to mark as failed. routeInvestigationId:{}, stationCode:{}, failCount:{}, tripInvestigationId:{}", 
											processData.getRouteInvestigation().getRouteInvestigationId(), 
											investigatedStationCode, 
											processData.getNetworkFailureCount(), 
											processData.getTripInvestigationId(), 
											ex3
										);
								}
								try {
									markTripInvestigationEnded(processData);
								} catch (Exception ex3) {
									Lg.lgr.error("processData:{}",processData, ex3);
								}
							}
						}
					}
					
					@Override
					public void onComplete(ArrayList<ArrayList<ILbiStationArrivalsDataResponse>> responseList) {
						try {
							processData.setBetweenThreads(false);
							lookAndProcessNextCommingBusOnSiriResponse(processData, responseList);
						} catch (Exception ex) {
							Lg.lgr.error("failed to process SIRI response. processData:{}, stationCode:{}", 
									processData,
									investigatedStationCode, 
									ex
								);
							if (processData.getTripInvestigationId() != null) {
								try {
									m_dal.updateTripInvestigationStatus(
											processData.getTripInvestigationId(),
											ELbiTripInvestigationStatuses.APPLICATION_ERROR.getId()
									);
								} catch (Exception ex2) { Lg.lgr.error("error in dal update as failed",ex2);}
							}

						}
					}

				}
		);
	}


	protected int getSecondsToWaitOnNetwrokFailure(LbiTripInvestigationProcessData processData) throws LbiAbortProcessing 
	{
		if (processData.isBeforeTripInvestigationStart()) {
			if(processData.getNetworkFailureCount() <= 1) { 
				return 30;
			} else if(processData.getNetworkFailureCount() <= 3) {
				return 90;
			} else if(processData.getNetworkFailureCount() <= 5) {
				return 5*60;
			} else if(processData.getNetworkFailureCount() <= 10) {
				return 15*60;
			} else if(processData.getNetworkFailureCount() <= 20) {
				return 30*60;
			} else {
				throw new LbiAbortProcessing();
			}
		} else {//already under processing
			//we can't wait more then one minute otherwise we might get bad results   
			if(processData.getNetworkFailureCount() <= 1) { 
				return 5;
			} else if(processData.getNetworkFailureCount() <= 2) {
				return 10;
			} else if(processData.getNetworkFailureCount() <= 5) {
				return 15;
			} else {
				throw new LbiAbortProcessing();
			}
		}
	}

	private void handleInvestigateRouteStatisticalStationsStops(LbiRouteInvestigation routeInvestigation) 
	{
		// TODO Auto-generated method stub
		
	}

	private void handleInvestigateAllTripDurations(LbiRouteInvestigation routeInvestigation) 
	{
		// TODO Auto-generated method stub
		
	}

	private void handleInvestigateAllStationsStops(LbiRouteInvestigation routeInvestigation) 
	{
		// TODO Auto-generated method stub
	}

	private void handleInvestigateRouteSnapshot(LbiRouteInvestigation routeInvestigation) 
	{
		try {
			startRouteSnapshot(routeInvestigation.getGtfsRouteIdFk(), routeInvestigation.getRouteInvestigationId());
		} catch (LbiInternalException | LbiNotFoundException ex) {
			try {
				Lg.lgr.error("failed to take snapshot. routeInvestigationId:" + routeInvestigation.getRouteInvestigationId(), ex);
				m_dal.updateRouteInvestigationStatus(routeInvestigation.getRouteInvestigationId(),
						ELbiRouteInvestigationStatuses.FAILED.getId());
				// updating status to complete on route inv rec will be done under startRouteSnapshot() function
			} catch (Exception ex2) {
				Lg.lgr.error("failed to update status. routeInvestigationId:" + routeInvestigation.getRouteInvestigationId(), ex2);
			}
		}
		
	}

	@Override
	public void startTripInvestigation() throws LbiInternalException {
		// TODO Auto-generated method stub

	}

	@Override
	public void startRouteInvestigation() throws LbiInternalException {
		// TODO Auto-generated method stub

	}

	@Override
	public void startRouteSnapshot(
			int gtfsRouteId, 
			int routeInvestigationId
	) throws LbiInternalException, LbiNotFoundException
	{
		List<Integer> stationCodes = m_dal.getStationsCodesOfRouteId(gtfsRouteId);
		m_dal.updateRouteInvestigationStatus(routeInvestigationId,ELbiRouteInvestigationStatuses.STARTED_NOT_RUNNING.getId());
//		ILbiSiriWebServiceClient siriClient = LbiSiriClientFactory.createLbiSiriWebServiceAsyncClient();
//		try {
//			siriClient.connect();
//		} catch (LbiNetworkFaliureException ex) {
//			throw Lg.lgr.throwing(new LbiInternalException(ex));
//		}
		//for (Integer stationCode:stationCodes){
			siriClient.requestStationArrivalsData(
					stationCodes,
					gtfsRouteId, 
					1, //only one arrival for each station
					new ILbiStationArrialsDataResponseEvent() {
						@Override
						public void onFailure(Exception ex) {
							Lg.lgr.warn("failed on:gtfsRouteId:" + gtfsRouteId, ", stationCode:" + NtvArraysUtils.toString(stationCodes),  ex);
						}
						
						@Override
						public void onComplete(ArrayList<ArrayList<ILbiStationArrivalsDataResponse>> responseList) {
							try {
								for (ArrayList<ILbiStationArrivalsDataResponse> subResponse: responseList) {
									m_dal.insertStationArrivalsDataResponse(routeInvestigationId, subResponse);
								}
								m_dal.updateRouteInvestigationStatus(routeInvestigationId,
										ELbiRouteInvestigationStatuses.COMPLETED.getId());
							} catch (Exception ex) {
								Lg.lgr.error("failed to insert on:gtfsRouteId:" + gtfsRouteId, ", stationCode:" + NtvArraysUtils.toString(stationCodes),  ex);
								try {
									m_dal.updateRouteInvestigationStatus(routeInvestigationId,
											ELbiRouteInvestigationStatuses.FAILED.getId());
								} catch (LbiInternalException | LbiNotFoundException ex2) {
									Lg.lgr.error("failed to update status. routeInvestigationId:" + routeInvestigationId, ex2);
								}
							}
						}
					}
			);
			//try {Thread.sleep(100);} catch (Exception ex) {}
		//}
	}

	@Override
	public void shutdown() {
		LbiScheduledThreadsPoolMgr.shutdown();
	}


}
