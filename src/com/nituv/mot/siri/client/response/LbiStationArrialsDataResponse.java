package com.nituv.mot.siri.client.response;

import java.util.Date;

import com.nituv.common.util.NtvDateUtils;
import com.nituv.lbi.inf.Lg;
import com.nituv.mot.siri.client.ILbiStationArrivalsDataResponse;

class LbiStationArrialsDataResponse implements ILbiStationArrivalsDataResponse {
	private boolean m_isMessageValid = false;
	private boolean m_hasMonitoredStopVisitData = false;
	private MonitoredStopVisit m_stationData = null;
	
	public LbiStationArrialsDataResponse(MonitoredStopVisit stationData) {
		m_isMessageValid = true;
		m_hasMonitoredStopVisitData = true;
		m_stationData = stationData;
		if (m_stationData.MonitoringRef != null && !m_stationData.MonitoringRef.equals(m_stationData.StopPointRef)) 
		{
			Lg.lgr.warn("m_stationData.MonitoringRef:{} != m_stationData.StopPointRef:{}", m_stationData.MonitoringRef, m_stationData.StopPointRef);
		}
	}

	@Override
	public boolean isMessageValid() {
		return m_isMessageValid;
	}

	@Override
	public boolean hasMonitoredStopVisitData() {
		return m_hasMonitoredStopVisitData;
	}

	@Override
	public Integer getMonitoringRef() {
		if (m_stationData != null) {
			return m_stationData.MonitoringRef;// and MonitoringRef.;
		} else {
			return null;
		}
	}

	@Override
	public Integer getLineRef() {
		if (m_stationData != null) {
			return m_stationData.LineRef;
		} else {
			return null;
		}
	}

	@Override
	public Integer getDirectionRef() {
		if (m_stationData != null) {
			return m_stationData.DirectionRef;
		} else {
			return null;
		}
	}

	@Override
	public String getPublishedLineName() {
		if (m_stationData != null) {
			return m_stationData.PublishedLineName;
		} else {
			return null;
		}
	}

	@Override
	public Integer getOperatorRef() {
		if (m_stationData != null) {
			return m_stationData.OperatorRef;
		} else {
			return null;
		}
	}

	@Override
	public Integer getDestinationRef() {
		if (m_stationData != null) {
			return m_stationData.DestinationRef;
		} else {
			return null;
		}
	}

	@Override
	public Date getRecordedAtTime() {
		if (m_stationData != null) {
			return m_stationData.getRecordedAtTime();
		} else {
			return null;
		}
	}

	@Override
	public Date getOriginAimedDepartureTime() {
		if (m_stationData != null) {
			return m_stationData.getOriginAimedDepartureTime();
		} else {
			return null;
		}
	}

	@Override
	public boolean getIsExpectedBusOnRoute() {
		if (m_stationData != null) {
			if (m_stationData.vehicleLatitude != null) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	@Override
	public Double getVehicleLongitude() {
		if (m_stationData != null) {
			return m_stationData.vehicleLongitude;
		} else {
			return null;
		}
	}

	@Override
	public Double getVehicleLatitude() {
		if (m_stationData != null) {
			return m_stationData.vehicleLatitude;
		} else {
			return null;
		}
	}

	@Override
	public boolean isVehicleAtStop() {
		if (m_stationData == null || m_stationData.VehicleAtStop == null) {
			return false;
		} else {
			return m_stationData.VehicleAtStop;
		}
	}

	@Override
	public Date getExpectedArrivalTime() {
		if (m_stationData != null) {
			return m_stationData.getExpectedArrivalTime();
		} else {
			return null;
		}
	}

	@Override
	public Date getAimedArrivalTime() {
		if (m_stationData != null) {
			return m_stationData.getAimedArrivalTime();
		} else {
			return null;
		}
	}

	@Override
	public Date getActualArrivalTime() {
		if (m_stationData != null) {
			return m_stationData.getActualArrivalTime();
		} else {
			return null;
		}
	}

	@Override
	public String getArrivalStatus() {
		if (m_stationData != null) {
			return m_stationData.ArrivalStatus;
		} else {
			return null;
		}
	}

	@Override
	public Integer getStopPointRef() {
		if (m_stationData != null) {
			return m_stationData.StopPointRef;// and MonitoringRef.;
		} else {
			return null;
		}
	}


	public String toString() {
		if (!isMessageValid()) {
			return "message not valid";
		}
		StringBuffer buf = new StringBuffer();
		buf.append("stn(mntr):" + getMonitoringRef());
		if (!hasMonitoredStopVisitData()) {
			buf.append(", has no monitored stop vists data");
			return buf.toString();
		}
		buf.append(", ln:" + getLineRef());
		buf.append(", dir:" + getDirectionRef());
		buf.append(", lnName:" + getPublishedLineName());
		buf.append(", opCod:" + getOperatorRef());
		buf.append(", dstStn:" + getDestinationRef());
		buf.append(", orgAimDepTime:" + NtvDateUtils.formatTimeShortString(getOriginAimedDepartureTime()));

		// based on VehicleLocation
		buf.append(", BusOnRoute:" + getIsExpectedBusOnRoute());
			buf.append(": long,lat:");
				buf.append(getVehicleLongitude() + "," + getVehicleLatitude());
		
		// based on MonitoredCall 
		buf.append(", atStp:" + isVehicleAtStop());
		buf.append(", expArv:" + NtvDateUtils.formatTimeShortString(getExpectedArrivalTime()));
		buf.append(", aimArv:" + NtvDateUtils.formatTimeShortString(getAimedArrivalTime()));
		buf.append(", actlArv:" + NtvDateUtils.formatTimeShortString(getActualArrivalTime()));
		buf.append(", arvStts:" + getArrivalStatus());
		buf.append(", stn(StpPnt):" + getStopPointRef());
		buf.append(", recTm:" + NtvDateUtils.formatTimeShortString(getRecordedAtTime()));
		
		return buf.toString();
	}

}
