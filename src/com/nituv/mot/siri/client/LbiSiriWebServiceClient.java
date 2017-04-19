package com.nituv.mot.siri.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import com.nituv.common.exceptions.LbiInternalException;
import com.nituv.common.exceptions.LbiInvalidDataException;
import com.nituv.common.util.NtvArraysUtils;
import com.nituv.lbi.inf.Lg;
import com.nituv.mot.siri.client.response.LbiSiriResponseParser;

public class LbiSiriWebServiceClient implements ILbiSiriWebServiceClient {

//	private String m_webServiceServerName = "localhost";
	private String m_webServiceServerName = "siri.motrealtime.co.il";
	private String m_webServiceUrlString = "http://" + m_webServiceServerName + ":8081/Siri/SiriServices";
	private CloseableHttpClient m_httpclient = null;
	
	@Override
	public void connect() throws LbiNetworkFaliureException {
		if (m_httpclient == null) {
	        PoolingHttpClientConnectionManager poolingMgr = 
	        		new PoolingHttpClientConnectionManager(5000,TimeUnit.MILLISECONDS);
			m_httpclient = HttpClients.custom().setConnectionManager(poolingMgr).build();
		}
		
	}

	@Override
	public void disconnect() throws LbiInternalException {
		if (m_httpclient != null) {
			m_httpclient = null;
		}
	}

	@Override
	public void requestStationArrivalsData(List<Integer> stationIds, int lineCode,int countOfArrivalsToFetch, ILbiStationArrialsDataResponseEvent event) 
	{
		HttpPost hp = new HttpPost(m_webServiceUrlString);
		hp.addHeader("Content-Type", "text/xml; charset=utf-8");
		hp.addHeader("Content-Type", "Accept");
		StringEntity entity = new StringEntity(LbiSiriRequestBuilder.buildRequest(stationIds, lineCode,countOfArrivalsToFetch),"utf-8"); 
		hp.setEntity(entity);
		
		CloseableHttpResponse response = null;
		try {
			response = m_httpclient.execute(hp);
		    HttpEntity entity2 = response.getEntity();
		    try {
//				System.out.println(EntityUtils.toString(entity2));
		    	Lg.lgr.trace("fetching data for: stationIds=({}), lineCode={}",NtvArraysUtils.toString(stationIds),lineCode);
		    	ArrayList<ArrayList<ILbiStationArrivalsDataResponse>> stationsArrivalsList;
				try {
					stationsArrivalsList = LbiSiriResponseParser.parseForStationArrivalsData(entity2.getContent());
					event.onComplete(stationsArrivalsList);
				} catch (LbiInvalidDataException ex) {
					Lg.lgr.error("message is invalid: stationIds=({}), lineCode={}",NtvArraysUtils.toString(stationIds),lineCode,ex);
					event.onFailure(ex);
				}
			} catch (LbiInternalException | UnsupportedOperationException | IOException ex) {
				Lg.lgr.error(ex);
				event.onFailure(ex);
			}
			
		} catch (ClientProtocolException ex) {
			Lg.lgr.error(ex);
			event.onFailure(ex);
		} catch (IOException ex) {
			Lg.lgr.error(ex);
			event.onFailure(ex);
		} finally {
			if (response != null) {
				try {
					response.close();
				} catch (Exception ex) {
					Lg.lgr.error("fail to close.",ex);
				}
			}
		}


	}
	
	@Override
	public void requestStationArrivalsData(int stationId, int lineCode,int countOfArrivalsToFetch, ILbiStationArrialsDataResponseEvent event) {
		HttpPost hp = new HttpPost(m_webServiceUrlString);
		hp.addHeader("Content-Type", "text/xml; charset=utf-8");
		hp.addHeader("Content-Type", "Accept");
		StringEntity entity = new StringEntity(LbiSiriRequestBuilder.buildRequest(stationId, lineCode, countOfArrivalsToFetch),"utf-8"); 
		hp.setEntity(entity);
		
		CloseableHttpResponse response = null;
		try {
			response = m_httpclient.execute(hp);
		    HttpEntity entity2 = response.getEntity();
		    try {
//				System.out.println(EntityUtils.toString(entity2));
		    	Lg.lgr.trace("fetching data for: stationId={}, lineCode={}",stationId,lineCode);
		    	ArrayList<ArrayList<ILbiStationArrivalsDataResponse>> stationsArrivalsList;
				try {
					stationsArrivalsList = LbiSiriResponseParser.parseForStationArrivalsData(entity2.getContent());
					event.onComplete(stationsArrivalsList);
				} catch (LbiInvalidDataException ex) {
					Lg.lgr.error("message is invalid: stationId={}, lineCode={}",stationId,lineCode,ex);
					event.onFailure(ex);
				}
			} catch (LbiInternalException | UnsupportedOperationException | IOException ex) {
				Lg.lgr.error(ex);
				event.onFailure(ex);
			}
			
		} catch (ClientProtocolException ex) {
			Lg.lgr.error(ex);
			event.onFailure(ex);
		} catch (IOException ex) {
			Lg.lgr.error(ex);
			event.onFailure(ex);
		} finally {
			if (response != null) {
				try {
					response.close();
				} catch (Exception ex) {
					Lg.lgr.error("fail to close.",ex);
				}
			}
		}

	}

}
