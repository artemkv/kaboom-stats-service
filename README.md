[![CircleCI](https://circleci.com/gh/artemkv/kaboom-stats-service.svg?style=svg)](https://circleci.com/gh/artemkv/kaboom-stats-service)

Kaboom Stats Service

Calculates aggregated statistics from events in Kafka and stores it in MongoDB for querying.

Custom properties:

```
kafkaconsumer.bootstrap_servers = ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
kafkaconsumer.heartbeat_interval = ${KAFKA_HEARTBEAT_INTERVAL:30000}
kafkaconsumer.session_timeout = ${KAFKA_SESSION_TIMEOUT:120000}
kafkaconsumer.max_poll_interval = ${KAFKA_MAX_POLL_INTERVAL:60000}
kafkaconsumer.poll_timeout = ${KAFKA_POLL_TIMEOUT:10000}
kafkaconsumer.max_empty_polls = ${KAFKA_MAX_EMPTY_POLLS:9}

mongoclient.connection_string=${MONGO_CONNECTION_STRING:mongodb://localhost:27017}
mongoclient.db_name=${MONGO_DBNAME:kaboom}
```