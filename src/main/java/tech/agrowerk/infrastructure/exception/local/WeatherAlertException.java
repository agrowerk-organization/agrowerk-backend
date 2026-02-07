package tech.agrowerk.infrastructure.exception.local;

public class WeatherAlertException extends RuntimeException {
    public WeatherAlertException(String message) {
        super(message);
    }
}
