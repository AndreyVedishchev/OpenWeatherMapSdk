package org.example.sdk;

import com.google.gson.JsonObject;

public class ExampleUsage {
    public static void main(String[] args) {

        String apiKey = "1036ed6808d6ee9e571741c7ccebda9f";
        try {
            WeatherSDK sdk = WeatherSDK.createInstance(apiKey, WeatherMode.ON_DEMAND);
            JsonObject weather = sdk.getWeather("Berlin");
            System.out.println(weather.toString());
            WeatherSDK.deleteInstance(apiKey);

        } catch (WeatherSDKException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
