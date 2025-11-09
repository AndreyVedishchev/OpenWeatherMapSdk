package org.example.sdk;

import java.time.Instant;
import com.google.gson.JsonObject;

public class WeatherCacheEntry {
    private final JsonObject data;
    private final Instant timestamp;

    public WeatherCacheEntry(JsonObject data) {
        this.data = data;
        this.timestamp = Instant.now();
    }

    public boolean isUpToDate() {
        // Data is actual if it less then 10 minutes old (600 sec)
        return Instant.now().minusSeconds(600).isBefore(timestamp);
    }

    public JsonObject getData() {
        return data;
    }
}
