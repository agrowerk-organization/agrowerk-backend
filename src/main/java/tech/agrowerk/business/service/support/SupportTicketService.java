package tech.agrowerk.business.service.support;

import org.springframework.stereotype.Service;
import tech.agrowerk.infrastructure.repository.support.SupportTicketRepository;

@Service
public class SupportTicketService {

    private final SupportTicketRepository supportTicketRepository;

    public SupportTicketService(SupportTicketRepository supportTicketRepository) {
        this.supportTicketRepository = supportTicketRepository;
    }
}
