package com.monitoring.commons.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Constants holding camel routes
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RoutesConstant {

    public static final String DIRECT_TO_INFLUX = "direct:writeToInflux";

}
