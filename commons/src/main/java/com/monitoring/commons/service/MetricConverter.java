package com.monitoring.commons.service;

import com.monitoring.commons.configuration.properties.InfluxDbConf;
import com.monitoring.commons.dto.GenericMetricDTO;
import lombok.ToString;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service containing stateless methods for easily converting from GenericMetricDTO to InfluxDB specific classes
 * such as Point or BatchPoints
 */
@Service
@ToString
public class MetricConverter {

    private final InfluxDbConf influxDBConf;

    @Autowired
    public MetricConverter(InfluxDbConf influxDBConf) {
        this.influxDBConf = influxDBConf;
    }

    public BatchPoints convertMetricsToBatchPoints(List<GenericMetricDTO> modelList) {
        return convertMetricListToBatchPoints(modelList, influxDBConf.getDatabase());
    }


    public BatchPoints convertMetricListToBatchPoints(List<GenericMetricDTO> modelList, String database) {
        BatchPoints batchPoints = BatchPoints.database(database).build();

        modelList.forEach(model -> batchPoints.point(convertMetricToPoint(model)));

        return batchPoints;
    }

    private Point convertMetricToPoint(GenericMetricDTO model) {
        Point.Builder point = Point.measurement(model.getMeasurement()).time(model.getTime(), model.getPrecision());

        model.getTags().forEach((key, value) -> point.tag(key, value));
        model.getFields().forEach((key, value) -> point.addField(key, value));

        return point.build();
    }


}
