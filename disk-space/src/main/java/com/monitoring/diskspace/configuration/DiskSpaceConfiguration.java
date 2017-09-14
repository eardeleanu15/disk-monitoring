package com.monitoring.diskspace.configuration;

import com.monitoring.commons.configuration.properties.CronExpression;
import com.monitoring.commons.configuration.properties.InfluxDbConf;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Model mapping diskspace properties received at runtime from the configuration server
 */
@Profile("diskspace")
@ConfigurationProperties
@RefreshScope
@Component
@Getter
@Setter
public class DiskSpaceConfiguration {

    private InfluxDbConf influx;

    private CronExpression cronExpression;

    private String[] monitoredPath;

    @Bean
    public InfluxDbConf influxDBConf() {
        return influx;
    }

    @Bean
    public CronExpression cronExpression() {
        return cronExpression;
    }


}
