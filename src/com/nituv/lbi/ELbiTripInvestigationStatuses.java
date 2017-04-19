package com.nituv.lbi;

public enum ELbiTripInvestigationStatuses {
	NOT_STARTED, //1
	STARTED, //2
	COMPLETED, //3
	NETWORK_ERROR, //4
	ABORTED, //5
	APPLICATION_ERROR, //6
	NO_VEHICLE_TRANSMITION_ON_DEPARTURE, //7
	NO_VEHICLE_TRANSMITION_ON_ARRIVAL, //8
	MISSING_GTFS_DATA, //9
	NO_SIRI_DATA_ON_ARRIVAL_STATION, //10
	TIMEOUT, //11
	;
	
	public int getId() {
		return (ordinal() + 1);
	}

	public static ELbiTripInvestigationStatuses valueOf(int id) {
		if (id < 0 || id > ELbiTripInvestigationStatuses.values().length) {
			return null;
		}
		return ELbiTripInvestigationStatuses.values()[id-1];
	}
	
}
