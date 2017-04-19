package com.nituv.lbi.dbobjects;

import java.util.Date;

import com.nituv.lbi.ELbiRouteInvestigationStatuses;
import com.nituv.lbi.ELbiRouteInvestigationTypes;

public interface ILbiRouteInvestigation 
{

	public Integer getRouteInvestigationId();
	public Integer getGtfsRouteIdFk();
	public Date getStartDate();
	public Date getEndDate();
	public Integer getPriority();
	public Integer getSamplesGoalForTimeFrame();
	public Integer getRouteInvestigationStatusIdFk();// 1='not started', 2='started', 3='ended'
	public Integer getRouteInvestigationTypeIdFk();// 1=statistical trips durations, 2=statistical stations stops, 3=all trip durations, 4=all stations stops
	public Integer[] getRouteInvestigationStopcodes();
//	public Integer[] getRouteInvestigationStopIds();
	public ELbiRouteInvestigationStatuses getRouteInvestigationStatus();
	public ELbiRouteInvestigationTypes getRouteInvestigationType();

}
