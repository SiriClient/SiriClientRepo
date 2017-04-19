package com.nituv.common.exceptions;

public class LbiAbortProcessing extends LbiException {

	private static final long serialVersionUID = 4939854852128674145L;

	public LbiAbortProcessing() {
		super();
	}

	public LbiAbortProcessing(Exception ex) {
		super(ex);
	}

	public LbiAbortProcessing(String msg) {
		super(msg);
	}

	public LbiAbortProcessing(String msg, Exception ex) {
		super(msg, ex);
	}
}
