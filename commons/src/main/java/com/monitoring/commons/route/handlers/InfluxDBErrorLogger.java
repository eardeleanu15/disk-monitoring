package com.monitoring.commons.route.handlers;

import lombok.extern.log4j.Log4j;
import org.apache.camel.ExchangeException;
import org.springframework.stereotype.Service;

/**
 * Simple Exception logger that logs an informative message and the stacktrace of the exception
 */
@Log4j
@Service
public class InfluxDBErrorLogger {

    public void handle(@ExchangeException Exception ex) {
        log.error("Problem inserting data to Influxdb ", ex);
    }
}
