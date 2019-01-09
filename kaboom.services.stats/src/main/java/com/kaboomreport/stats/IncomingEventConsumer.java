package com.kaboomreport.stats;

public interface IncomingEventConsumer {
    void run();
    void shutdown();
}
