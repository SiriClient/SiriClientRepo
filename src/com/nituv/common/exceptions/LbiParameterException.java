package com.nituv.common.exceptions;

public class LbiParameterException extends LbiException {

	private static final long serialVersionUID = 7297866077982888747L;

	public LbiParameterException() {
		super();
	}

	public LbiParameterException(Exception ex) {
		super(ex);
	}

	public LbiParameterException(String msg) {
		super(msg);
	}

	public LbiParameterException(String msg, Exception ex) {
		super(msg, ex);
	}
}
