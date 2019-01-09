package com.kaboomreport.stats;

public interface StatsUpdater extends AutoCloseable {
    void updateEventStats(AppEvent event);
}
