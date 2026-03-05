package tech.agrowerk.infrastructure.repository.inventory;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.inventory.Stock;

import java.util.UUID;

@Repository
public interface StockRepository extends JpaRepository<Stock, UUID> {
    Stock findByPropertyIdAndInputId(UUID propertyId,  UUID inputId);
}
