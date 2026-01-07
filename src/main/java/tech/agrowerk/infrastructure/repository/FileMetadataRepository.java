package tech.agrowerk.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.FileMetadata;
import tech.agrowerk.infrastructure.enums.FileCategory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {

    Optional<FileMetadata> findByCloudinaryPublicIdAndDeletedFalse(String cloudinaryPublicId);

    List<FileMetadata> findByCategoryAndEntityIdAndDeletedFalse(FileCategory category, Long entityId);

    List<FileMetadata> findByCategoryAndDeletedFalse(FileCategory category);

    @Query("SELECT f FROM FileMetadata f WHERE f.deleted = true AND f.deletedAt < :cutoffDate")
    List<FileMetadata> findDeletedFilesBefore(@Param("cutoffDate") LocalDateTime cutoffDate);

    long countByCategoryAndDeletedFalse(FileCategory category);

    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM FileMetadata f WHERE f.deleted = false")
    Long getTotalStorageUsed();

    List<FileMetadata> findByFileSizeGreaterThanAndDeletedFalse(Long minSize);
}
