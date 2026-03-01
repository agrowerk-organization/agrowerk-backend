package tech.agrowerk.infrastructure.model.farming.views;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;
import org.hibernate.annotations.Synchronize;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "vw_active_plantings")
@Immutable
@Subselect("SELECT * FROM vw_active_plantings")
@Synchronize({"plantings", "properties", "crops", "seasons", "fields"})
@Getter
@NoArgsConstructor
public class ActivePlantingView {

    @Id
    private UUID plantingId;
    private String propertyName;
    private String cropName;
    private String seasonName;
    private String fieldName;
    private BigDecimal areaHectares;
    private LocalDate plantingDate;
    private LocalDate expectedHarvestDate;
    private String plantingStatus;
}
