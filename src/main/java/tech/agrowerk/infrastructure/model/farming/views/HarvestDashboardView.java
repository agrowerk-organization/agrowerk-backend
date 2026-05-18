package tech.agrowerk.infrastructure.model.farming.views;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;
import org.hibernate.annotations.Synchronize;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Immutable
@Subselect("SELECT * FROM harvest_dashboard_view")
@Synchronize({"harvests", "harvest_partials", "plantings", "harvest_forecasts"})
@Getter
@NoArgsConstructor
public class HarvestDashboardView {

    @Id
    @Column(name = "planting_id")
    private UUID plantingId;

    @Column(name = "property_id")
    private UUID propertyId;

    @Column(name = "total_partials")
    private Long totalPartials;

    @Column(name = "total_harvested_kg")
    private BigDecimal totalHarvestedKg;

    @Column(name = "quality_grade")
    private String qualityGrade;

    @Column(name = "finalized")
    private Boolean finalized;

    @Column(name = "harvest_date")
    private LocalDate harvestDate;

    @Column(name = "estimated_quantity")
    private BigDecimal estimatedQuantity;

    @Column(name = "committed_quantity")
    private BigDecimal committedQuantity;

    @Column(name = "confidence_level")
    private String confidenceLevel;

    @Column(name = "variety_name")
    private String varietyName;

    @Column(name = "crop_name")
    private String cropName;

    @Column(name = "field_name")
    private String fieldName;

    @Column(name = "season_name")
    private String seasonName;

    @Column(name = "planting_date")
    private LocalDate plantingDate;

    @Column(name = "expected_harvest_date")
    private LocalDate expectedHarvestDate;

    @Column(name = "achievement_rate")
    private BigDecimal achievementRate;

    @Column(name = "available_quantity")
    private BigDecimal availableQuantity;
}