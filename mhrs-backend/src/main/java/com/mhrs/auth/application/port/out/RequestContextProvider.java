package com.mhrs.auth.application.port.out;

public interface RequestContextProvider {

    String clientIp();

    String userAgent();
}
