package com.monitoring.commons.route.handlers;

import com.monitoring.commons.dto.GenericMetricDTO;
import com.monitoring.commons.dto.MonitoringInfo;
import com.monitoring.commons.service.mapper.GenericMetricDTOMapper;
import com.monitoring.commons.constant.MicroserviceConstant;
import lombok.ToString;
import lombok.extern.log4j.Log4j;
import org.apache.camel.Body;
import org.apache.camel.ExchangeProperties;
import org.apache.camel.Handler;
import org.influxdb.dto.BatchPoints;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Monitor Inserts Processor defines the general implementation
 * to monitor the number of inserted metrics after metric persistence.
 * Each service can provide a specific implementation, that will be used by
 * each metric processing route, by overriding the @Handler method.
 */
@Service
@Lazy
@Log4j
@ToString
public class MonitorInsertsProcessor {

    protected final GenericMetricDTOMapper genericMetricDTOMapper;
    /**
     * Based on the monitored stack 'serviceTag' attribute should
     * represent the key used to identify the service/location from
     * where metrics were retrieved.
     * Possible values: server (by default), path (for 'diskspace' stack) etc.
     */
    protected final String serviceTag;

    @Autowired
    public MonitorInsertsProcessor(final GenericMetricDTOMapper genericMetricDTOMapper) {
        this.genericMetricDTOMapper = genericMetricDTOMapper;
        this.serviceTag = MicroserviceConstant.SERVER;
    }

    public MonitorInsertsProcessor(final GenericMetricDTOMapper genericMetricDTOMapper, @Value(MicroserviceConstant.SERVER) final String serviceTag) {
        this.genericMetricDTOMapper = genericMetricDTOMapper;
        this.serviceTag = serviceTag;
    }

    /**
     * Method designed to monitor the number of inserted metrics.
     * <p>
     * Each service can provide a specific implementation, that will be used by
     * each metric processing route, by overriding this method.
     *
     * @param batchPoints - the batch of Influx points that were sent for persistence
     * @param properties  - exchange properties that might be useful in obtaining more details
     *                    about the monitored route
     * @return List of Generic Metrics for saving to InfluxDB in order to provide a persistent tracking
     */
    @Handler
    public List<GenericMetricDTO> handle(@Body BatchPoints batchPoints,
                                         @ExchangeProperties Map<String, Object> properties) {

        String service = (String) properties.get(this.serviceTag);
        Long time = (Long) properties.get(MicroserviceConstant.INIT_TIME_TAG);

        int inserts = batchPoints.getPoints().size();
        log.info(String.format("%d inserted metrics for %s %s", inserts, this.serviceTag, service));

        MonitoringInfo monitoringInfo = createMonitoringInfo(inserts, service, time);
        GenericMetricDTO metricDTO = genericMetricDTOMapper.createMetricDTO(monitoringInfo, time);
        return Collections.singletonList(metricDTO);
    }

    /**
     * Method for conveniently creating a monitoring information object
     * encapsulating information regarding the service, the metric collection runtime
     * and the number of inserted metrics
     *
     * @param inserts   - number of inserted metrics in InfluxDB
     * @param service   - service/path for which the metrics were collected
     * @param startTime - the start time for the metric collection, useful for calculating the total runtime
     * @return MonitoringInfo object created based on the parameters received
     */
    protected MonitoringInfo createMonitoringInfo(int inserts, String service, Long startTime) {

        MonitoringInfo monitoringInfo = new MonitoringInfo();
        setMonitoringInfoDetails(monitoringInfo, inserts, service, startTime);
        return monitoringInfo;
    }

    /**
     * Method designed to set the appropriate details on the provided monitoring info object
     *
     * @param monitoringInfo - the monitoring info on which the details to be set
     * @param inserts        - number of inserted metrics in InfluxDB
     * @param service        - service/path for which the metrics were collected
     * @param startTime      - the start time for the metric collection, useful for calculating the total runtime
     */
    protected void setMonitoringInfoDetails(MonitoringInfo monitoringInfo, int inserts, String service, Long startTime) {
        long runTime = System.currentTimeMillis() - startTime;
        monitoringInfo.setInserts(inserts);
        monitoringInfo.setService(service);
        monitoringInfo.setRunTime(runTime);
    }

}
