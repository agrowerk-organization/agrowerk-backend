package tech.agrowerk.infrastructure.repository.barter;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.barter.CropCommitment;
import tech.agrowerk.infrastructure.model.barter.enums.CommitmentStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface CropCommitmentRepository extends JpaRepository<CropCommitment, UUID> {

    List<CropCommitment> findByTransaction_Id(UUID transactionId);

    List<CropCommitment> findByFarmer_Id(UUID farmerId);

    List<CropCommitment> findByTransaction_IdAndStatus(UUID transactionId, CommitmentStatus commitmentStatus);

    List<CropCommitment> findByExpectedDeliveryDateBeforeAndStatusNotIn(LocalDate date, List<CommitmentStatus> excludedStatuses);

    boolean existsByTransaction_IdAndStatusNotIn(UUID transactionId, List<CommitmentStatus> commitmentStatuses);

}
