package tech.agrowerk.application.dto.weather.location;

import jakarta.validation.constraints.Size;
import java.util.UUID;

public record WeatherLocationUpdateRequest(
        @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
        String name,

        String timezone,

        UUID propertyId,

        Boolean active
) {}