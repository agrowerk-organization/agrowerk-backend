package tech.agrowerk.application.dto.response.property;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.agrowerk.application.dto.response.core.AddressResponse;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PropertyResponse {
    private UUID id;
    private String name;
    private String stateRegistration;
    private String ruralRegistration;
    private AddressResponse address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal totalArea;
    private BigDecimal plantedArea;
    private String mainCrop;
    private Boolean isActive;
    private String stateName;
    private boolean hasWeatherLocation;
    private List<FarmUnitResponse> units;
    private Instant createdAt;
}