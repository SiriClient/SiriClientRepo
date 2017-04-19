package com.nituv.common.exceptions;

public class LbiException extends Exception {
	private static final long serialVersionUID = -427636383270581527L;

//	private String m_message = null; 
	
	public LbiException() {
		super();
	}

	public LbiException(Exception ex) {
		super(ex);
	}

	public LbiException(String msg) {
		super(msg);
//		m_message = msg;
	}

	
	public LbiException(String msg, Exception ex) {
		super(msg, ex);
//		m_message = msg;
	}

	public String toString() {
//		StringBuffer buff = new StringBuffer();
//		buff.append(super.toString());
//		if (m_message != null) {
//			buff.append("; msg:"); buff.append(m_message);
//		}
//		return buff.toString();
		return super.toString();
	}
}
