package com.nituv.lbi;

import com.nituv.common.exceptions.LbiInternalException;
import com.nituv.common.exceptions.LbiNotFoundException;

public interface ILbiLinesBehaviourIntelligenceMgr 
{
	public void startDbMonitoringForNewInvestigations() throws LbiInternalException;
	public int checkDbForNewInvestigations() throws LbiInternalException;
	public void startTripInvestigation() throws LbiInternalException;
	public void startRouteInvestigation() throws LbiInternalException;
	public void startRouteSnapshot(int gtfsRouteId, int routeInvestigationId) throws LbiInternalException, LbiNotFoundException;
	public void shutdown();
}
