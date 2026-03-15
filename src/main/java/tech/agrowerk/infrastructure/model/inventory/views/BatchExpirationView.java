package tech.agrowerk.infrastructure.model.inventory.views;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;
import org.hibernate.annotations.Synchronize;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "vw_batch_expiration")
@Immutable
@Subselect("SELECT * FROM vw_batch_expiration")
@Synchronize({"batchs", "inputs", "properties", "suppliers"})
@Getter
@NoArgsConstructor
public class BatchExpirationView {

    @Id
    private UUID batchId;
    private String batchNumber;
    private String inputName;
    private String categoryName;
    private UUID propertyId;
    private String propertyName;
    private String supplierName;
    private BigDecimal currentQuantity;
    private LocalDate expirationDate;
    private BigDecimal unitPrice;
    private BigDecimal currentValue;
    private Integer daysUntilExpiration;
    private String expirationStatus;
}