package com.monitoring.commons.dto;

import lombok.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Generic dto for keeping metric information.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class GenericMetricDTO {

    private String measurement;
    private Long time;
    private TimeUnit precision;
    private Map<String, String> tags;
    private Map<String, ? extends Number> fields;

}
