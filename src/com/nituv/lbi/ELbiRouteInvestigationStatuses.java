package com.nituv.lbi;

public enum ELbiRouteInvestigationStatuses {
	NOT_STARTED,
	STARTED_NOT_RUNNING,
	WAITING_TO_NEXT_TIME_FRAME,
	RUNNING,
	COMPLETED,
	FAILED, 
	NO_SCHEDULED_TRIPS_BY_GTFS_DATA
	;
	
	public int getId() {
		return (ordinal() + 1);
	}

	public static ELbiRouteInvestigationStatuses valueOf(int id) {
		if (id < 0 || id > ELbiRouteInvestigationStatuses.values().length) {
			return null;
		}
		return ELbiRouteInvestigationStatuses.values()[id-1];
	}
}
