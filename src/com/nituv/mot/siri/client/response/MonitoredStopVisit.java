package com.nituv.mot.siri.client.response;

import java.util.Date;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

import com.nituv.common.util.NtvDateUtils;

@Root(strict=false)
@Element(name="MonitoredStopVisit",required=false)
@Namespace(prefix="ns3", reference="http://www.siri.org.uk/siri")
public class MonitoredStopVisit {
	//MonitoredStopVisit
	@Element(required=false)
	String RecordedAtTime;
	@Element(required=false)
	Integer ItemIdentifier; // unique message code
	@Element(required=false)
	Integer MonitoringRef; // station code
	
	/////////MonitoredVehicleJourney
	@Element(required=false)
	@Path("MonitoredVehicleJourney")
	Integer LineRef;
	
	@Element(required=false)
	@Path("MonitoredVehicleJourney")
	Integer DirectionRef;
	
	@Element(required=false)
	@Path("MonitoredVehicleJourney")
	String PublishedLineName;
	
	@Element(required=false)
	@Path("MonitoredVehicleJourney")
	Integer OperatorRef;
	
	@Element(required=false)
	@Path("MonitoredVehicleJourney")
	Integer DestinationRef;
	
	@Element(required=false)
	@Path("MonitoredVehicleJourney")
	String OriginAimedDepartureTime;
	
	///////////////VehicleLocation
	
	@Path("MonitoredVehicleJourney/VehicleLocation")
	@Element(name="Longitude")
	Double  vehicleLongitude;
	
	@Path("MonitoredVehicleJourney/VehicleLocation")
	@Element(name="Latitude")
	Double  vehicleLatitude;
	
	///////////////MonitoredCall
	@Element(required=false)
	@Path("MonitoredVehicleJourney/MonitoredCall")
	Integer StopPointRef; //station code
	
	@Element(required=false)
	@Path("MonitoredVehicleJourney/MonitoredCall")
	String ExpectedArrivalTime; // deprecated (supports ver 2.6) when bus didn't left first station 
	
	@Element(required=false)
	@Path("MonitoredVehicleJourney/MonitoredCall")
	Boolean VehicleAtStop; 
	
	@Element(required=false)
	@Path("MonitoredVehicleJourney/MonitoredCall")
	String AimedArrivalTime; // exists when bus is NOT on route
	
	@Element(required=false)
	@Path("MonitoredVehicleJourney/MonitoredCall")
	String ActualArrivalTime;
	
	@Element(required=false)
	@Path("MonitoredVehicleJourney/MonitoredCall")
	String ArrivalStatus;
	
	
	Date getRecordedAtTime() {
		return handleDateGet(RecordedAtTime);
	}
	Date getOriginAimedDepartureTime() {
		return handleDateGet(OriginAimedDepartureTime);
	}
	Date getExpectedArrivalTime() {
		return handleDateGet(ExpectedArrivalTime);
	}	
	Date getAimedArrivalTime() {
		return handleDateGet(AimedArrivalTime);
	}
	Date getActualArrivalTime() {
		return handleDateGet(ActualArrivalTime);
	}
	
	private Date handleDateGet(String dateStr) {
		if (dateStr==null) {
			return null;
		}
		
		try {
			return NtvDateUtils.parseDateOutOfSoapString(dateStr);
		} catch (Exception ex) {
			ex.printStackTrace();// TODO: handle exception
		}
		return null;
	}
	
	
	public String toString()
	{
		return "RecordedAtTime:" + getRecordedAtTime() + ", LineRef:" + LineRef + ", vehicleLongitude=" + vehicleLongitude;
	}
}
