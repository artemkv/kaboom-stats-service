package com.kaboomreport.stats;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AppEvent {
    private String eventTypeString;
    private String applicationCode;

    @JsonProperty("t")
    private String getEventTypeString() {
        return eventTypeString;
    }
    private void setEventTypeString(String type) {
        this.eventTypeString = type;
    }

    public EventType getEventType() {
        switch (eventTypeString) {
            case "S":
                return EventType.START;
            case "C":
                return EventType.CRASH;
            default:
                return EventType.UNKNOWN;
        }
    }

    @JsonProperty("a")
    public String getApplicationCode() {
        return applicationCode;
    }
    private void setApplicationCode(String applicationCode) {
        this.applicationCode = applicationCode;
    }
}
