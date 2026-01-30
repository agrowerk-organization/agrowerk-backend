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
public class QualityMetrics {

    private BigDecimal moisturePercentage;
    private BigDecimal impurityPercentage;
    private String qualityGrade;

    public boolean meetsStandard(BigDecimal maxMoisture, BigDecimal maxImpurity) {
        return moisturePercentage.compareTo(maxMoisture) <= 0 &&
                impurityPercentage.compareTo(maxImpurity) <= 0;
    }

    public String getQualityDescription() {
        if (moisturePercentage.compareTo(new BigDecimal("14")) <= 0 &&
                impurityPercentage.compareTo(new BigDecimal("1")) <= 0) {
            return "Premium";
        } else if (moisturePercentage.compareTo(new BigDecimal("18")) <= 0 &&
                impurityPercentage.compareTo(new BigDecimal("2")) <= 0) {
            return "Standard";
        }
        return "Below Standard";
    }
}
