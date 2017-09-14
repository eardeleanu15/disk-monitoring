package com.monitoring.commons.route;

import com.monitoring.commons.route.handlers.GeneralExceptionProcessor;
import com.monitoring.commons.service.MetricConverter;
import org.apache.camel.builder.RouteBuilder;

/**
 * Declares common exception policy to be used for handling exceptions from the service monitoring routes
 * In order to use the AbstractRouteBuilder:
 * 1. The class configuring the routes should extend the AbstractRouteBuilder
 * 2. Call super.configure() in the overridden configure method where the actual routes are defined
 */
public abstract class AbstractRouteBuilder extends RouteBuilder {

    private final GeneralExceptionProcessor generalExceptionProcessor;
    protected final MetricConverter metricConverter;

    public AbstractRouteBuilder(GeneralExceptionProcessor generalExceptionProcessor, MetricConverter metricConverter) {
        this.generalExceptionProcessor = generalExceptionProcessor;
        this.metricConverter = metricConverter;
    }

    /**
     * Defines a general exception policy that marks the exception as handled,
     * after calling a profile specific exception processor and sending the details to InfluxDB
     *
     * @throws Exception
     */
    @Override
    public void configure() throws Exception {

        onException(Exception.class)
                .bean(generalExceptionProcessor)
                .bean(metricConverter, "convertMetricsToBatchPoints")
                .to("influxdb://influxDBConnection?batch=true")
                .handled(true)
                .end();

    }
}
