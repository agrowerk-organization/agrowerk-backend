package tech.agrowerk.application.dto.weather.location;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

public record WeatherLocationCreateRequest(
        @NotBlank(message = "Name is required")
        @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
        String name,

        @NotNull(message = "Latitude is required")
        @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
        @DecimalMax(value = "90.0", message = "Latitude must be <= 90")
        BigDecimal latitude,

        @NotNull(message = "Longitude is required")
        @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
        @DecimalMax(value = "180.0", message = "Longitude must be <= 180")
        BigDecimal longitude,

        @NotBlank(message = "State is required")
        @Size(min = 2, max = 2, message = "State must be exactly 2 characters")
        @Pattern(regexp = "[A-Z]{2}", message = "State must be in uppercase")
        String state,

        String country,

        String timezone,

        UUID propertyId,

        Boolean active
) {
    public WeatherLocationCreateRequest {
        if (country == null) country = "BR";
        if (active == null) active = true;
    }
}