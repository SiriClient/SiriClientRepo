package com.nituv.common.exceptions;

public class LbiCanceledException extends LbiException {

	private static final long serialVersionUID = 7297866077982888747L;

	public LbiCanceledException() {
		super();
	}
	
	public LbiCanceledException(Exception ex) {
		super(ex);
	}

	public LbiCanceledException(String msg) {
		super(msg);
	}

	public LbiCanceledException(String msg, Exception ex) {
		super(msg, ex);
	}
}
