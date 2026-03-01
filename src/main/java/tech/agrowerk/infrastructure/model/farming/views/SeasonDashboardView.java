package tech.agrowerk.infrastructure.model.farming.views;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;
import org.hibernate.annotations.Synchronize;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "mv_season_dashboard")
@Immutable
@Subselect("SELECT * FROM mv_season_dashboard")
@Synchronize({"seasons, plantings, properties, crops, harvests, yields"})
@Getter
@NoArgsConstructor
public class SeasonDashboardView {

    @Id
    @Column(name = "season_id")
    private UUID seasonId;

    @Column(name = "season_name")
    private String seasonName;

    @Column(name = "property_id")
    private UUID propertyId;

    @Column(name = "property_name")
    private String propertyName;

    @Column(name = "crop_name")
    private String cropName;

    @Column(name = "total_plantings")
    private Long totalPlantings;

    @Column(name = "total_area")
    private BigDecimal totalArea;

    @Column(name = "total_produced_kg")
    private BigDecimal totalProducedKg;

    @Column(name = "avg_productivity")
    private BigDecimal avgProductivity;
}