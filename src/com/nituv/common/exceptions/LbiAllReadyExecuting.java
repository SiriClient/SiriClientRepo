package com.nituv.common.exceptions;

public class LbiAllReadyExecuting extends LbiException {

	private static final long serialVersionUID = -7321835188289991583L;

	public LbiAllReadyExecuting() {
		super();
	}
	
	public LbiAllReadyExecuting(Exception ex) {
		super(ex);
	}

	public LbiAllReadyExecuting(String msg) {
		super(msg);
	}

	public LbiAllReadyExecuting(String msg, Exception ex) {
		super(msg, ex);
	}
}
