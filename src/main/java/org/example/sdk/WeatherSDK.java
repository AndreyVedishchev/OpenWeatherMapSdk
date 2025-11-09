package org.example.sdk;

import com.google.gson.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class WeatherSDK {
    private static final Map<String, WeatherSDK> instances = new ConcurrentHashMap<>();
    private final Map<String, WeatherCacheEntry> cache = new ConcurrentHashMap<>();
    private final String apiKey;
    private final WeatherMode mode;
    private ScheduledExecutorService executor;

    private static final int MAX_CACHE_SIZE = 10;

    private WeatherSDK(String apiKey, WeatherMode mode) throws WeatherSDKException {
        if (apiKey == null || apiKey.isEmpty())
            throw new WeatherSDKException("API Key cannot be null or empty");

        this.apiKey = apiKey;
        this.mode = mode;

        if (mode == WeatherMode.POLLING) {
            startPolling();
        }
    }

    public static synchronized WeatherSDK createInstance(String apiKey, WeatherMode mode) throws WeatherSDKException {
        if (instances.containsKey(apiKey))
            throw new WeatherSDKException("Instance with this API key already exists");
        WeatherSDK instance = new WeatherSDK(apiKey, mode);
        instances.put(apiKey, instance);
        return instance;
    }

    public static synchronized void deleteInstance(String apiKey) {
        WeatherSDK instance = instances.remove(apiKey);
        if (instance != null && instance.executor != null)
            instance.executor.shutdownNow();
    }

    public JsonObject getWeather(String city) throws WeatherSDKException {
        if (city == null || city.isEmpty())
            throw new WeatherSDKException("City name cannot be null or empty");

        WeatherCacheEntry entry = cache.get(city);
        if (entry != null && entry.isUpToDate()) {
            return entry.getData();
        }

        JsonObject updatedData = fetchWeatherFromAPI(city);
        update(city, updatedData);
        return updatedData;
    }

    private synchronized void update(String city, JsonObject data) {
        if (cache.size() >= MAX_CACHE_SIZE) {
            String outdated = cache.keySet().iterator().next();
            cache.remove(outdated);
        }
        cache.put(city, new WeatherCacheEntry(data));
    }

    private JsonObject fetchWeatherFromAPI(String city) throws WeatherSDKException {
        try {
            String urlStr = String.format("https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s&units=metric",
                            URLEncoder.encode(city, "UTF-8"),
                            apiKey
            );

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() != 200)
                throw new WeatherSDKException("API request failed: " + conn.getResponseMessage());

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                return convertToStandardJson(json);
            }
        } catch (Exception e) {
            throw new WeatherSDKException("Failed to fetch weather: " + e.getMessage(), e);
        }
    }

    private JsonObject convertToStandardJson(JsonObject json) {
        JsonObject res = new JsonObject();

        JsonObject weatherPart = new JsonObject();
        JsonObject w = json.getAsJsonArray("weather").get(0).getAsJsonObject();
        weatherPart.addProperty("main", w.get("main").getAsString());
        weatherPart.addProperty("description", w.get("description").getAsString());
        res.add("weather", weatherPart);

        JsonObject temperaturePart = new JsonObject();
        JsonObject main = json.getAsJsonObject("main");
        temperaturePart.addProperty("temp", main.get("temp").getAsDouble());
        temperaturePart.addProperty("feels_like", main.get("feels_like").getAsDouble());
        res.add("temperature", temperaturePart);

        res.addProperty("visibility", json.get("visibility").getAsInt());

        JsonObject windPart = new JsonObject();
        JsonObject wind = json.getAsJsonObject("wind");
        windPart.addProperty("speed", wind.get("speed").getAsDouble());
        res.add("wind", windPart);

        res.addProperty("datetime", json.get("dt").getAsLong());
        JsonObject sys = json.getAsJsonObject("sys");
        JsonObject sysPart = new JsonObject();
        sysPart.addProperty("sunrise", sys.get("sunrise").getAsLong());
        sysPart.addProperty("sunset", sys.get("sunset").getAsLong());
        res.add("sys", sysPart);

        res.addProperty("timezone", json.get("timezone").getAsInt());
        res.addProperty("name", json.get("name").getAsString());

        return res;
    }

    private void startPolling() {
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            for (String city : cache.keySet()) {
                try {
                    JsonObject data = fetchWeatherFromAPI(city);
                    cache.put(city, new WeatherCacheEntry(data));
                    System.out.println("Data add in cash about city " + city + " in method startPolling.");
                } catch (WeatherSDKException e) {
                    System.err.println("Polling update failed for " + city + ": " + e.getMessage());
                }
            }
        }, 0, 10, TimeUnit.MINUTES);
    }
}
