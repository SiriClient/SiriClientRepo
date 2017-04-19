package com.nituv.common.exceptions;

public class LbiInvalidDataException extends LbiException {

	private static final long serialVersionUID = -27922247548060599L;

	public LbiInvalidDataException() {
		super();
	}

	public LbiInvalidDataException(Exception ex) {
		super(ex);
	}

	public LbiInvalidDataException(String msg) {
		super(msg);
	}

	public LbiInvalidDataException(String msg, Exception ex) {
		super(msg, ex);
	}
}
