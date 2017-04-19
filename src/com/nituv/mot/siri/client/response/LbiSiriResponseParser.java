package com.nituv.mot.siri.client.response;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import com.nituv.common.exceptions.LbiInternalException;
import com.nituv.common.exceptions.LbiInvalidDataException;
import com.nituv.lbi.inf.Lg;
import com.nituv.mot.siri.client.ILbiStationArrivalsDataResponse;

public class LbiSiriResponseParser 
{
	
	public static ArrayList<ArrayList<ILbiStationArrivalsDataResponse>> parseForStationArrivalsData(InputStream is) 
			throws LbiInternalException, LbiInvalidDataException
	{
		
		Serializer serializer = new Persister();
//		LbiStationArrialsDataResponse responseData = null;
		ArrayList<ArrayList<ILbiStationArrivalsDataResponse>> returnedList = null; 
		try {
//			File f = new File("C:\\Users\\SHACHAR\\Desktop\\to_delete\\smd.xml");
//			LbiStopMonitoringDelivery smd = serializer.read(LbiStopMonitoringDelivery.class, f);
//			StopMonitoringDelivery smd = serializer.read(StopMonitoringDelivery.class, is);
			BufferedReader br =  new BufferedReader(new InputStreamReader(is));
			/*if (Lg.lgr.isDebugEnabled()) 
			{
				br.mark(1);
			}*/
			Envelope envelope = serializer.read(Envelope.class, br);
			/*if (Lg.lgr.isDebugEnabled()) 
			{
				br.reset();
				StringBuffer sb = new StringBuffer();
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				Lg.lgr.trace("siri stream content:" + sb.toString());
			}*/
			List<StopMonitoringDelivery> messages = envelope.smdList;
			returnedList = new ArrayList<ArrayList<ILbiStationArrivalsDataResponse>>(messages.size());
			ArrayList<ILbiStationArrivalsDataResponse> subList;
			for (StopMonitoringDelivery message: messages) {
				if (message == null || message.Status == false) {
					//throw Lg.lgr.throwing(Level.WARN, new LbiInvalidDataException("message is null or status=false."));
					Lg.lgr.trace("message is null or status=false.");
	//				m_isMessageValid = false;
					subList = new ArrayList<ILbiStationArrivalsDataResponse>(0);
				} else {
	//				m_isMessageValid = true;
					if (message.list == null || message.list.size() ==  0) {
	//					m_hasMonitoredStopVisitData = false;
						subList = new ArrayList<ILbiStationArrivalsDataResponse>();
//						returnedList = new ArrayList<ILbiStationArrivalsDataResponse>(0);
					} else {
						subList = new ArrayList<ILbiStationArrivalsDataResponse>(message.list.size());
						for (MonitoredStopVisit stationData: message.list) 
						{
							subList.add(new LbiStationArrialsDataResponse(stationData));
						}
	//					m_stationData = message.list.iterator().next();
	//					m_hasMonitoredStopVisitData = true;
	//					if (m_stationData.MonitoringRef != m_stationData.StopPointRef) 
	//					{
	//						Lg.lgr.warn("m_stationData.MonitoringRef:{} != m_stationData.StopPointRef:{}", m_stationData.MonitoringRef, m_stationData.StopPointRef);
	//					}
						
					}
				}
				returnedList.add(subList);
			}
			
			
//			responseData = new LbiStationArrialsDataResponse(envelope.smd);
//			System.out.println(envelope.smd.toString());
		} catch (Exception ex) {
			Lg.lgr.error(ex);
			throw new LbiInternalException(ex);
		}
		return returnedList;
	}
	
}
