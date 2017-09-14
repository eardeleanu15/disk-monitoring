package com.monitoring.commons.configuration.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Bean designed to hold properties
 * used for building InfluxDB Continuous Queries.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContinuousQuery {

    private String selectValue;
    private String intoPolicyName;
    private String fromPolicyName;
    private String groupByTimeValue;

}
