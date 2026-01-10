package tech.agrowerk.business.service;

import org.springframework.stereotype.Service;
import tech.agrowerk.infrastructure.repository.SupportTicketRepository;

@Service
public class SupportTicketService {

    private final SupportTicketRepository supportTicketRepository;

    public SupportTicketService(SupportTicketRepository supportTicketRepository) {
        this.supportTicketRepository = supportTicketRepository;
    }
}
