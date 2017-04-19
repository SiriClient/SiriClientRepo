package com.nituv.mot.siri.client.response;

import java.util.List;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;

@Root(strict=false)//,name="StopMonitoringDelivery")
@Element(name="StopMonitoringDelivery",required=false)
@Namespace(prefix="ns3", reference="http://www.siri.org.uk/siri")
public class StopMonitoringDelivery {

	@Element(name="ResponseTimestamp",required=false)
	String ResponseTimestamp;

	@Element(name="Status",required=false)
	Boolean Status;
	
	@ElementList(entry="MonitoredStopVisit",required=false,inline=true, type=MonitoredStopVisit.class)
	List<MonitoredStopVisit> list;
	
//	@Element(name="MonitoredStopVisit",required=false)
//	MonitoredStopVisit list;

	public String toString() {
		return "responseTimestamp:" + ResponseTimestamp
				+ " status=" + Status
				+ " MonitoredStopVisit=" + list.iterator().next().toString();
	}
}


