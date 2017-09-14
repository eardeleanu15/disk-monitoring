package com.monitoring.commons.service.mapper;

import com.monitoring.commons.dto.GenericMetricDTO;
import com.monitoring.commons.dto.MonitoringInfo;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.monitoring.commons.constant.MonitoringTable.*;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Mapper class designed to conveniently translate
 * generic monitoring info object to GenericMetricDTO
 */
@Lazy
@Service
public class GenericMetricDTOMapper {

    /**
     * Method designed to create a generic metric DTO from monitoring information and the metric collection time
     *
     * @param monitoringInfo - Monitoring info containing details regarding the total runtime of collecting the metric,
     *                       service name and type of exception
     * @param collectTime    - metric collection start time
     * @return Metric created for the monitoring info and the metric collection time
     */
    public GenericMetricDTO createMetricDTO(MonitoringInfo monitoringInfo, long collectTime) {
        Map<String, String> tags = new HashMap<>();
        tags.put(SERVICE_TAG, monitoringInfo.getService());

        Map<String, Number> fields = new HashMap<>();
        fields.put(INSERTS_FIELD, monitoringInfo.getInserts());
        fields.put(EXCEPTIONS_FIELD, monitoringInfo.getExceptions());
        fields.put(CONNECT_FAIL_FIELD, monitoringInfo.getConnectFail());
        fields.put(RUNTIME_FIELD, monitoringInfo.getRunTime());

        long seconds = TimeUnit.SECONDS.convert(collectTime, TimeUnit.MILLISECONDS);

        return new GenericMetricDTO(MONITORING_MEASUREMENT, seconds, SECONDS, tags, fields);
    }

}
