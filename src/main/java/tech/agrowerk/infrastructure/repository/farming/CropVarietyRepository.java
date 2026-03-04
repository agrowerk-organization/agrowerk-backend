package tech.agrowerk.infrastructure.repository.farming;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.farming.CropVariety;

import java.util.UUID;

@Repository
public interface CropVarietyRepository extends JpaRepository<CropVariety, UUID> {
    Page<CropVariety> findByCropId(UUID cropId, Pageable pageable);
    boolean existsByNameIgnoreCaseAndCropId(String name, UUID cropId);
    Page<CropVariety> findByCropIdAndNameContainingIgnoreCase(
            UUID cropId, String name, Pageable pageable);
}