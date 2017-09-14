package com.monitoring.commons.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Bean holding constants to be used
 * across monitoring microservices.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MicroserviceConstant {

    public static final String PATHS_TAG = "paths";
    public static final String PATH_TAG = "path";
    public static final String INIT_TIME_TAG = "initTime";

    public static final String SERVER = "server";

    public static final String METRIC_TAG = "metric";

    public static final String VALUE_FIELD = "value";

}
