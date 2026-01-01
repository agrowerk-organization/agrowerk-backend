package tech.agrowerk.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.InputCategory;

@Repository
public interface InputCategoryRepository extends JpaRepository<InputCategory, Long> {
}
