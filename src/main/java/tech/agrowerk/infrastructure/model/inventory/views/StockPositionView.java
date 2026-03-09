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
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vw_stock_position")
@Immutable
@Subselect("SELECT * FROM vw_stock_position")
@Synchronize({"stocks", "inputs", "input_categories", "warehouses", "properties"})
@Getter
@NoArgsConstructor
public class StockPositionView {

    @Id
    private UUID stockId;
    private String propertyName;
    private String inputName;
    private String categoryName;
    private String stockType;
    private BigDecimal currentQuantity;
    private BigDecimal reservedQuantity;
    private BigDecimal availableQuantity;
    private BigDecimal weightedAverageCost;
    private BigDecimal totalValue;
    private BigDecimal minimumStock;
    private BigDecimal maximumStock;
    private String stockAlert;
    private String warehouseName;
    private LocalDateTime lastEntryDate;
    private LocalDateTime lastExitDate;
}