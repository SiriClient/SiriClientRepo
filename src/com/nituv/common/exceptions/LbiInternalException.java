package com.nituv.common.exceptions;

public class LbiInternalException extends LbiException {

	public LbiInternalException() {
		super();
	}

	public LbiInternalException(Exception ex) {
		super(ex);
	}

	public LbiInternalException(String msg) {
		super(msg);
	}

	public LbiInternalException(String msg, Exception ex) {
		super(msg, ex);
	}

	private static final long serialVersionUID = 8576094272590636354L;
}
