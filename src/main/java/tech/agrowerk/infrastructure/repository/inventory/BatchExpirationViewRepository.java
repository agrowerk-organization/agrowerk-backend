package tech.agrowerk.infrastructure.repository.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.inventory.views.BatchExpirationView;

import java.util.UUID;

@Repository
public interface BatchExpirationViewRepository extends JpaRepository<BatchExpirationView, UUID> {
}
