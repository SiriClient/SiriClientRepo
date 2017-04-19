package com.nituv.lbi.dbobjects;

import java.util.Date;

import org.postgresql.jdbc.PgArray;

import com.nituv.common.util.NtvDateUtils;
import com.nituv.lbi.ELbiRouteInvestigationStatuses;
import com.nituv.lbi.ELbiRouteInvestigationTypes;
import com.nituv.lbi.inf.Lg;

public class LbiRouteInvestigation implements
	ILbiRouteInvestigation
{

	private Integer routeInvestigationId;
	private Integer gtfsRouteIdFk;
	private Date startDate;
	private Date endDate;
	private Integer priority;
	private Integer samplesGoalForTimeFrame;
	private Integer routeInvestigationStatusIdFk;// 1='not started', 2='started', 3='ended'
	private Integer routeInvestigationTypeIdFk;
	private Integer[] stopcodes;
//	private Integer[] stopIds;
	
	public Integer getRouteInvestigationId() {
		return routeInvestigationId;
	}
	public Integer getGtfsRouteIdFk() {
		return gtfsRouteIdFk;
	}
	public Date getStartDate() {
		return startDate;
	}
	public Date getEndDate() {
		return endDate;
	}
	public Integer getPriority() {
		return priority;
	}
	public Integer getSamplesGoalForTimeFrame() {
		return samplesGoalForTimeFrame;
	}
	public Integer getRouteInvestigationStatusIdFk() {
		return routeInvestigationStatusIdFk;
	}
	public Integer getRouteInvestigationTypeIdFk() {
		return routeInvestigationTypeIdFk;
	}
	
	
	public void setRoute_Investigation_Id(Integer routeInvestigationId) {
		this.routeInvestigationId = routeInvestigationId;
	}
	public void setGtfs_Route_Id_Fk(Integer gtfsRouteIdFk) {
		this.gtfsRouteIdFk = gtfsRouteIdFk;
	}
	public void setStart_Date(Date startDate) {
		this.startDate = startDate;
	}
	public void setEnd_Date(Date endDate) {
		this.endDate = endDate;
	}
	public void setPriority(Integer priority) {
		this.priority = priority;
	}
	public void setSamples_Goal_For_Time_Frame(Integer samplesGoalForTimeFrame) {
		this.samplesGoalForTimeFrame = samplesGoalForTimeFrame;
	}
	public void setRoute_Investigation_Status_Id_Fk(Integer routeInvestigationStatusIdFk) {
		this.routeInvestigationStatusIdFk = routeInvestigationStatusIdFk;
	}
	public void setRoute_Investigation_Type_Id_Fk(Integer routeInvestigationTypeIdFk) {
		this.routeInvestigationTypeIdFk = routeInvestigationTypeIdFk;
	}
	
	@Override
	public String toString()
	{
		StringBuffer strBuf = new StringBuffer();
		strBuf.append("invId:");strBuf.append(routeInvestigationId);
		strBuf.append(", RtId :");strBuf.append(gtfsRouteIdFk);
		strBuf.append(", strtDate:");strBuf.append(NtvDateUtils.toUtcDate(startDate));
		strBuf.append(", endDate:");strBuf.append(NtvDateUtils.toUtcDate(endDate));
		strBuf.append(", priority:");strBuf.append(priority);
		strBuf.append(", samplesGoal:");strBuf.append(samplesGoalForTimeFrame);
		strBuf.append(", status:");strBuf.append(routeInvestigationStatusIdFk);
		strBuf.append(", type:");strBuf.append(routeInvestigationTypeIdFk);
		return strBuf.toString();
	}
	
	@Override
	public ELbiRouteInvestigationStatuses getRouteInvestigationStatus()
	{
		return ELbiRouteInvestigationStatuses.valueOf(getRouteInvestigationStatusIdFk());
	}

	@Override
	public ELbiRouteInvestigationTypes getRouteInvestigationType()
	{
		return ELbiRouteInvestigationTypes.valueOf(getRouteInvestigationTypeIdFk());
	}
	
	@Override
	public Integer[] getRouteInvestigationStopcodes() {
		return stopcodes;
	}
	
//	@Override
//	public Integer[] getRouteInvestigationStopIds() {
//		return stopIds;
//	}
	
	public void setRoute_Investigation_Stopcodes(PgArray stopcodes) {
		try {
			this.stopcodes = (Integer[]) stopcodes.getArray();
		} catch (Exception ex) {
			Lg.lgr.error(ex);
		}
	}

//	public void setRoute_Investigation_Stop_ids(PgArray stopIds) {
//		try {
//			this.stopIds = (Integer[]) stopIds.getArray();
//		} catch (Exception ex) {
//			Lg.lgr.error(ex);
//		}
//	}
}

