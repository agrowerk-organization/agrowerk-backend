package tech.agrowerk.infrastructure.repository.inventory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.inventory.Input;

import java.util.List;
import java.util.UUID;

@Repository
public interface InputRepository extends JpaRepository<Input, UUID> {

    @Query("""
        SELECT i FROM Input i
        WHERE i.active = true
        AND (i.globalVisible = true OR i.supplier IS NOT NULL)
        ORDER BY i.name ASC
    """)
    Page<Input> findAllVisibleToProducer(Pageable pageable);

    Page<Input> findByCategory_IdAndActiveTrue(
            UUID categoryId, Pageable pageable);

    Page<Input> findByNameContainingIgnoreCaseAndActiveTrue(
            String name, Pageable pageable);

    Page<Input> findBySupplier_IdAndActiveTrue(
            UUID supplierId, Pageable pageable);

    List<Input> findByControlledTrueAndActiveTrue();

    boolean existsByInternalCodeAndIdNot(String code, UUID id);
    boolean existsByInternalCode(String internalCode);

    @Query("""
        SELECT COUNT(s) > 0 FROM Stock s
        WHERE s.input.id = :inputId
        AND s.currentQuantity > 0
    """)
    boolean hasActiveStock(@Param("inputId") UUID inputId);
}
