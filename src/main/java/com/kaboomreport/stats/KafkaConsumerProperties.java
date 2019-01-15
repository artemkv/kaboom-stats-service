package com.kaboomreport.stats;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:kafkaconsumer.properties")
@ConfigurationProperties(prefix="kafkaconsumer")
public class KafkaConsumerProperties {
    private String bootstrapServers = "localhost:9092";
    private int heartbeatInterval = 30000; // 30 seconds
    private int sessionTimeout = 120000; // 2 minutes
    private int maxPollInterval = 60000; // 1 minute
    private int pollTimeout = 10000; // 10 seconds
    private int maxEmptyPolls = 9; // 1.5 minute of empty polls

    public String getBootstrapServers() {
        return bootstrapServers;
    }
    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public int getPollTimeout() {
        return pollTimeout;
    }
    public void setPollTimeout(int pollTimeout) {
        this.pollTimeout = pollTimeout;
    }

    public int getMaxEmptyPolls() {
        return maxEmptyPolls;
    }
    public void setMaxEmptyPolls(int maxEmptyPolls) {
        this.maxEmptyPolls = maxEmptyPolls;
    }

    public int getSessionTimeout() {
        return sessionTimeout;
    }
    public void setSessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public int getHeartbeatInterval() {
        return heartbeatInterval;
    }
    public void setHeartbeatInterval(int heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }

    public int getMaxPollInterval() {
        return maxPollInterval;
    }
    public void setMaxPollInterval(int maxPollInterval) {
        this.maxPollInterval = maxPollInterval;
    }
}
