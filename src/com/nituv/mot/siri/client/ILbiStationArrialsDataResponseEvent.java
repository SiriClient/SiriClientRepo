package com.nituv.mot.siri.client;

import java.util.ArrayList;

public interface ILbiStationArrialsDataResponseEvent {
	public void onComplete(ArrayList<ArrayList<ILbiStationArrivalsDataResponse>> stationsArrivalsList);
	public void onFailure(Exception ex);
}
