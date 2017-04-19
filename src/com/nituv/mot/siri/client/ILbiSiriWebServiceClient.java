package com.nituv.mot.siri.client;

import java.util.List;

import com.nituv.common.exceptions.LbiInternalException;

public interface ILbiSiriWebServiceClient {
	
	public void connect() throws LbiNetworkFaliureException;
	public void disconnect() throws LbiInternalException;
	
	public void requestStationArrivalsData(
			int stationId, 
			int lineCode,
			int countOfArrivalsToFetch, 
			ILbiStationArrialsDataResponseEvent event
	);
	public void requestStationArrivalsData(
			List<Integer> stationIds, 
			int lineCode,
			int countOfArrivalsToFetch, 
			ILbiStationArrialsDataResponseEvent event
	);

}
