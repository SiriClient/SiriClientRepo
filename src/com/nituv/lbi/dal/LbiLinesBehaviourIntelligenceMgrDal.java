package com.nituv.lbi.dal;

import java.sql.Time;
import java.util.Date;
import java.util.List;

import com.nituv.common.exceptions.LbiInternalException;
import com.nituv.common.exceptions.LbiNotFoundException;
import com.nituv.common.util.NtvDBUtils;
import com.nituv.common.util.NtvDateUtils;
import com.nituv.lbi.ELbiRouteInvestigationStatuses;
import com.nituv.lbi.ELbiTripInvestigationStatuses;
import com.nituv.lbi.ILbiLineQueryParameters;
import com.nituv.lbi.ILbiLinesBehaviourIntelligenceMgrDal;
import com.nituv.lbi.dbobjects.LbiRouteInvestigation;
import com.nituv.lbi.dbobjects.LbiTimeAndDaysBitmask;
import com.nituv.lbi.inf.Lg;
import com.nituv.mot.siri.client.ILbiStationArrivalsDataResponse;

public class LbiLinesBehaviourIntelligenceMgrDal implements ILbiLinesBehaviourIntelligenceMgrDal
{

	public List<ILbiLineQueryParameters> getLinesToQuery() 
	{
		return null;
	}

	@Override
	public List<Integer> getStationsCodesOfRouteId(int gtfsRouteId) throws LbiInternalException, LbiNotFoundException {
		//SELECT * FROM route_stations_view WHERE route_id=1100
		List<Integer> list = NtvDBUtils.getIntegerColoumn("route_stations_view", "stop_code", "route_id = " + gtfsRouteId);
		if (list == null || list.size() == 0) {
			throw new LbiNotFoundException();
		}
		return list;
	}

	@Override
	public void insertStationArrivalsDataResponse(
			int routeInvestigationId,
			List<ILbiStationArrivalsDataResponse> arrivalsList
	) throws LbiInternalException 
	{
		StringBuffer insertBuf = new StringBuffer(200 + arrivalsList.size() * 150);
		insertBuf.append("INSERT INTO siri_raw_data");
		insertBuf.append("\n(route_investigation_id_fk, recorded_at_time, monitoring_ref, line_ref, direction_ref, published_line_name, operator_ref, destination_ref, origin_aimed_departure_time, vehicle_longitude, vehicle_latitude, stop_point_ref, expected_arrival_time, vehicle_at_stop, aimed_arrival_time, actual_arrival_time, arrival_status)");
		insertBuf.append("VALUES ");

		boolean isFirst = true;
		for (ILbiStationArrivalsDataResponse arrivalData: arrivalsList)
		{
			if (isFirst) {
				isFirst = false;
			} else {
				insertBuf.append(",");
			}
			addInsertValuesRowForSiriRawDataTable(insertBuf,routeInvestigationId, arrivalData);
		}
		NtvDBUtils.insertOrUpdate(insertBuf.toString());
		
	}

	private void addInsertValuesRowForSiriRawDataTable(
			StringBuffer insertBuf, 
			int routeInvestigationId,
			ILbiStationArrivalsDataResponse arrivalData
	) {
		
		insertBuf.append("(");
		insertBuf.append(routeInvestigationId); insertBuf.append(", ");
		insertBuf.append(NtvDBUtils.dateToSqlString(arrivalData.getRecordedAtTime())); insertBuf.append(", ");
		insertBuf.append(arrivalData.getMonitoringRef()); insertBuf.append(", ");
		insertBuf.append(arrivalData.getLineRef()); insertBuf.append(", '");
		insertBuf.append(arrivalData.getPublishedLineName()); insertBuf.append("', ");
		insertBuf.append(arrivalData.getDirectionRef()); insertBuf.append(", ");
		insertBuf.append(arrivalData.getOperatorRef()); insertBuf.append(", ");
		insertBuf.append(arrivalData.getDestinationRef()); insertBuf.append(", ");
		insertBuf.append(NtvDBUtils.dateToSqlString(arrivalData.getOriginAimedDepartureTime())); insertBuf.append(", ");
		insertBuf.append(arrivalData.getVehicleLongitude()); insertBuf.append(", ");
		insertBuf.append(arrivalData.getVehicleLatitude()); insertBuf.append(", ");
		insertBuf.append(arrivalData.getStopPointRef()); insertBuf.append(", ");
		insertBuf.append(NtvDBUtils.dateToSqlString(arrivalData.getExpectedArrivalTime())); insertBuf.append(", ");
		insertBuf.append(arrivalData.isVehicleAtStop()); insertBuf.append(", ");
		insertBuf.append(NtvDBUtils.dateToSqlString(arrivalData.getAimedArrivalTime())); insertBuf.append(", ");
		insertBuf.append(NtvDBUtils.dateToSqlString(arrivalData.getActualArrivalTime())); insertBuf.append(", '");
		insertBuf.append(arrivalData.getArrivalStatus()); insertBuf.append("'");
		insertBuf.append(")");
	}

	@Override
	public List<LbiRouteInvestigation> getRelevantRouteInvestigations()
			throws LbiInternalException, LbiNotFoundException 
	{
		List<LbiRouteInvestigation> routeInvList = 
				NtvDBUtils.getObjectsList(LbiRouteInvestigation.class, 
						"SELECT *"
								+ ", ARRAY(SELECT stop_code FROM route_stations_view WHERE (stop_sequence = 1 OR stop_sequence = route_stops_count) AND route_id = gtfs_route_id_fk ORDER BY stop_sequence) AS route_investigation_stopcodes"
								//+ ", ARRAY(SELECT stop_code FROM route_investigation_stops WHERE route_investigation_id_fk = route_investigation_id ORDER BY stop_sequence) AS route_investigation_stopcodes"
								//+ ", ARRAY(SELECT route_investigation_stop_id FROM route_investigation_stops WHERE route_investigation_id_fk = route_investigation_id ORDER BY stop_sequence) AS route_investigation_stop_ids"
						+ " FROM route_investigations"
						+ " WHERE route_investigation_status_id_fk <= 2" // 1=not started, 2=started not running 
							+ " AND NOW() between start_date AND end_date"
						+ " ORDER BY priority DESC"
				);
		return routeInvList;
		
	}

	@Override
	public void updateRouteInvestigationStatus(int routeInvestigationId, int statusId)
			throws LbiInternalException, LbiNotFoundException 
	{
		Lg.lgr.trace("updateing routeInvestigationId:" + routeInvestigationId + ", statusId:" + statusId);
		String sql = "update route_investigations SET route_investigation_status_id_fk = " + statusId
				+ " WHERE route_investigation_id = " + routeInvestigationId;
		int effectedCount = NtvDBUtils.insertOrUpdate(sql);
		if (effectedCount == 0) 
		{
			throw new LbiNotFoundException("failed to update, sql: sql");
		}
	}
	
	@Override
	public int updateRouteInvestigationsFromRunningToNotRunning() throws LbiInternalException {
		final String sql = "UPDATE route_investigations SET route_investigation_status_id_fk =" 
				+ ELbiRouteInvestigationStatuses.STARTED_NOT_RUNNING.getId()
				+ " WHERE route_investigation_status_id_fk = "
				+ ELbiRouteInvestigationStatuses.RUNNING.getId();
		return NtvDBUtils.insertOrUpdate(sql);
		
	}

//	@Override
//	public Integer getLastStationCodeOfRoute(
//			int gtfsRouteIdFk
//	) throws LbiInternalException 
//	{
//		return null;
//	}

//	@Override
//	public Integer getFirstStationCodeOfRoute(
//			int gtfsRouteIdFk
//	) throws LbiInternalException {
//		return null;
//	}

	@Override
	public int getTripInvestigationCountForTimeFrame(int routeInvestigationId, Date pointInTime)
			throws LbiInternalException, LbiNotFoundException {
		final StringBuffer sqlBuf = new StringBuffer(1100);
		sqlBuf.append(
				"SELECT DISTINCT (count(actual_departure_time) OVER w)::INT cnt\r\n" //--, DISTINCT start_time, end_time\r\n"
				+ "FROM time_frames\r\n"
				+ "	LEFT JOIN trip_investigations ON actual_departure_time::time BETWEEN start_time AND end_time\r\n"
				+ "		AND actual_departure_time IS NOT NULL\r\n"
				+ "		AND actual_arrival_time IS NOT NULL\r\n"
				+ "		AND route_investigation_id_fk = "); sqlBuf.append(routeInvestigationId);  sqlBuf.append("\r\n"
				+ "WHERE '"); sqlBuf.append(NtvDateUtils.formatTimeShortString(pointInTime)); sqlBuf.append("' BETWEEN start_time AND end_time - interval '1 millisecond'\r\n"
				+ "	AND time_frame_group_id_fk = 1\r\n"
				//+ "	AND (route_investigation_id_fk IS NULL OR route_investigation_id_fk = "); sqlBuf.append(routeInvestigationId);  sqlBuf.append(")\r\n"
				+ "WINDOW w AS (PARTITION BY start_time)");
		
		List<Integer> list = NtvDBUtils.getColumn(Integer.class,sqlBuf.toString());
		if (list.size() == 0)
			throw new LbiNotFoundException(sqlBuf.toString());
		if (list.size() > 1) 
			Lg.lgr.warn("list.size():{} > 1; sql:{}", list.size(),sqlBuf.toString());
		
		if (list.get(0) == null) {
			throw new LbiInternalException("value returned null");
		}
		return list.get(0);
	}
	
	@Override
	public int insertTripInvestigation(
		int routeId,
		int routeInvestigationId, 
		Date aimedDepartureTime,
		boolean busTransmittedNearFirstStation
	) throws LbiInternalException {
		StringBuffer sqlBuf = new StringBuffer(200);
		sqlBuf.append(
				"INSERT INTO trip_investigations (route_investigation_id_fk, expected_depature_time, bus_was_detected_near_departure_station, expected_gtfs_seconds_duration)\r\n" 
						+ "VALUES ("); 
							sqlBuf.append(routeInvestigationId); sqlBuf.append(","); 
							sqlBuf.append(NtvDBUtils.dateToSqlString(aimedDepartureTime)); sqlBuf.append(",");  
							sqlBuf.append(busTransmittedNearFirstStation); 
						sqlBuf.append(",(");
						appendGtfsTripDurationQuery(sqlBuf, routeId, aimedDepartureTime);
						sqlBuf.append("))\r\n");
				sqlBuf.append("RETURNING trip_investigation_id");
		List<Integer> createdPkIds = NtvDBUtils.getColumn(Integer.class, sqlBuf.toString());
		if (createdPkIds == null || createdPkIds.size() == 0 || createdPkIds.get(0) == null) {
			throw new LbiInternalException("createdPkIds == null || createdPkIds.size() == 0 || createdPkIds.get(0) == null");
		}
		return createdPkIds.get(0);
	}

	@Override
	public int updateTripInvestigationActualDepartureTimeAndStatusToStarted(
			int tripInvestigationId, 
			Date departureTime
	) throws LbiInternalException 
	{
		return NtvDBUtils.updateFields(
				"trip_investigations", //table 
				"actual_departure_time, trip_investigation_status_id_fk", //columns
				new Object[]{departureTime, 2}, //2=started
				"trip_investigation_id = " + tripInvestigationId //where
			);
	}

	@Override
	public int updateTripInvestigationExpectedArrivalTimeAndExpectedSecondsDuration(
			int tripInvestigationId,
			Date expectedArrivalTime, 
			int expectedSecondsDuration
	) throws LbiInternalException 
	{
		
		return NtvDBUtils.updateFields(
			"trip_investigations", //table 
			"expected_arrival_time, expected_seconds_duration", //columns
			new Object[]{expectedArrivalTime, expectedSecondsDuration}, //values
			"trip_investigation_id = " + tripInvestigationId //where
		);
		
	}

	@Override
	public int updateTripInvestigationActualArrivalTimeAndActualSecondsDurationAndStatusCompleted(
			int tripInvestigationId, 
			Date actualArrivalTime, 
			int actualSecondsDuration
	)
			throws LbiInternalException 
	{
		return NtvDBUtils.updateFields(
				"trip_investigations", //table 
				"actual_arrival_time, actual_seconds_duration,trip_investigation_status_id_fk", //columns
				new Object[]{actualArrivalTime,actualSecondsDuration,ELbiTripInvestigationStatuses.COMPLETED.getId()}, //values
				"trip_investigation_id = " + tripInvestigationId //where
			);
	}

	@Override
	public void updateTripInvestigationStatus(int tripInvestigationId, int tripInvestigationStatus) throws LbiInternalException {
		// if we'll a status field in the future or we'll decide to put a marker 
		// in one of the fields like: actual_seconds_duration = -1 
		NtvDBUtils.updateField(
				"trip_investigations", //table 
				"trip_investigation_status_id_fk", //column
				tripInvestigationStatus, //value
				"trip_investigation_id = " + tripInvestigationId //where
			);
		
	}

	@Override
	public List<Time> getRequiredTimeFrames(int routeInvestigationId) throws LbiInternalException {
		
		final StringBuffer sqlBuf = new StringBuffer(1100);
		sqlBuf.append(
		"SELECT a.start_time--, CASE WHEN b.cnt IS NULL THEN 0 ELSE b.cnt END AS cnt \r\n"
		+ "FROM\r\n"
		+ "	(SELECT start_time, 0 AS cnt, order_number\r\n"
		+ "	FROM time_frames\r\n"
		+ "	WHERE time_frame_group_id_fk = 1\r\n"
		+ "	ORDER BY order_number\r\n"
		+ "	) as a\r\n"
		+ "LEFT JOIN\r\n"
		+ "	(\r\n"
		+ "	SELECT start_time, count(*) AS cnt, samples_goal_for_time_frame\r\n"
		+ "	FROM time_frames\r\n"
		+ "		LEFT JOIN trip_investigations ON actual_departure_time::time BETWEEN start_time AND end_time\r\n"
		+ "		LEFT JOIN route_investigations ON route_investigation_id = route_investigation_id_fk\r\n"
		+ "	WHERE actual_departure_time IS NOT NULL AND actual_arrival_time IS NOT NULL\r\n"
		+ "		AND time_frame_group_id_fk = 1\r\n"
		+ "		AND route_investigation_id = "); sqlBuf.append(routeInvestigationId);  sqlBuf.append("\r\n"
		+ "	GROUP BY route_investigation_id, time_frame_id, start_time, end_time, order_number, samples_goal_for_time_frame\r\n"
		+ "	) as b ON a.start_time = b.start_time\r\n"
		+ "WHERE b.cnt IS NULL\r\n"
		+ "	OR b.cnt < samples_goal_for_time_frame\r\n"
		+ "ORDER BY order_number");
		
		List<Time> list = NtvDBUtils.getColumn(Time.class,sqlBuf.toString());
		return list;
	}

	@Override
	public List<LbiTimeAndDaysBitmask> getRouteDepartureTimesAndDays(int routeId)
		throws LbiInternalException
	{
		String sql = "SELECT departure_time AS \"time\", days_bitmask"
		+ " FROM gtfs_trips INNER JOIN gtfs_calendar ON gtfs_calendar.service_id = gtfs_trips.service_id"
		+ " WHERE trip_date < NOW() AND route_id = " + routeId
		+ " ORDER BY departure_time";
		
		List<LbiTimeAndDaysBitmask> departureTimesAndDays = 
				NtvDBUtils.getObjectsList(LbiTimeAndDaysBitmask.class, 
						sql
				);
		return departureTimesAndDays;
	}

	@Override
	public int getGtfsTripDuration(Integer gtfsRouteIdFk, Date departureTime)
			throws LbiInternalException, 
			LbiNotFoundException 
	{
		final StringBuffer sqlBuf = new StringBuffer(450);
		appendGtfsTripDurationQuery(sqlBuf,gtfsRouteIdFk, departureTime);
		List<Integer> list = NtvDBUtils.getColumn(Integer.class,sqlBuf.toString());
		if (list.size() == 0)
			throw new LbiNotFoundException(sqlBuf.toString());
		
		if (list.get(0) == null) {
			throw new LbiInternalException("value returned null");
		}
		return list.get(0);
	}

	private void appendGtfsTripDurationQuery(StringBuffer sqlBuf, Integer gtfsRouteIdFk, Date departureTime) 
	{
		String depTimeStr = NtvDateUtils.formatTimeShortString(departureTime);
		sqlBuf.append(
				"SELECT EXTRACT(EPOCH FROM (arrival_time - departure_time))::int as sec_duration\r\n"
				+ "FROM gtfs_trips\r\n"
				+ "	INNER JOIN gtfs_calendar ON gtfs_trips.service_id = gtfs_calendar.service_id\r\n" 
				+ "WHERE route_id = "); sqlBuf.append(gtfsRouteIdFk);  sqlBuf.append("\r\n"
				+ "	AND departure_time BETWEEN '"); sqlBuf.append(depTimeStr); sqlBuf.append("' - interval '10 minutes' AND '"); sqlBuf.append(depTimeStr); sqlBuf.append("' + interval '10 minutes'\r\n"
				+ "	AND (days_bitmask & POWER(2,(EXTRACT (dow FROM NOW())))::int >= 1)\r\n"
				+ "	AND trip_date <= NOW() LIMIT 1");
	}

	@Override
	public int getGtfsRouteDuration(Integer gtfsRouteIdFk) throws LbiInternalException, LbiNotFoundException {
		final StringBuffer sqlBuf = new StringBuffer(450);
		sqlBuf.append(
				"SELECT MAX(EXTRACT(EPOCH FROM (arrival_time - departure_time)))::int as sec_duration\r\n"
				+ "FROM gtfs_trips\r\n"
				+ "WHERE route_id = "); sqlBuf.append(gtfsRouteIdFk);  sqlBuf.append("\r\n"
				+ "	AND trip_date <= NOW()"
		);
		
		List<Integer> list = NtvDBUtils.getColumn(Integer.class,sqlBuf.toString());
		if (list.size() == 0)
			throw new LbiNotFoundException(sqlBuf.toString());
		
		if (list.get(0) == null) {
			throw new LbiInternalException("value returned null");
		}
		return list.get(0);
	}

	@Override
	public int getSecondStationCodeOfRoute(Integer gtfsRouteId) throws LbiInternalException, LbiNotFoundException {
		//SELECT * FROM route_stations_view WHERE route_id=1100
		List<Integer> list = NtvDBUtils.getIntegerColoumn("route_stations_view", "stop_code", "route_id = " + gtfsRouteId + " AND stop_sequence = 2");
		if (list == null || list.size() == 0) {
			throw new LbiNotFoundException();
		}
		if (list.get(0) == null) {
			throw new LbiInternalException("value returned null");
		}
		return list.get(0);
	}

	@Override
	public void updateTripInvestigationBusWasDetectedNearDepartureStation(
			int tripInvestigationId, 
			boolean wasDetected
	) throws LbiInternalException 
	{
		NtvDBUtils.updateField(
				"trip_investigations", //table 
				"bus_was_detected_near_departure_station", //column
				wasDetected, //value
				"trip_investigation_id = " + tripInvestigationId //where
			);
	}

}
