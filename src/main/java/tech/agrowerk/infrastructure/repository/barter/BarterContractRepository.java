package tech.agrowerk.infrastructure.repository.barter;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.barter.BarterContract;
import tech.agrowerk.infrastructure.model.barter.enums.ContractStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BarterContractRepository extends JpaRepository<BarterContract, UUID> {
    Optional<BarterContract> findByTransaction_Id(UUID transactionId);
    List<BarterContract> findByContractStatus(ContractStatus status);
    boolean existsByContractNumber(String contractNumber);
}