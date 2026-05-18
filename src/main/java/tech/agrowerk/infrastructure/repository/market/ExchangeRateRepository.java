package tech.agrowerk.infrastructure.repository.market;

import org.springframework.cglib.core.Local;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.market.ExchangeRate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, UUID> {
    boolean existsByCurrencyPairAndReferenceDate(String usdBrl, LocalDate date);

    Optional<ExchangeRate> findByCurrencyPairAndReferenceDate(String usdBrl, LocalDate targetDate);

    Optional<ExchangeRate> findTopByCurrencyPairAndReferenceDateBetweenOrderByReferenceDateDesc(
            String currencyPair, LocalDate start, LocalDate end
    );

    List<ExchangeRate> findByCurrencyPairAndReferenceDateBetweenOrderByReferenceDateAsc(String usdBrl, LocalDate start, LocalDate end);

    List<ExchangeRate> findAllByCurrencyPairAndReferenceDateBetween(String currencyPair, LocalDate start, LocalDate end);
}
