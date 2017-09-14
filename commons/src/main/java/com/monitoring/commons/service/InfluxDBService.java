package com.monitoring.commons.service;

import com.monitoring.commons.configuration.properties.ContinuousQuery;
import com.monitoring.commons.configuration.properties.InfluxDbConf;
import com.monitoring.commons.configuration.properties.RetentionPolicy;
import lombok.ToString;
import lombok.extern.log4j.Log4j;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.monitoring.commons.constant.InfluxDBQueryConstant.*;

/**
 * Service class interacting with influxdb database.
 */
@Component
@Log4j
@ToString
public class InfluxDBService {

    private static final String RETENTION_POLICY_ALREADY_EXISTS = "retention policy already exists";
    private static final String CREATE_CQ_LOGGER = "Creating %s continuous query for measurement %s";

    private final InfluxDbConf influxDBConf;
    private final InfluxDB influxDBConnection;

    @Autowired
    public InfluxDBService(InfluxDbConf influxDBConf, InfluxDB influxDBConnection) {
        this.influxDBConf = influxDBConf;
        this.influxDBConnection = influxDBConnection;
    }

    /**
     * Method designed to initialize an InfluxDBInstance with:
     * - one user with all privileges
     * - one database
     * - retention policies (number of retention policies
     * could differ based on monitored service)
     */
    public void initInfluxDb() {
        log.info("Initializing InfluxDB");
        // Creating user with all privileges
        String query = String.format(CREATE_USER_WITH_ALL_PRIVILEGES, influxDBConf.getUsername(), influxDBConf.getPassword());
        executeQuery(null, query, true);
        log.info("InfluxDB: created user with all privileges");

        // Creating new influxdb instance
        influxDBConnection.createDatabase(influxDBConf.getDatabase());
        log.info("InfluxDB: created database:" + influxDBConf.getDatabase());

        // Creating retention policies
        Arrays.stream(influxDBConf.getRetentionPolicies()).forEach(this::createRetentionPolicies);
    }

    private void createRetentionPolicies(RetentionPolicy policy) {
        String database = influxDBConf.getDatabase();
        String query = String.format(
                "RETENTION POLICY %s ON %s DURATION %s REPLICATION 1 %s",
                policy.getName(),
                database,
                policy.getDuration(),
                policy.isDefaultPolicy() ? " DEFAULT" : "");

        QueryResult result = executeQuery(database, String.format("CREATE %s", query), false);

        if (!result.getResults().isEmpty() && result.getResults().get(0).hasError() && RETENTION_POLICY_ALREADY_EXISTS.equals(result.getResults().get(0).getError())) {
            result = executeQuery(database, String.format("ALTER %s", query), false);
        }

        if (!result.getResults().isEmpty() && result.getResults().get(0).hasError()) {
            log.error(String.format("While creating retention policy %s on %s got error %s",
                    policy.getName(), database, result.getResults().get(0).getError()));
        }
        log.info(String.format("InfluxDB: created retention policy: %s", policy.getName()));
    }

    /**
     * Method designed for InfluxDB continuous queries creation.
     * This operation depends on the number of retention policies present for each service.
     * i.e If 'live_data' retention policy is present a Continuous Query will be created to
     * aggregate data from 'live_data' to 'minute_data' retention policy.
     */
    public void createContinuousQueries() {
        log.info("Creating continuous queries");

        // Execute InfluxDB Query to find all measurements/tables in our database
        QueryResult measurements = executeQuery(influxDBConf.getDatabase(), SHOW_MEASUREMENTS, false);

        // ForEach measurement -> create continuous queries
        measurements.getResults().get(0).getSeries().get(0).getValues().forEach(measurement -> {
            String measurementName = measurement.get(0).toString();

            // Discover all tag keys from a measurement
            String tags = "\"" + String.join("\", \"", fetchTagNames(measurementName)) + "\"";

            Set<String> policyNames =
                    Arrays.stream(influxDBConf.getRetentionPolicies())
                            .map(RetentionPolicy::getName)
                            .collect(Collectors.toSet());

            if (policyNames.contains(LIVE_DATA_TAG)) {
                log.info(String.format(CREATE_CQ_LOGGER, MINUTE_CQ_TYPE, measurementName));
                // Create Minute Continuous Query
                createContinuousQuery(measurementName, MINUTE_CQ_TYPE, tags);
            }


            if (policyNames.contains(MINUTE_DATA_TAG)) {
                log.info(String.format(CREATE_CQ_LOGGER, HOUR_CQ_TYPE, measurementName));
                // Create Hour Continuous Query
                createContinuousQuery(measurementName, HOUR_CQ_TYPE, tags);
            }

            if (policyNames.contains(HOUR_DATA_TAG)) {
                log.info(String.format(CREATE_CQ_LOGGER, DAY_CQ_TYPE, measurementName));
                // Create Day Continuous Query
                createContinuousQuery(measurementName, DAY_CQ_TYPE, tags);
            }
        });
    }

    /**
     * Method designed for building continuous query from template
     * and for executing it in InfluxDB using Influx Java API.
     *
     * @param measurementName - name of the measurement/table
     * @param type            - type of the created continuous query
     *                        ('minute', 'hour', 'day')
     * @param tags            - set of tag keys from measurement/table
     */
    private void createContinuousQuery(String measurementName, String type, String tags) {
        ContinuousQuery continuousQueryParams = influxDBConf.getContinuousQueries().get(type);
        if (continuousQueryParams == null) {
            log.error(String.format("Continuous Query parameters are missing from configuration file for type %s", type));
            throw new IllegalStateException(String.format("Unable to create continuous query - parameters are missing for type %s", type));
        }

        String cqName = String.format("%s_%s", measurementName, type);
        String database = influxDBConf.getDatabase();
        String groupBy = String.format("%s,%s", continuousQueryParams.getGroupByTimeValue(), tags);
        String into = String.format("%s.%s", continuousQueryParams.getIntoPolicyName(), measurementName);
        String from = String.format("%s.%s", continuousQueryParams.getFromPolicyName(), measurementName);

        String query = String.format(CONTINUOUS_QUERY_TEMPLATE,
                cqName,
                database,
                continuousQueryParams.getSelectValue(),
                into,
                from,
                groupBy);

        executeQuery(database, query, false);
    }

    /**
     * Method designed to return tag keys from a measurement/table.
     *
     * @param measurement - name of the measurement
     * @return - list of tag keys
     */
    private List<String> fetchTagNames(String measurement) {
        String query = String.format(SHOW_TAGS_KEY, measurement);
        QueryResult tags = executeQuery(influxDBConf.getDatabase(), query,false);

        List<String> tagNames = new ArrayList<>();
        tags.getResults().get(0).getSeries().get(0).getValues().forEach(tag -> tagNames.add(tag.get(0).toString()));

        return tagNames;
    }

    /**
     * Retrieve time of the last insert into InfluxDB table
     *
     * @param measurement -> name of the queried table
     * @return  -> the latest inserted record
     */
    public QueryResult fetchLatestRecord (String measurement) {
        String query = String.format(FETCH_LAST_INSERT_TIME, measurement);

        return executeQuery(influxDBConf.getDatabase(), query, false);
    }

    /**
     * Method to be used for querying InfluxDB instances on default database
     * (database specified in configuration file)
     *
     * @param query - query to be executed on InfluxDB instance
     * @param requiresPost - true -> only if the query requires a POST instead of a GET; false -> otherwise
     * @return - a List of Series which matched the query.
     */
    public QueryResult executeQuery(String query, boolean requiresPost) {
        return executeQuery(influxDBConf.getDatabase(), query, requiresPost);
    }


    /**
     * Method to be used for querying InfluxDB instances, while specifying
     * queried database.
     *
     * @param database - the database to be queried
     * @param query - query to be executed on InfluxDB instance
     * @param requiresPost - true -> only if the query requires a POST instead of a GET; false -> otherwise
     * @return - a List of Series which matched the query.
     */
    public QueryResult executeQuery(String database, String query, boolean requiresPost) {
        if (requiresPost) {
            return influxDBConnection.query(new Query(query, database, true));
        }

        return influxDBConnection.query(new Query(query, database));
    }

}
