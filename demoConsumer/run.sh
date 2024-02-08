#export JAVA_TOOL_OPTIONS="-javaagent:/Users/hkimpel/projects/kafka/java/opentelemetry-javaagent.jar"
export OTEL_TRACES_EXPORTER=otlp
export OTEL_METRICS_EXPORTER=otlp
export OTEL_LOGS_EXPORTER=otlp
export OTEL_EXPORTER_OTLP_ENDPOINT='https://otlp.nr-data.net'
export OTEL_EXPORTER_OTLP_HEADERS="api-key=NEW_RELIC_LICENSE_KEY"
export OTEL_SERVICE_NAME="kafka-java-consumer"

./mvnw spring-boot:run