package com.monitoring.commons.constant;

/**
 * Constants holding query strings
 */
public class InfluxDBQueryConstant {

    public static final String CREATE_USER_WITH_ALL_PRIVILEGES = "CREATE USER %s WITH PASSWORD '%s' WITH ALL PRIVILEGES";
    public static final String SHOW_MEASUREMENTS = "SHOW MEASUREMENTS";
    public static final String SHOW_TAGS_KEY = "SHOW TAG KEYS FROM %s";

    public static final String FETCH_LAST_INSERT_TIME = "SELECT last(value) FROM %s";

    public static final String LIVE_DATA_TAG = "live_data";
    public static final String MINUTE_DATA_TAG = "minute_data";
    public static final String HOUR_DATA_TAG = "hour_data";
    public static final String MINUTE_CQ_TYPE = "minute";
    public static final String HOUR_CQ_TYPE = "hour";
    public static final String DAY_CQ_TYPE = "day";

    public static final String CONTINUOUS_QUERY_TEMPLATE =
            "CREATE CONTINUOUS QUERY  %s ON %s " +
            "BEGIN " +
                "SELECT %s " +
                "INTO %s " +
                "FROM %s " +
                "GROUP BY %s " +
            "END";
}
