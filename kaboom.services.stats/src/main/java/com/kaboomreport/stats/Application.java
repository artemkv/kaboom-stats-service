package com.kaboomreport.stats;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication
public class Application {
    private static ExecutorService pool = Executors.newCachedThreadPool();
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(Application.class, args);

        // Start processing messages on a separate thread
        KafkaConsumerProperties kafkaConsumerProperties =
            context.getBean(KafkaConsumerProperties.class);
        KafkaEventConsumer eventConsumer = new KafkaEventConsumer(
            new String[]{"launch_event", "crash_event"},
            kafkaConsumerProperties);
        pool.execute(eventConsumer);
    }
}