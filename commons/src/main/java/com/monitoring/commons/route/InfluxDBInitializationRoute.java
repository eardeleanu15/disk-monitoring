package com.monitoring.commons.route;

import com.monitoring.commons.configuration.properties.CronExpression;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Create the database, retention policies and an user with all privileges, if it does not already exist.
 * Run the initialization only once, before starting any other routes.
 */
@Component
public class InfluxDBInitializationRoute extends RouteBuilder {

    private final CronExpression cronExpression;

    @Autowired
    public InfluxDBInitializationRoute(CronExpression cronExpression) {
        this.cronExpression = cronExpression;
    }

    @Override
    public void configure() throws Exception {

        // run only once, with highest priority order => timer://runOnce?repeatCount=1
        from(cronExpression.getDatabaseCreationCron())
                .startupOrder(1)
                .bean("influxDBService", "initInfluxDb");
    }
}
