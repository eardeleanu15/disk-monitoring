package com.monitoring.commons.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Constants class keeping monitoring table columns
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MonitoringTable {

    public static final String MONITORING_MEASUREMENT = "monitoring";

    public static final String SERVICE_TAG = "service";

    public static final String INSERTS_FIELD = "inserts";
    public static final String RUNTIME_FIELD = "runTime";
    public static final String EXCEPTIONS_FIELD = "exceptions";
    public static final String CONNECT_FAIL_FIELD = "connectFail";

}
