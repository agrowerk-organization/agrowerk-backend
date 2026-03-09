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
@Table(name = "vw_stock_movements")
@Immutable
@Subselect("SELECT * FROM vw_stock_movements")
@Synchronize({"movement_stocks", "stocks", "properties", "inputs", "users", "batchs"})
@Getter
@NoArgsConstructor
public class StockMovementView {

    @Id
    private UUID movementId;
    private String movementType;
    private BigDecimal quantity;
    private BigDecimal unitValue;
    private BigDecimal totalValue;
    private LocalDateTime movementDate;
    private String propertyName;
    private String inputName;
    private String userName;
    private String batchNumber;
    private String notes;
    private Boolean reversed;
    private UUID reversedMovementId;
}