package tech.agrowerk.infrastructure.repository.farming;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.farming.Crop;
import tech.agrowerk.infrastructure.model.farming.enums.CropCategory;

import java.util.UUID;

@Repository
public interface CropRepository extends JpaRepository<Crop, UUID> {

    boolean existsByNameIgnoreCase(String name);

    Page<Crop> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Crop> findByCropCategory(CropCategory cropCategory, Pageable pageable);
}
