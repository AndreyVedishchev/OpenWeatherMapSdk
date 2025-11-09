package org.example.sdk;

public class WeatherSDKException extends Exception {
    public WeatherSDKException(String message) {
        super(message);
    }

    public WeatherSDKException(String message, Throwable cause) {
        super(message, cause);
    }
}
