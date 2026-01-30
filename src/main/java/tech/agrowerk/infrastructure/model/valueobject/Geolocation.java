package tech.agrowerk.infrastructure.model.valueobject;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Geolocation {

    private BigDecimal latitude;
    private BigDecimal longitude;

    public String toWKT() {
        return String.format("POINT(%s %s", latitude, latitude);
    }

    public boolean isValid() {
        return latitude != null && longitude != null &&
                latitude.compareTo(new BigDecimal("-90")) >= 0 &&
                latitude.compareTo(new BigDecimal("90")) <= 0 &&
                longitude.compareTo(new BigDecimal("-180")) >= 0 &&
                longitude.compareTo(new BigDecimal("180")) <= 0;
    }
}
