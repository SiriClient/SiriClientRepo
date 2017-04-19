package com.nituv.common.exceptions;

public class LbiNotFoundException extends LbiException {

	private static final long serialVersionUID = -3207548919189461965L;

	public LbiNotFoundException() {
		super();
	}

	public LbiNotFoundException(Exception ex) {
		super(ex);
	}

	public LbiNotFoundException(String msg) {
		super(msg);
	}

	public LbiNotFoundException(String msg, Exception ex) {
		super(msg, ex);
	}
}
