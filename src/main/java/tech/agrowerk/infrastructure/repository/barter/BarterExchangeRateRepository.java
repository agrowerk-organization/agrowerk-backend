package tech.agrowerk.infrastructure.repository.barter;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.barter.BarterExchangeRate;

import java.util.List;
import java.util.UUID;

@Repository
public interface BarterExchangeRateRepository extends JpaRepository<BarterExchangeRate, UUID> {
    List<BarterExchangeRate> findByActiveTrue();
    List<BarterExchangeRate> findByCrop_IdAndActiveTrue(UUID cropId);
}
