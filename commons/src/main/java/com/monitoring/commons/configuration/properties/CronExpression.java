package com.monitoring.commons.configuration.properties;

import lombok.*;

/**
 * Retrieve scheduler expressions properties
 * from the configuration server
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CronExpression {

    private String databaseCreationCron;
    private String continuousQueryCreationCron;
    private String retrieveMonitoringInfoCron;

}
