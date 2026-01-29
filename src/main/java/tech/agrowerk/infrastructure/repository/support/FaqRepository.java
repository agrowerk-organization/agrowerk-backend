package tech.agrowerk.infrastructure.repository.support;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.support.Faq;

@Repository
public interface FaqRepository extends JpaRepository<Faq, Long> {
}
