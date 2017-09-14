package com.monitoring.commons.route;

import com.monitoring.commons.route.handlers.InfluxDBErrorLogger;
import com.monitoring.commons.route.handlers.MonitorInsertsProcessor;
import com.monitoring.commons.service.MetricConverter;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.monitoring.commons.constant.RoutesConstant.DIRECT_TO_INFLUX;

/**
 * Component designed to configure camel route for interaction
 * (insert) with InfluxDB. To achieve this we've decided to use
 * influxdb camel component.
 */
@Component
public class InfluxWriterRoute extends RouteBuilder {

    private final MetricConverter metricConverter;

    private final MonitorInsertsProcessor monitorInsertsProcessor;

    private final InfluxDBErrorLogger influxDBErrorLogger;

    public InfluxWriterRoute(final MetricConverter metricConverter, final MonitorInsertsProcessor monitorInsertsProcessor,
                             final InfluxDBErrorLogger influxDBErrorLogger) {
        this.metricConverter = metricConverter;
        this.monitorInsertsProcessor = monitorInsertsProcessor;
        this.influxDBErrorLogger = influxDBErrorLogger;
    }

    /**
     * Camel route for interaction (write - default operation) with
     * InfluxDB via influx camel component. Mandatory exchange body
     * needs to be BatchPoints (influxDB java api entity) when batch
     * insert is enabled (batch=true set on camel endpoint).
     *
     * InfluxDB Camel Endpoint format
     * influxdb://connectionBean?[options]
     *
     */
    @Override
    public void configure() throws Exception {

        // for InfluxDB IOExceptions we want to avoid using the general logger which inserts to influxDB
        from(DIRECT_TO_INFLUX)
                .onException(IOException.class)
                    .bean(influxDBErrorLogger)
                    .handled(true)
                .end()
                .onCompletion().onCompleteOnly()
                    .bean(monitorInsertsProcessor)
                    .bean(metricConverter, "convertMetricsToBatchPoints")
                    .to("influxdb://influxDBConnection?batch=true")
                .end()
                .to("influxdb://influxDBConnection?batch=true");
    }
}
