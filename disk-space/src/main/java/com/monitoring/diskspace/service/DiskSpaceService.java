package com.monitoring.diskspace.service;

import com.monitoring.commons.dto.GenericMetricDTO;
import com.monitoring.commons.service.MetricConverter;
import com.monitoring.diskspace.configuration.DiskSpaceConfiguration;
import com.google.common.collect.ImmutableMap;
import com.monitoring.diskspace.constant.DiskSpaceTable;
import lombok.ToString;
import lombok.extern.log4j.Log4j;
import org.apache.commons.io.FileUtils;
import org.influxdb.dto.BatchPoints;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.monitoring.commons.constant.MicroserviceConstant.METRIC_TAG;
import static com.monitoring.commons.constant.MicroserviceConstant.PATH_TAG;
import static com.monitoring.commons.constant.MicroserviceConstant.VALUE_FIELD;

/**
 * Service dealing with gathering diskspace metrics and converting
 * them to the appropriate format for write in InfluxDB database
 */
@Log4j
@Profile("diskspace")
@Service
@ToString
public class DiskSpaceService {

    private final DiskSpaceConfiguration diskSpace;
    private final MetricConverter metricConverter;

    @Autowired
    public DiskSpaceService(DiskSpaceConfiguration diskSpace, MetricConverter metricConverter) {
        this.diskSpace = diskSpace;
        this.metricConverter = metricConverter;
    }

    /**
     * Method designed to collect and transform disk-space metrics.
     *
     */
    public BatchPoints createPointList(String path, Long collectTime) throws IOException {
        log.info("DiskSpace: create BatchPoints list");
        List<GenericMetricDTO> pointListForPath = createPointListForPath(path, collectTime);
        List<GenericMetricDTO> pointListForDirInPath = createPointListForDirInPath(path, collectTime);

        BatchPoints batchPoints = metricConverter.convertMetricListToBatchPoints(
                Stream.concat(pointListForPath.stream(), pointListForDirInPath.stream()).collect(Collectors.toList()),
                diskSpace.getInflux().getDatabase());
        log.info(String.format("DiskSpace: BatchPoints contains %d points.", batchPoints.getPoints().size()));
        return batchPoints;
    }

    private List<GenericMetricDTO> createPointListForPath(String path, Long collectTime) throws IOException {
        log.info(String.format("DiskSpace: creating point list for path %s", path));
        List<GenericMetricDTO> pointList = new ArrayList<>();
        FileStore folder = Files.getFileStore(FileSystems.getDefault().getPath(path, ""));
        Long total = folder.getTotalSpace();
        Long free = folder.getUsableSpace();

        pointList.add(createMetric(DiskSpaceTable.METRIC_TOTAL_VALUE, path, total, collectTime));
        pointList.add(createMetric(DiskSpaceTable.METRIC_FREE_VALUE, path, free, collectTime));
        pointList.add(createMetric(DiskSpaceTable.METRIC_USED_VALUE, path, total - free, collectTime));

        log.info(String.format("DiskSpace: [%s, %d] ", path, pointList.size()));
        return pointList;

    }

    private GenericMetricDTO createMetric(String METRIC_VALUE, String path, Long value, Long collectTime) {
        Map<String, String> tags = ImmutableMap.of(PATH_TAG, path, METRIC_TAG, METRIC_VALUE);
        Map<String, Double> fields = ImmutableMap.of(VALUE_FIELD, value.doubleValue());
        long seconds = TimeUnit.SECONDS.convert(collectTime, TimeUnit.MILLISECONDS);
        return new GenericMetricDTO(DiskSpaceTable.DISKSPACE_MEASUREMENT, seconds, TimeUnit.SECONDS, tags, fields);
    }


    private List<GenericMetricDTO> createPointListForDirInPath(String path, Long collectTime) {
        log.info(String.format("DiskSpace: creating point list for path: %s ", path));
        List<GenericMetricDTO> pointList = new ArrayList<>();
        List<String> directoryList = getDirectoryList(path);

        log.info(String.format("DiskSpace: directory list %s ", directoryList));

        directoryList.forEach(directory -> {
            Long size = FileUtils.sizeOfDirectory(new File(directory));
            log.info(String.format("DiskSpace: %s -> %d", directory, size));

            Map<String, String> tagsForUsed = ImmutableMap.of(PATH_TAG, directory, METRIC_TAG, DiskSpaceTable.METRIC_USED_VALUE);
            log.info(String.format("DiskSpace: tags %s", tagsForUsed));

            Map<String, Double> fieldsForUsed = ImmutableMap.of(VALUE_FIELD, size.doubleValue());
            log.info(String.format("DiskSpace: fields %s", fieldsForUsed));
            long seconds = TimeUnit.SECONDS.convert(collectTime, TimeUnit.MILLISECONDS);
            pointList.add(new GenericMetricDTO(DiskSpaceTable.DISKSPACE_MEASUREMENT, seconds, TimeUnit.SECONDS, tagsForUsed, fieldsForUsed));
            log.info(String.format("DiskSpace: point %s", pointList));
        });
        log.info(String.format("DiskSpace: [%s, %d] ", path, pointList.size()));
        return pointList;

    }

    private List<String> getDirectoryList(String path) {
        File[] fileList = new File(path).listFiles(File::isDirectory);

        List<String> directoryList = Arrays.stream(fileList).map(File::getAbsolutePath).collect(Collectors.toList());
        log.info(String.format("Directory list: %s", directoryList));
        return directoryList;
    }


}
