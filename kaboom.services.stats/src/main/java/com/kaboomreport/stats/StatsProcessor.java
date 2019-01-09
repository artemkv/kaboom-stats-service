package com.kaboomreport.stats;

import org.springframework.stereotype.Component;

@Component
public class StatsProcessor implements Runnable {
    private IncomingEventConsumer consumer;

    public StatsProcessor(IncomingEventConsumer consumer) {
        if (consumer == null) {
            throw new IllegalArgumentException("consumer");
        }

        this.consumer = consumer;
    }

    @Override
    public void run() {
        consumer.run();
    }

    public void shutdown() {
        consumer.shutdown();
    }
}
