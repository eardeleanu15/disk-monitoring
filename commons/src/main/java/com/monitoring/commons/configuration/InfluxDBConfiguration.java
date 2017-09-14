package com.monitoring.commons.configuration;

import com.monitoring.commons.configuration.properties.InfluxDbConf;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * InfluxDB configuration class
 */
@Configuration
public class InfluxDBConfiguration {

    /**
     * Bean used across monitor microservice for
     * communication (inserts/ query) with
     * InfluxDB instance.
     * @param influxDBConf - InfluxDB configuration property object
     * @return - InfluxDB
     */
    @Bean
    public InfluxDB influxDBConnection(InfluxDbConf influxDBConf) {
        return InfluxDBFactory.connect(influxDBConf.getUrl(), influxDBConf.getUsername(), influxDBConf.getPassword());
    }
}
