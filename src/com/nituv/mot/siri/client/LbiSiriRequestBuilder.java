package com.nituv.mot.siri.client;

import java.util.Date;
import java.util.List;

import com.nituv.common.util.NtvArraysUtils;
import com.nituv.common.util.NtvDateUtils;

class LbiSiriRequestBuilder {
	private static String REQUESTOR_REF = "DS223400";
	private static String REQUESTOR_REF_NUM = "220345344";
	private static String HEADER =
			"<?xml version=\"1.0\"?>\r\n"
			+ "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:acsb=\"http://www.ifopt.org.uk/acsb\" xmlns:datex2=\"http://datex2.eu/schema/1_0/1_0\" xmlns:ifopt=\"http://www.ifopt.org.uk/ifopt\" xmlns:siri=\"http://www.siri.org.uk/siri\" xmlns:siriWS=\"http://new.webservice.namespace\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"./siri\">\r\n"
				    + "<SOAP-ENV:Header />\r\n"
				    + "<SOAP-ENV:Body>\r\n"
				        + "<siriWS:GetStopMonitoringService>\r\n"
//				            + "<Request xsi:type=\"siri:ServiceRequestStructure\">\r\n";
			;
	
	
	private static String FOOTER =
//			            "</Request>\r\n"
			        "</siriWS:GetStopMonitoringService>\r\n"
			    + "</SOAP-ENV:Body>\r\n"
			+ "</SOAP-ENV:Envelope>\r\n";
	
	static String buildRequest(List<Integer> monitoringRef,int lineCode, int maximumStopVisits) 
	{
		StringBuffer requestStrBuf = new StringBuffer();
		requestStrBuf.append(HEADER);
		addRequests(requestStrBuf, monitoringRef,lineCode, maximumStopVisits);
		requestStrBuf.append(FOOTER);
		return requestStrBuf.toString();
	}
	
	static String buildRequest(List<Integer> monitoringRef,int lineCode) {
		int maximumStopVisits = 1;
		return buildRequest(monitoringRef,lineCode,maximumStopVisits);
	}	

	static String buildRequest(int monitoringRef,int lineCode, int maximumStopVisits) {
		return buildRequest(NtvArraysUtils.createArrayList(monitoringRef), lineCode, maximumStopVisits);
	}

	static String buildRequest(int monitoringRef, int maximumStopVisits) {
		return buildRequest(NtvArraysUtils.createArrayList(monitoringRef), -1, maximumStopVisits);
	}
	
	private static void addRequests(
			StringBuffer strBuf,
			List<Integer> monitoringRefs,
			int lineCode,
			int maximumStopVisits
			) //throws LbiInternalException
	{
		strBuf.append("<Request xsi:type=\"siri:ServiceRequestStructure\">\r\n");
		String nowRequestString = "<siri:RequestTimestamp>" 
				+ NtvDateUtils.formatSoapDateOfNow()
				+ "</siri:RequestTimestamp>\r\n";
		strBuf.append(nowRequestString);
		strBuf.append("<siri:RequestorRef xsi:type=\"siri:ParticipantRefStructure\">" + REQUESTOR_REF + "</siri:RequestorRef>\r\n");
		strBuf.append("<siri:MessageIdentifier xsi:type=\"siri:MessageQualifierStructure\">" + REQUESTOR_REF_NUM + ":"  + new Date().getTime() + "</siri:MessageIdentifier>\r\n");		
		for (int i=0; i < monitoringRefs.size(); i++) 
		{
			strBuf.append("<siri:StopMonitoringRequest version=\"IL2.7\" xsi:type=\"siri:StopMonitoringRequestStructure\">\r\n");
			strBuf.append(	nowRequestString);
			strBuf.append(	"<siri:MessageIdentifier xsi:type=\"siri:MessageQualifierStructure\">");
				strBuf.append(i);
					strBuf.append("</siri:MessageIdentifier>");
			strBuf.append(	"<siri:PreviewInterval>PT1H</siri:PreviewInterval>");
			strBuf.append(	"<siri:MonitoringRef xsi:type=\"siri:MonitoringRefStructure\">" + monitoringRefs.get(i) + "</siri:MonitoringRef>");
			if (lineCode != -1) {
				strBuf.append("<siri:LineRef>" + lineCode + "</siri:LineRef>");
			}
			strBuf.append(	"<siri:MaximumStopVisits>" + maximumStopVisits + "</siri:MaximumStopVisits>");
			strBuf.append("</siri:StopMonitoringRequest>");
		}
		strBuf.append("</Request>\r\n");
	}
}
