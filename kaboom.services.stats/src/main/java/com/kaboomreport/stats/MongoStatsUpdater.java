package com.kaboomreport.stats;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.setOnInsert;

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
                saveCrashDetails(event, application);
                break;
            case UNKNOWN:
                // Ignore unknown events (at least for now)
                break;
        }
    }

    private void updateStartEventStats(AppEvent event, Document application) {
        String appId = application.get("_id").toString();

        String year = getYear(event.getReceivedOn());
        MongoCollection<Document> appStatsStartByYear =
            database.getCollection("appstats.start.byyear");
        appStatsStartByYear.updateOne(
            and(eq("appId", appId), eq("dt", year)),
            new Document("$inc", new Document("count", 1)),
            new UpdateOptions().upsert(true));

        String month = getMonth(event.getReceivedOn());
        MongoCollection<Document> appStatsStartByMonth =
            database.getCollection("appstats.start.bymonth");
        appStatsStartByMonth.updateOne(
            and(eq("appId", appId), eq("dt", month)),
            new Document("$inc", new Document("count", 1)),
            new UpdateOptions().upsert(true));

        String day = getDay(event.getReceivedOn());
        MongoCollection<Document> appStatsStartByDay =
            database.getCollection("appstats.start.byday");
        appStatsStartByDay.updateOne(
            and(eq("appId", appId), eq("dt", day)),
            new Document("$inc", new Document("count", 1)),
            new UpdateOptions().upsert(true));

        String hour = getHour(event.getReceivedOn());
        MongoCollection<Document> appStatsStartByHour =
            database.getCollection("appstats.start.byhour");
        appStatsStartByHour.updateOne(
            and(eq("appId", appId), eq("dt", hour)),
            new Document("$inc", new Document("count", 1)),
            new UpdateOptions().upsert(true));

        String minute = getMinute(event.getReceivedOn());
        MongoCollection<Document> appStatsStartByMinute =
            database.getCollection("appstats.start.byminute");
        appStatsStartByMinute.updateOne(
            and(eq("appId", appId), eq("dt", minute)),
            new Document("$inc", new Document("count", 1)),
            new UpdateOptions().upsert(true));

        String second = getSecond(event.getReceivedOn());
        MongoCollection<Document> appStatsStartBySecond =
            database.getCollection("appstats.start.bysecond");
        appStatsStartBySecond.updateOne(
            and(eq("appId", appId), eq("dt", second)),
            new Document("$inc", new Document("count", 1)),
            new UpdateOptions().upsert(true));
    }

    private void updateCrashEventStats(AppEvent event, Document application) {
        String appId = application.get("_id").toString();

        String year = getYear(event.getReceivedOn());
        MongoCollection<Document> appStatsCrashByYear =
            database.getCollection("appstats.crash.byyear");
        appStatsCrashByYear.updateOne(
            and(eq("appId", appId), eq("dt", year)),
            new Document("$inc", new Document("count", 1)),
            new UpdateOptions().upsert(true));

        String month = getMonth(event.getReceivedOn());
        MongoCollection<Document> appStatsCrashByMonth =
            database.getCollection("appstats.crash.bymonth");
        appStatsCrashByMonth.updateOne(
            and(eq("appId", appId), eq("dt", month)),
            new Document("$inc", new Document("count", 1)),
            new UpdateOptions().upsert(true));

        String day = getDay(event.getReceivedOn());
        MongoCollection<Document> appStatsCrashByDay =
            database.getCollection("appstats.crash.byday");
        appStatsCrashByDay.updateOne(
            and(eq("appId", appId), eq("dt", day)),
            new Document("$inc", new Document("count", 1)),
            new UpdateOptions().upsert(true));

        String hour = getHour(event.getReceivedOn());
        MongoCollection<Document> appStatsCrashByHour =
            database.getCollection("appstats.crash.byhour");
        appStatsCrashByHour.updateOne(
            and(eq("appId", appId), eq("dt", hour)),
            new Document("$inc", new Document("count", 1)),
            new UpdateOptions().upsert(true));

        String minute = getMinute(event.getReceivedOn());
        MongoCollection<Document> appStatsCrashByMinute =
            database.getCollection("appstats.crash.byminute");
        appStatsCrashByMinute.updateOne(
            and(eq("appId", appId), eq("dt", minute)),
            new Document("$inc", new Document("count", 1)),
            new UpdateOptions().upsert(true));

        String second = getSecond(event.getReceivedOn());
        MongoCollection<Document> appStatsCrashBySecond =
            database.getCollection("appstats.crash.bysecond");
        appStatsCrashBySecond.updateOne(
            and(eq("appId", appId), eq("dt", second)),
            new Document("$inc", new Document("count", 1)),
            new UpdateOptions().upsert(true));
    }

    private void saveCrashDetails(AppEvent event, Document application) {
        try {
            String appId = application.get("_id").toString();
            String hash = getDigest(event.getMessage() + event.getDetails());

            Document crash = new Document("appId", appId)
                .append("hash", hash)
                .append("message", event.getMessage())
                .append("details", event.getDetails())
                .append("dt", event.getReceivedOn());

            MongoCollection<Document> appCrashes = database.getCollection("appcrashes");
            appCrashes.updateOne(
                and(eq("appId", appId), eq("hash", hash)),
                combine(
                    new Document("$setOnInsert", crash),
                    new Document("$inc", new Document("count", 1))
                ),
                new UpdateOptions().upsert(true));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Could not calculate event hash. MD5 is not supported", e);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Could not calculate event hash. UTF-8 is not supported", e);
        }
    }

    private String getYear(LocalDateTime date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy");
        return date.format(formatter);
    }

    private String getMonth(LocalDateTime date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");
        return date.format(formatter);
    }

    private String getDay(LocalDateTime date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return date.format(formatter);
    }

    private String getHour(LocalDateTime date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHH");
        return date.format(formatter);
    }

    private String getMinute(LocalDateTime date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        return date.format(formatter);
    }

    private String getSecond(LocalDateTime date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return date.format(formatter);
    }

    private String getDigest(String data)
        throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(data.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
