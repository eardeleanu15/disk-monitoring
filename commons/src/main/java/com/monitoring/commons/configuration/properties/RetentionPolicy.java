package com.monitoring.commons.configuration.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Configuration Property bean defining retention policy
 * parameters used in InfluxDb databases.
 * Attributes:
 *    - name - retention policy name (i.e live_data)
 *    - duration - retention policy duration (i.e 2h or 2d or 2w)
 *    - defaultPolicy - boolean value used to instruct InfluxDB to
 *    persist metrics into a specific retention policy
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RetentionPolicy {

    private String name;
    private String duration;
    private boolean defaultPolicy;

}
