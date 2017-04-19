package com.nituv.mot.siri.client.response;

import java.util.List;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

@Root(strict=false)
@Element(name="Envelope",required=false)
@Namespace(prefix="S", reference="http://schemas.xmlsoap.org/soap/envelope/")
public class Envelope {
	
	@Namespace(prefix="ns3", reference="http://www.siri.org.uk/siri")
	@Path("Body/GetStopMonitoringServiceResponse/Answer")
//	@Element(name="StopMonitoringDelivery")
	@ElementList(entry="StopMonitoringDelivery",required=false,inline=true, type=StopMonitoringDelivery.class)
	public List<StopMonitoringDelivery> smdList;
	
//	public StopMonitoringDelivery smd;

}
