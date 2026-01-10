package tech.agrowerk.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.SupportMessage;

@Repository
public interface SupportMessageRepository extends JpaRepository<SupportMessage, Long> {
}
