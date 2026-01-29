package tech.agrowerk.infrastructure.repository.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.inventory.Input;

import java.util.UUID;

@Repository
public interface InputRepository extends JpaRepository<Input, UUID> {
}
