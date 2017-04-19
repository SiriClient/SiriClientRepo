package com.nituv.mot.siri.client;

import com.nituv.common.exceptions.LbiException;

public class LbiNetworkFaliureException extends LbiException {
	private static final long serialVersionUID = -8549047753718840390L;
	public LbiNetworkFaliureException(Exception ex) {
		super(ex);
	}
}
