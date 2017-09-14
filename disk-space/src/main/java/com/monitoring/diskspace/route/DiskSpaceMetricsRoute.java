package com.monitoring.diskspace.route;

import com.monitoring.commons.route.AbstractRouteBuilder;
import com.monitoring.commons.route.handlers.GeneralExceptionProcessor;
import com.monitoring.commons.service.MetricConverter;
import com.monitoring.diskspace.configuration.DiskSpaceConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import static com.monitoring.commons.constant.MicroserviceConstant.INIT_TIME_TAG;
import static com.monitoring.commons.constant.MicroserviceConstant.PATHS_TAG;
import static com.monitoring.commons.constant.MicroserviceConstant.PATH_TAG;
import static com.monitoring.commons.constant.RoutesConstant.DIRECT_TO_INFLUX;

/**
 * Component designed to define camel route for
 * reading and processing diskspace metrics
 */
@Component
@Profile("diskspace")
public class DiskSpaceMetricsRoute extends AbstractRouteBuilder {

    private final DiskSpaceConfiguration diskSpace;

    @Autowired
    public DiskSpaceMetricsRoute(DiskSpaceConfiguration diskSpace, GeneralExceptionProcessor generalExceptionProcessor, MetricConverter metricConverter) {
        super(generalExceptionProcessor, metricConverter);
        this.diskSpace = diskSpace;
    }

    @Override
    public void configure() throws Exception {
        super.configure();
        String cronExpression = diskSpace.getCronExpression().getRetrieveMonitoringInfoCron();

        from(cronExpression)
                .setProperty(INIT_TIME_TAG).spel("#{T(java.lang.System).currentTimeMillis()}")
                .setHeader(PATHS_TAG, method(diskSpace,"getMonitoredPath"))
                    .split(header(PATHS_TAG)).parallelProcessing()
                        .setProperty(PATH_TAG).body()
                        .bean("diskSpaceService", "createPointList(${header.path},${header.initTime})")
                        .to(DIRECT_TO_INFLUX)
                .end();

    }

}
