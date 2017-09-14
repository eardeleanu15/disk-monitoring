package com.monitoring.commons.route;

import com.monitoring.commons.configuration.properties.CronExpression;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Create continuous queries.
 * Start creating them with a delay of 10 minutes, so there's some data inserted in all the tables, for sure.
 * Try every 3 hours
 */
@Component
public class InfluxDBContinuousQueryRoute extends RouteBuilder {

    private final CronExpression cronExpression;

    @Autowired
    public InfluxDBContinuousQueryRoute(CronExpression cronExpression) {
        this.cronExpression = cronExpression;
    }

    @Override
    public void configure() throws Exception {

        // cron expression format. may be different for each service
        // i.e. run once every 3 hours, with a start delay of 10 mins => timer://timerName?delay=600000&fixedRate=true&period=10800000
        from(cronExpression.getContinuousQueryCreationCron())
                .bean("influxDBService", "createContinuousQueries");
    }
}
