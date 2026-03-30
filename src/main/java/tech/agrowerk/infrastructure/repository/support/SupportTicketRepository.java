package tech.agrowerk.infrastructure.repository.support;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.support.SupportTicket;
import tech.agrowerk.infrastructure.model.support.enums.SupportTicketStatus;

import java.util.UUID;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, UUID> {

    Page<SupportTicket> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Page<SupportTicket> findByStatusOrderByCreatedAtDesc(SupportTicketStatus supportTicketStatus, Pageable pageable);

}
