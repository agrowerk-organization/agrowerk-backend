package tech.agrowerk.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.Input;

@Repository
public interface InputRepository extends JpaRepository<Input, Long> {
}
