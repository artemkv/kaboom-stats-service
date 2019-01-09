package com.kaboomreport.stats;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

// TODO: call shutdown upon shutdown
public class KafkaEventConsumer implements Runnable {
    // Should not change after release, or it will re-read all messages from the beginning!!!
    private final String CONSUMER_GROUP_ID = "stats.aggregator.consumer";

    private final AtomicBoolean shutdownRequested = new AtomicBoolean(false);
    private final String[] topics;
    private final KafkaConsumerProperties consumerProperties;
    private Consumer<String, String> consumer;

    public KafkaEventConsumer(String[] topics,
                              KafkaConsumerProperties consumerProperties) {
        if (consumerProperties == null) {
            throw new IllegalArgumentException("bootstrapServers");
        }
        if (topics == null || topics.length == 0) {
            throw new IllegalArgumentException("topics");
        }

        this.topics = topics;
        this.consumerProperties = consumerProperties;
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
            consumer.subscribe(Arrays.asList(topics));

            int emptyPollsCounter = 0;
            while (emptyPollsCounter < consumerProperties.getMaxEmptyPolls()) {
                ConsumerRecords<String, String> records = consumer.poll(
                    Duration.ofMillis(consumerProperties.getPollTimeout()));

                if (records.count() == 0) {
                    emptyPollsCounter++;
                } else {
                    emptyPollsCounter = 0;

                    for (ConsumerRecord<String, String> record : records) {
                        // TODO: process records
                        System.out.println(String.format("Consumer Record:(%s, %s)", record.key(), record.value()));
                    }
                    consumer.commitSync();
                }
            }
            System.out.println("Could not retrieve any messages, exiting");
        }
        catch (WakeupException e) {
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
        }
    }

    public void shutdown() {
        shutdownRequested.set(true);
        consumer.wakeup();
    }
}
