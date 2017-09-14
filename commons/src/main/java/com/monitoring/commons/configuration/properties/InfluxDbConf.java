package com.monitoring.commons.configuration.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Configuration Property bean defined to hold
 * properties used in InfluxDb interactions.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InfluxDbConf {

    private String url;
    private String database;
    private String username;
    private String password;
    private String consistency;
    private RetentionPolicy[] retentionPolicies;
    private Map<String, ContinuousQuery> continuousQueries;

}
