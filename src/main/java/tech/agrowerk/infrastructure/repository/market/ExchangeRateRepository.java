package tech.agrowerk.infrastructure.repository.market;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.market.ExchangeRate;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, UUID> {
    boolean existsByCurrencyPairAndReferenceDate(String usdBrl, LocalDate date);

    Optional<ExchangeRate> findByCurrencyPairAndReferenceDate(String usdBrl, LocalDate targetDate);
}
