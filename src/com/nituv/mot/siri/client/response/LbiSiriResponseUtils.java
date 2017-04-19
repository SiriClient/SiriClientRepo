package com.nituv.mot.siri.client.response;

import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.Level;

import com.nituv.common.exceptions.LbiInternalException;
import com.nituv.common.util.NtvArraysUtils;
import com.nituv.common.util.NtvDateUtils;
import com.nituv.lbi.inf.Lg;
import com.nituv.mot.siri.client.ILbiStationArrivalsDataResponse;

public class LbiSiriResponseUtils {
	static public  ILbiStationArrivalsDataResponse getFirstStationArrivalData(
			ArrayList<ArrayList<ILbiStationArrivalsDataResponse>> responseList) throws LbiInternalException 
	{
		if (responseList == null || responseList.size() == 0 ) {
			throw Lg.lgr.throwing(Level.ERROR, new LbiInternalException("responseList == null || responseList.size() == 0."));
		}
		ArrayList<ILbiStationArrivalsDataResponse> innerList = responseList.get(0);
		if (innerList == null || innerList.size() == 0 ) {
			throw Lg.lgr.throwing(Level.ERROR, new LbiInternalException("innerList == null || innerList.size() == 0."));
		}
		ILbiStationArrivalsDataResponse retVal = innerList.get(0);
		if (retVal == null) {
			throw Lg.lgr.throwing(Level.ERROR, new LbiInternalException("innerList.get(0) == null"));
		}

		return retVal;
	}

	public static ILbiStationArrivalsDataResponse getRelevantStationArrivalData(
			ArrayList<ArrayList<ILbiStationArrivalsDataResponse>> responseList, 
			Date expectedDepartureTime, int gtfsRouteId
	) {
		if (NtvArraysUtils.isEmpty(responseList)) {
			return null;
		}
		if (expectedDepartureTime == null) { // no trip key (expectedDepartureTime) - get the first station arrival data
			return getFirstActualStationArrivalData(responseList,gtfsRouteId);
		}
		ArrayList<ILbiStationArrivalsDataResponse> innerList = responseList.get(0);
		if (responseList.size() > 1) {
			Lg.lgr.warn("responseList.size:{} > 1; responseList:{}", responseList.size(), ArrayUtils.toString(responseList)); 
		}

		 // we have a trip key (expectedDepartureTime)
		// search for station arrival data with 
		ILbiStationArrivalsDataResponse stationArrivalData;
		for (int i=0; i<innerList.size();i++)  
		{
			stationArrivalData = innerList.get(i);
			if (stationArrivalData.getOriginAimedDepartureTime() == null || stationArrivalData.getLineRef() == null) {
				Lg.lgr.debug("stationArrivalData.getOriginAimedDepartureTime() == null || stationArrivalData.getLineRef() == null. arrivalsData:{}", stationArrivalData);
				continue;
			}
			if (expectedDepartureTime.equals(stationArrivalData.getOriginAimedDepartureTime())
				&& stationArrivalData.getLineRef().equals(gtfsRouteId)
			) {
				Lg.lgr.debug("we have a match!. arrivalsData:{}", stationArrivalData);
				return stationArrivalData;
			}
		}
		Lg.lgr.debug("we got {} station arrival data from SIRI. nothing matched out trip key:{}. arrivalsData:{}. probably the bus left the station and we get ETA for the next bus. ",
				innerList.size(),
				NtvDateUtils.formatDateUtc(expectedDepartureTime),
				ArrayUtils.toString(responseList)
		);
		return null;
	}

	/**
	 * return the first station arrival that is in the future 
	 * @param responseList
	 * @param gtfsRouteId 
	 * @return
	 */
	public static ILbiStationArrivalsDataResponse getFirstActualStationArrivalData(
			ArrayList<ArrayList<ILbiStationArrivalsDataResponse>> responseList, 
			int gtfsRouteId
	) {
		if (NtvArraysUtils.isEmpty(responseList)) {
			return null;
		}
		ArrayList<ILbiStationArrivalsDataResponse> innerList = responseList.get(0);
		if (responseList.size() > 1) {
			Lg.lgr.warn("responseList.size:{} > 1; responseList:{}", responseList.size(), ArrayUtils.toString(responseList)); 
		}
		Date currentDatePluse = DateUtils.addMinutes(new Date(), 2);
		
		// search for station arrival data with 
		ILbiStationArrivalsDataResponse stationArrivalData;
		for (int i=0; i<innerList.size();i++)  
		{
			stationArrivalData = innerList.get(i);
			if (stationArrivalData == null) {
				Lg.lgr.warn("stationArrivalData == null.  responseList:{}", ArrayUtils.toString(responseList));
			} else if (stationArrivalData.getLineRef() == null) {
				Lg.lgr.warn("stationArrivalData.getLineRef == null.  stationArrivalData:{}", stationArrivalData);
			} else if (!stationArrivalData.getLineRef().equals(gtfsRouteId)) {
				Lg.lgr.warn("(stationArrivalData.getLineRef:{} != gtfsRouteId:{}.  stationArrivalData:{}", stationArrivalData.getLineRef(), gtfsRouteId, stationArrivalData);
			} else if (stationArrivalData.getOriginAimedDepartureTime() == null) {
				Lg.lgr.warn("stationArrivalData.getOriginAimedDepartureTime() == null.  stationArrivalData:{}", stationArrivalData);
			} else if (currentDatePluse.before(stationArrivalData.getOriginAimedDepartureTime())) {
				return stationArrivalData;
			}
		}
		Lg.lgr.debug("we got {} station arrival data from SIRI. nothing is later then now (pluse 2 minutes). arrivalsData:{}.",
				innerList.size(),
				ArrayUtils.toString(responseList)
		);
		return null;
	}
}
