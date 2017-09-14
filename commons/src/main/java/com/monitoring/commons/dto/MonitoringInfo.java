package com.monitoring.commons.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simple POJO for holding monitoring information.
 * This information is generally logged to InfluxDB.
 * It can be very useful in providing information regarding failed queries
 * or running time of collection information on a per service/path basis.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class MonitoringInfo {

    private int inserts;
    private int exceptions;
    private int connectFail;

    private long runTime;

    private String service;

}
