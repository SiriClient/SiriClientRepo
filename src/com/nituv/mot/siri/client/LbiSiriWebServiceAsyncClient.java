package com.nituv.mot.siri.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;

import com.nituv.common.exceptions.LbiCanceledException;
import com.nituv.common.exceptions.LbiInternalException;
import com.nituv.common.exceptions.LbiInvalidDataException;
import com.nituv.common.util.NtvArraysUtils;
import com.nituv.lbi.inf.Lg;
import com.nituv.mot.siri.client.response.LbiSiriResponseParser;

class LbiSiriWebServiceAsyncClient implements ILbiSiriWebServiceClient {

	private final String m_webServiceServerName = "siri.motrealtime.co.il";
	private final String m_webServiceUrlString = "http://" + m_webServiceServerName + ":8081/Siri/SiriServices";

	private CloseableHttpAsyncClient m_httpclient = null;
	
	@Override
	public void connect() throws LbiNetworkFaliureException {
		Lg.lgr.info("connecting");
		if (m_httpclient == null) {
//			m_httpclient = HttpAsyncClients.createDefault();
	        RequestConfig requestConfig = RequestConfig.custom()
	                .setSocketTimeout(5000)
	                .setConnectTimeout(5000).build();
//	        PoolingHttpClientConnectionManager poolingMgr = 
//	        		new PoolingHttpClientConnectionManager(5000,TimeUnit.MILLISECONDS);
//	        HttpAsyncClients.createMinimal(poolingMgr);
			m_httpclient = HttpAsyncClients.custom()
	            .setDefaultRequestConfig(requestConfig)
	            .setMaxConnPerRoute(1000)
	            .setMaxConnTotal(1000)
	            .build();
		}
		if (!m_httpclient.isRunning()) {
			m_httpclient.start();
		}
		
	}

	@Override
	public void disconnect() throws LbiInternalException {
		if (m_httpclient.isRunning()) {
			try {
				m_httpclient.close();
			} catch (IOException ex) {
				throw new LbiInternalException(ex);
			}
		}
		if (m_httpclient != null) {
			m_httpclient = null;
		}
	}

	@Override
	public void requestStationArrivalsData(
			int stationId, 
			int lineCode,
			int countOfArrivalsToFetch,
			ILbiStationArrialsDataResponseEvent event
	) {
		requestStationArrivalsData(NtvArraysUtils.createArrayList(stationId), lineCode, countOfArrivalsToFetch, event);
	}
	
	@Override
	public void requestStationArrivalsData(
			List<Integer> stationIds, 
			int lineCode,
			int countOfArrivalsToFetch,
			ILbiStationArrialsDataResponseEvent event
	) {
		HttpPost hp = new HttpPost(m_webServiceUrlString);
		hp.addHeader("Content-Type", "text/xml; charset=utf-8");
		hp.addHeader("Content-Type", "Accept");
		StringEntity entity = new StringEntity(LbiSiriRequestBuilder.buildRequest(stationIds, lineCode,countOfArrivalsToFetch),"utf-8"); 
		hp.setEntity(entity);
		
//		m_httpclient.
		
		m_httpclient.execute(hp, new FutureCallback<HttpResponse>() {
			
			public void completed(final HttpResponse response) {
//				System.out.println(response.getStatusLine());
			    HttpEntity entity2 = response.getEntity();
			    try {
//					System.out.println(EntityUtils.toString(entity2));
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
			}
			
			public void failed(final Exception ex) {
				Lg.lgr.error("failed: stationIds=({}), lineCode={}",NtvArraysUtils.toString(stationIds),lineCode,ex);
				event.onFailure(ex);
			}
			
			public void cancelled() {
				Lg.lgr.error("canceled: stationIds=({}), lineCode={}",NtvArraysUtils.toString(stationIds),lineCode);
				event.onFailure(new LbiCanceledException());
			}
			
		});

	}

}
