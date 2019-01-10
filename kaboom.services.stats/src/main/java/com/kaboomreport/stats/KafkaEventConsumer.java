package com.kaboomreport.stats;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

// TODO: call shutdown upon shutdown (check https://www.baeldung.com/spring-boot-shutdown)
@Component
public class KafkaEventConsumer implements IncomingEventConsumer {
    private static final String[] TOPICS = new String[]{"launch_event", "crash_event"};
    // Should not change after release, or it will re-read all messages from the beginning!!!
    private static final String CONSUMER_GROUP_ID = "stats.aggregator.consumer";

    private final AtomicBoolean shutdownRequested = new AtomicBoolean(false);
    private final KafkaConsumerProperties consumerProperties;
    private final StatsUpdater statsUpdater;

    private Consumer<String, String> consumer;

    public KafkaEventConsumer(KafkaConsumerProperties consumerProperties,
                              StatsUpdater statsUpdater) {
        if (consumerProperties == null) {
            throw new IllegalArgumentException("bootstrapServers");
        }
        if (statsUpdater == null) {
            throw new IllegalArgumentException("statsUpdater");
        }

        this.consumerProperties = consumerProperties;
        this.statsUpdater = statsUpdater;
    }

    @Override
    public void run() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, consumerProperties.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, CONSUMER_GROUP_ID);
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, consumerProperties.getHeartbeatInterval());
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, consumerProperties.getSessionTimeout());
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, consumerProperties.getMaxPollInterval());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        try {
            consumer = new KafkaConsumer<>(props);
            consumer.subscribe(Arrays.asList(TOPICS));

            int emptyPollsCounter = 0;
            while (emptyPollsCounter < consumerProperties.getMaxEmptyPolls() &&
                !shutdownRequested.get()) {
                ConsumerRecords<String, String> records = consumer.poll(
                    Duration.ofMillis(consumerProperties.getPollTimeout()));

                if (records.count() == 0) {
                    emptyPollsCounter++;
                } else {
                    emptyPollsCounter = 0;

                    for (ConsumerRecord<String, String> record : records) {
                        process(record);
                    }
                    consumer.commitSync();
                }
            }
            if (shutdownRequested.get()) {
                System.out.println("Shutdown requested, exiting");
            } else {
                System.out.println("Could not retrieve any messages, exiting");
            }
        } catch (WakeupException e) {
            if (!shutdownRequested.get()) {
                throw e;
            } else {
                // Ignore it, we are shutting down
            }
        } finally {
            if (consumer != null) {
                try {
                    consumer.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (statsUpdater != null) {
                try {
                    statsUpdater.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void shutdown() {
        shutdownRequested.set(true);
        consumer.wakeup();
    }

    private void process(ConsumerRecord<String, String> record) {
        // TODO: process records
        System.out.println(String.format("Consumer Record:(%s, %s)", record.key(), record.value()));

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        try {
            AppEvent event = mapper.readValue(record.value(), AppEvent.class);
            statsUpdater.updateEventStats(event);
        } catch (IOException e) {
            // Ignore invalid events (at least for now)
            e.printStackTrace();
        }
    }
}
