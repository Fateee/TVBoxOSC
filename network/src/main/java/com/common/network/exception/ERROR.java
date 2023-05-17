package com.common.network.exception;

public interface ERROR {
	public static final int UNKNOWN = 1000;
	public static final int PARSE_ERROR = 1001;
	public static final int NETWORK_ERROR = 1002;
	public static final int SSL_ERROR = 1003;
	public static final int TIMEOUT_ERROR = 1004;
	public static final int HTTP_ERROR = 1005;
}
