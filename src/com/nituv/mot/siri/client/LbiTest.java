package com.nituv.mot.siri.client;

import java.util.ArrayList;
import java.util.List;

import com.nituv.common.exceptions.LbiInternalException;
import com.nituv.common.exceptions.LbiNotFoundException;
import com.nituv.common.util.NtvDBUtils;
import com.nituv.lbi.ILbiLinesBehaviourIntelligenceMgr;
import com.nituv.lbi.LbiLinesBehaviourIntelligenceFactory;
import com.nituv.lbi.dal.LbiLinesBehaviourIntelligenceMgrDal;
import com.nituv.lbi.inf.Lg;

public class LbiTest {

	public static void main(String[] args) {

		
		
		ILbiLinesBehaviourIntelligenceMgr lbiMgr = LbiLinesBehaviourIntelligenceFactory.createLinesBehaviourIntelligenceMgr();
		
		try {
			lbiMgr.checkDbForNewInvestigations();
			if (1+1 == 2 ) return;
			lbiMgr.startRouteSnapshot(1100, 1);
		} catch (LbiInternalException ex) {
			Lg.lgr.error(ex);
		} catch (LbiNotFoundException ex) {
			Lg.lgr.error(ex);
		}
		if (1+1 == 2 ) return;
		
		LbiSiriWebServiceAsyncClient cl = new LbiSiriWebServiceAsyncClient();
		try {
			//DBUtils.test();
			
			cl.connect();
			int gtfsRouteId = 1100;
			int stationCode = 32434;
			cl.requestStationArrivalsData(
					stationCode,
					gtfsRouteId,
					1,
					new ILbiStationArrialsDataResponseEvent() {
						@Override public void onFailure(Exception ex) 
							{Lg.lgr.warn("failed on:gtfsRouteId:" + gtfsRouteId, ", stationCode:" + stationCode,  ex);}
						@Override public void onComplete(ArrayList<ArrayList<ILbiStationArrivalsDataResponse>> arrivalsList) 
							{Lg.lgr.trace("success: " + arrivalsList);}
					}
			);
//			cl.requestStationArrivalsData(31620,-1);
			
			
			
//			File f = new File("C:\\Users\\SHACHAR\\Desktop\\to_delete\\smd.xml");
//			File f = new File("C:\\Users\\SHACHAR\\Desktop\\to_delete\\full_envelope.xml");
//			
//			Serializer serializer = new Persister();
//			Envelope envelope = serializer.read(Envelope.class, f);
//			System.out.println(envelope.smd.toString());

//		} catch (LbiNetworkFaliureException e) {
//			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
