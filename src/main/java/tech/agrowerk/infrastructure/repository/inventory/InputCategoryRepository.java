package tech.agrowerk.infrastructure.repository.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.inventory.InputCategory;

import java.util.List;
import java.util.UUID;

@Repository
public interface InputCategoryRepository extends JpaRepository<InputCategory, UUID> {

    List<InputCategory> findByParentIsNullAndIsActiveTrue();

    List<InputCategory> findByParent_IdAndIsActiveTrue(UUID parentId);

    boolean existsByParent_IdAndIsActiveTrue(UUID parentId);

    boolean existsByIdAndInputs_ActiveTrue(UUID categoryId);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);

    @Query("""
        SELECT COUNT(c) FROM InputCategory c
        WHERE c.id = :id
    """)
    int countLevelById(@Param("id") UUID id);
}
