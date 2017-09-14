package com.monitoring.commons.route.handlers;

import com.monitoring.commons.dto.GenericMetricDTO;
import com.monitoring.commons.dto.MonitoringInfo;
import com.monitoring.commons.service.mapper.GenericMetricDTOMapper;
import lombok.ToString;
import lombok.extern.log4j.Log4j;
import org.apache.camel.ExchangeException;
import org.apache.camel.ExchangeProperties;
import org.apache.camel.Handler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.monitoring.commons.constant.MicroserviceConstant.INIT_TIME_TAG;
import static com.monitoring.commons.constant.MicroserviceConstant.SERVER;

/**
 * General Exception Processor class that defines the
 * processor used by the AbstractRouteBuilder in order to
 * handle exceptions before logging them to InfluxDB
 * Each service can override the default implementation,
 * that will be used by the AbstractRouteBuilder
 */
@Service
@Lazy
@Log4j
@ToString
public class GeneralExceptionProcessor {

    protected final GenericMetricDTOMapper genericMetricDTOMapper;
    /**
     * Based on the monitored stack 'serviceTag' attribute should
     * represent the key used to identify the service/location from
     * where metrics were retrieved.
     * Possible values: server (by default), path (for 'diskspace' stack) etc.
     */
    protected final String serviceTag;

    @Autowired
    public GeneralExceptionProcessor(final GenericMetricDTOMapper genericMetricDTOMapper) {
        this.genericMetricDTOMapper = genericMetricDTOMapper;
        this.serviceTag = SERVER;
    }

    public GeneralExceptionProcessor(final GenericMetricDTOMapper genericMetricDTOMapper, @Value(SERVER) final String serviceTag) {
        this.genericMetricDTOMapper = genericMetricDTOMapper;
        this.serviceTag = serviceTag;
    }

    /**
     * Handler method that defines general error handling implementation.
     * This method could be overridden for specific service implementation.
     *
     * @param exception  - the thrown exception that caused the interruption of the processing flow
     * @param properties - exchange properties that might be useful in obtaining more details about the monitored route
     *                   where the exception occurred (such as service instance or collection start time)
     * @return List of Generic Metrics for saving to InfluxDB in order to provide a persistent tracking
     */
    @Handler
    public List<GenericMetricDTO> handle(@ExchangeException Exception exception,
                                         @ExchangeProperties Map<String, Object> properties) {

        // retrieve needed values from headers
        String service = (String) properties.get(this.serviceTag);
        Long startTime = (Long) properties.get(INIT_TIME_TAG);
        log.error(String.format("Exception occurred while collecting metrics for %s %s", this.serviceTag, service), exception);

        //create MonitoringInfo metric
        MonitoringInfo monitoringInfo = createMonitoringInfo(exception, service, startTime);

        // convert monitoring info instance to a genericMetricDTO
        GenericMetricDTO genericMetric = genericMetricDTOMapper.createMetricDTO(monitoringInfo, startTime);
        return Collections.singletonList(genericMetric);

    }

    /**
     * Method for conveniently creating a monitoring information object
     * encapsulating information regarding the service, the metric collection runtime and the type of exception
     *
     * @param exception - exception that caused the error situation
     * @param service   - service/path for which the metrics were collected
     * @param startTime - the start time for the metric collection, useful for calculating the total runtime
     * @return MonitoringInfo object created based on the parameters received
     */
    protected MonitoringInfo createMonitoringInfo(Exception exception, String service, Long startTime) {
        MonitoringInfo monitoringInfo = new MonitoringInfo();
        setMonitoringInfoDetails(monitoringInfo, exception, service, startTime);
        return monitoringInfo;
    }

    /**
     * Method designed to set the appropriate details on the provided monitoring info object
     *
     * @param monitoringInfo - the monitoring info on which the details to be set
     * @param exception      - the thrown exception that caused the interruption of the processing flow
     * @param service        - service/path for which the metrics were collected
     * @param startTime      - the start time for the metric collection, useful for calculating the total runtime
     */
    protected void setMonitoringInfoDetails(MonitoringInfo monitoringInfo, Exception exception, String service, Long startTime) {
        long runTime = System.currentTimeMillis() - startTime;

        monitoringInfo.setRunTime(runTime);
        monitoringInfo.setService(service);

        applyExceptionPolicy(monitoringInfo, exception);
    }

    /**
     * Method designed to apply specific exception policy (increment internal counter)
     * on monitoring-info instance based on exception type.
     *
     * @param monitoringInfo - MonitoringInfo instance on which the exception policy is applied
     * @param exception      - caught exception
     */
    protected void applyExceptionPolicy(MonitoringInfo monitoringInfo, Exception exception) {
        if (exception instanceof IOException) {
            monitoringInfo.setConnectFail(1);
        } else {
            monitoringInfo.setExceptions(1);
        }
    }

}
