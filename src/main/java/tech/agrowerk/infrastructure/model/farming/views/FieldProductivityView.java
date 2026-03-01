package tech.agrowerk.infrastructure.model.farming.views;

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
@Table(name = "vw_field_productivity")
@Immutable
@Subselect("SELECT * FROM vw_field_productivy")
@Synchronize({"fields, yields, harvests, plantings, properties"})
@Getter
@NoArgsConstructor
public class FieldProductivityView {

    @Id
    private UUID fieldId;
    private String fieldName;
    private String propertyName;
    private BigDecimal avgProductivity;
    private BigDecimal totalProducedKg;
    private Long totalHarvests;
}