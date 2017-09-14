package com.monitoring.diskspace.route.handlers;

import com.monitoring.commons.route.handlers.MonitorInsertsProcessor;
import com.monitoring.commons.service.mapper.GenericMetricDTOMapper;
import lombok.ToString;
import lombok.extern.log4j.Log4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import static com.monitoring.commons.constant.MicroserviceConstant.PATH_TAG;

/**
 * Disk Space Specific Insert Monitor Processor
 */
@Profile("diskspace")
@Service
@Primary
@Lazy
@Log4j
@ToString
public class DiskSpaceMonitorInsertsProcessor extends MonitorInsertsProcessor {

    public DiskSpaceMonitorInsertsProcessor(GenericMetricDTOMapper genericMetricDTOMapper) {
        super(genericMetricDTOMapper, PATH_TAG);
    }

}
