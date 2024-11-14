export JAVA_TOOL_OPTIONS="-javaagent:/root/java-kafka-otel-producer-consumer/opentelemetry-javaagent.jar"
export OTEL_TRACES_EXPORTER=otlp
export OTEL_METRICS_EXPORTER=otlp
export OTEL_LOGS_EXPORTER=otlp
# US region
export OTEL_EXPORTER_OTLP_ENDPOINT='https://otlp.nr-data.net'
# EU region
#export OTEL_EXPORTER_OTLP_ENDPOINT='https://otlp.eu01.nr-data.net'
export OTEL_EXPORTER_OTLP_HEADERS="api-key=${NEW_RELIC_LICENSE_KEY}"
export OTEL_SERVICE_NAME="kafka-java-producer"

./mvnw spring-boot:run