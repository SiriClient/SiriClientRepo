package com.nituv.lbi;

public enum ELbiRouteInvestigationTypes {
	STATISTICAL_TRIPS_DURATIONS,
	STATISTICAL_STATIONS_STOPS,
	ALL_TRIP_DURATIONS,
	ALL_STATIONS_STOPS,
	ROUTE_SNAPSHOT;
	
	public int getId() {
		return (ordinal() + 1);
	}

	public static ELbiRouteInvestigationTypes valueOf(int id) {
		if (id < 0 || id > ELbiRouteInvestigationTypes.values().length) {
			return null;
		}
		return ELbiRouteInvestigationTypes.values()[id-1];
	}
}
