package com.common.network.exception;

public class ResponseThrowable extends Exception {
	public int code;
	public String message;
	public String data;

	public ResponseThrowable(Throwable throwable, int code) {
		super(throwable);
		this.code = code;
	}

	public ResponseThrowable(Throwable throwable, int code, String data) {
		super(throwable);
		this.code = code;
		this.data = data;
	}

	@Override
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
