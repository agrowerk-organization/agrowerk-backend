package tech.agrowerk.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.Faq;

@Repository
public interface FaqRepository extends JpaRepository<Faq, Long> {
}
