package tech.agrowerk.infrastructure.repository.file;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.file.FileMetadata;
import tech.agrowerk.infrastructure.model.file.enums.FileCategory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, UUID> {

    Optional<FileMetadata> findByCloudinaryPublicIdAndDeletedFalse(String cloudinaryPublicId);

    List<FileMetadata> findByFileCategoryAndEntityIdAndDeletedFalse(FileCategory category, UUID entityId);

    List<FileMetadata> findByFileCategoryAndDeletedFalse(FileCategory category);

    @Query("SELECT f FROM FileMetadata f WHERE f.deleted = true AND f.deletedAt < :cutoffDate")
    List<FileMetadata> findDeletedFilesBefore(@Param("cutoffDate") LocalDateTime cutoffDate);

    long countByFileCategoryAndDeletedFalse(FileCategory category);

    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM FileMetadata f WHERE f.deleted = false")
    Long getTotalStorageUsed();

    List<FileMetadata> findByFileSizeGreaterThanAndDeletedFalse(Long minSize);
}
