package tech.agrowerk.infrastructure.model.valueobject;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Period {

    private LocalDate startDate;
    private LocalDate endDate;

    public boolean isActive() {
        LocalDate now = LocalDate.now();
        return !now.isBefore(startDate) && !now.isAfter(endDate);
    }

    public long getDurationInDays() {
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    public boolean overlaps(Period period) {
        return !this.endDate.isBefore(period.startDate) &&
                !period.endDate.isBefore(this.startDate);
    }
}
