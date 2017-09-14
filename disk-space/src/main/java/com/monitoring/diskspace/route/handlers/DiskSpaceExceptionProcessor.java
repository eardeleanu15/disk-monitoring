package com.monitoring.diskspace.route.handlers;

import com.monitoring.commons.route.handlers.GeneralExceptionProcessor;
import com.monitoring.commons.service.mapper.GenericMetricDTOMapper;
import lombok.ToString;
import lombok.extern.log4j.Log4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import static com.monitoring.commons.constant.MicroserviceConstant.PATH_TAG;

/**
 * Disk Space Specific Exception Handler
 */
@Profile("diskspace")
@Primary
@Lazy
@Service
@Log4j
@ToString
public class DiskSpaceExceptionProcessor extends GeneralExceptionProcessor {

    public DiskSpaceExceptionProcessor(GenericMetricDTOMapper genericMetricDTOMapper) {
        super(genericMetricDTOMapper, PATH_TAG);
    }

}
