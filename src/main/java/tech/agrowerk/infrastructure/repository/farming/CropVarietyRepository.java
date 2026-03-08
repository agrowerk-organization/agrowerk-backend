package tech.agrowerk.infrastructure.repository.farming;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.farming.CropVariety;

import java.util.UUID;

@Repository
public interface CropVarietyRepository extends JpaRepository<CropVariety, UUID> {
    Page<CropVariety> findByCrop_Id(UUID cropId, Pageable pageable);

    boolean existsByNameIgnoreCaseAndCrop_Id(String name, UUID cropId);

    Page<CropVariety> findByCrop_IdAndNameContainingIgnoreCase(
            UUID cropId, String name, Pageable pageable);
}