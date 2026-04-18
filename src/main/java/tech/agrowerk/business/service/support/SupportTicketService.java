package tech.agrowerk.business.service.support;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.application.dto.request.support.TicketRequest;
import tech.agrowerk.application.dto.response.support.TicketResponse;
import tech.agrowerk.business.mapper.support.SupportTicketMapper;
import tech.agrowerk.business.utils.AuthUtil;
import tech.agrowerk.business.utils.AuthenticatedUser;
import tech.agrowerk.business.validators.SupportAccessValidator;
import tech.agrowerk.infrastructure.exception.local.EntityNotFoundException;
import tech.agrowerk.infrastructure.model.core.User;
import tech.agrowerk.infrastructure.model.support.SupportTicket;
import tech.agrowerk.infrastructure.model.support.enums.SupportTicketStatus;
import tech.agrowerk.infrastructure.repository.core.UserRepository;
import tech.agrowerk.infrastructure.repository.support.SupportTicketRepository;

import java.util.UUID;

@Service
public class SupportTicketService {

    private final SupportTicketRepository supportTicketRepository;
    private final UserRepository userRepository;
    private final SupportTicketMapper supportTicketMapper;
    private final SupportAccessValidator supportAccessValidator;
    private final AuthUtil authUtil;

    public SupportTicketService(SupportTicketRepository supportTicketRepository,
                                UserRepository userRepository,
                                SupportTicketMapper supportTicketMapper,
                                SupportAccessValidator supportAccessValidator,
                                AuthUtil authUtil) {
        this.supportTicketRepository = supportTicketRepository;
        this.userRepository = userRepository;
        this.supportTicketMapper = supportTicketMapper;
        this.supportAccessValidator = supportAccessValidator;
        this.authUtil = authUtil;
    }

    @Transactional
    public TicketResponse createTicket(TicketRequest request) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        User user = userRepository.findById(auth.id())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        SupportTicket supportTicket = new SupportTicket();

        supportTicket.setUser(user);
        supportTicket.setSubject(request.subject());
        supportTicket.setDescription(request.description());
        supportTicket.setTicketCategory(request.ticketCategory());
        supportTicket.setTicketPriority(request.supportTicketPriority());
        supportTicket.setSupportTicketStatus(SupportTicketStatus.OPEN);

        return supportTicketMapper.toResponse(supportTicketRepository.save(supportTicket));
    }

    @Transactional(readOnly = true)
    public Page<TicketResponse> findMine(Pageable pageable) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

       return supportTicketRepository.findByUserIdOrderByCreatedAtDesc(auth.id(), pageable)
                .map(supportTicketMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<TicketResponse> findAll(SupportTicketStatus status, Pageable pageable) {
        return status != null
                ? supportTicketRepository.findBySupportTicketStatusOrderByCreatedAtDesc(status, pageable)
                .map(supportTicketMapper::toResponse)
                : supportTicketRepository.findAll(pageable)
                .map(supportTicketMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public TicketResponse findById(UUID ticketId) {
        return supportTicketMapper.toResponse(supportAccessValidator.validateTicketAccess(ticketId));
    }

    @Transactional
    public TicketResponse updateTicketStatus(UUID ticketId, SupportTicketStatus status) {
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found"));
        ticket.setSupportTicketStatus(status);
        return supportTicketMapper.toResponse(supportTicketRepository.save(ticket));
    }

    @Transactional
    public TicketResponse assignTicket(UUID ticketId, UUID adminUserId) {
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found"));
        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        ticket.setAssignedUser(admin);
        return supportTicketMapper.toResponse(supportTicketRepository.save(ticket));
    }
}
