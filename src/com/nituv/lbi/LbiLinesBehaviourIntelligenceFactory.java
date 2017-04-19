package com.nituv.lbi;

import com.nituv.common.exceptions.LbiParameterException;
import com.nituv.lbi.dal.LbiLinesBehaviourIntelligenceMgrDal;
import com.nituv.lbi.inf.Lg;
import com.nituv.mot.siri.client.LbiNetworkFaliureException;

public class LbiLinesBehaviourIntelligenceFactory {

	public static ILbiLinesBehaviourIntelligenceMgr createLinesBehaviourIntelligenceMgr()
	{
		try {
			return new LbiLinesBehaviourIntelligenceMgr(new LbiLinesBehaviourIntelligenceMgrDal());
		} catch (LbiParameterException | LbiNetworkFaliureException ex) {
			Lg.lgr.error(ex);
			return null;
		}
		
	}
}
