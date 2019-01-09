package com.kaboomreport.stats;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.stereotype.Component;
import static com.mongodb.client.model.Filters.eq;

@Component
public class MongoStatsUpdater implements StatsUpdater {
    private final MongoClientProperties properties;

    private MongoClient mongoClient;
    private MongoDatabase database;

    public MongoStatsUpdater(MongoClientProperties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("properties");
        }

        this.properties = properties;

        mongoClient = MongoClients.create();
        database = mongoClient.getDatabase("kaboom");
    }

    @Override
    public void close() throws Exception {
        mongoClient.close();
    }

    @Override
    public void updateEventStats(AppEvent event) {
        MongoCollection<Document> applications = database.getCollection("applications");
        Document application = applications.find(eq("appCode", event.getApplicationCode())).first();
        if (application == null) {
            // Skip orphan events (at least for now)
            return;
        }

        switch (event.getEventType()) {
            case START:
                updateStartEventStats(event, application);
                break;
            case CRASH:
                updateCrashEventStats(event, application);
                break;
            case UNKNOWN:
                // Ignore unknown events (at least for now)
                break;
        }
    }

    private void updateStartEventStats(AppEvent event, Document application) {
        System.out.println("HANDLING START EVENT"); // TODO:
    }

    private void updateCrashEventStats(AppEvent event, Document application) {
        System.out.println("HANDLING CRASH EVENT"); // TODO:
    }
}
