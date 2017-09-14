mvn clean install &&
cd aggregator &&
mvn package -DskipTests docker:build