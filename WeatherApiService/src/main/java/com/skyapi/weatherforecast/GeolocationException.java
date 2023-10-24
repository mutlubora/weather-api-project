package com.skyapi.weatherforecast;

import java.io.IOException;

public class GeolocationException extends Exception {

    public GeolocationException(String message) {
        super(message);
    }
    public GeolocationException(String message, Throwable cause) {
        super(message, cause);
    }
}
