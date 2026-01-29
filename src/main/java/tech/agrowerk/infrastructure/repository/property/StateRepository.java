package tech.agrowerk.infrastructure.repository.property;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.property.State;

import java.util.UUID;

@Repository
public interface StateRepository extends JpaRepository<State, UUID> {
}
