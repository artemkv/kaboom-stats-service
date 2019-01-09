package com.kaboomreport.stats;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:mongoclient.properties")
@ConfigurationProperties(prefix="mongoclient")
public class MongoClientProperties {
    private String connectionString = "mongodb://localhost:27017";

    public String getConnectionString() {
        return connectionString;
    }
    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }
}
