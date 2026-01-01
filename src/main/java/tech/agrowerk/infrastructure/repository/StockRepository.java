package tech.agrowerk.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.Stock;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
}
