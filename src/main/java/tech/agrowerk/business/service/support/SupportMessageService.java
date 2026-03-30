package tech.agrowerk.business.service.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.application.dto.request.support.TicketMessageRequest;
import tech.agrowerk.application.dto.response.support.TicketMessageResponse;
import tech.agrowerk.business.mapper.support.SupportTicketMapper;
import tech.agrowerk.business.utils.AuthUtil;
import tech.agrowerk.business.utils.AuthenticatedUser;
import tech.agrowerk.business.validators.SupportAccessValidator;
import tech.agrowerk.infrastructure.exception.local.EntityNotFoundException;
import tech.agrowerk.infrastructure.model.core.User;
import tech.agrowerk.infrastructure.model.support.SupportMessage;
import tech.agrowerk.infrastructure.model.support.SupportTicket;
import tech.agrowerk.infrastructure.model.support.enums.SupportTicketStatus;
import tech.agrowerk.infrastructure.repository.core.UserRepository;
import tech.agrowerk.infrastructure.repository.support.SupportMessageRepository;
import tech.agrowerk.infrastructure.repository.support.SupportTicketRepository;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class SupportMessageService {

    private final SupportMessageRepository messageRepository;
    private final SupportTicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final SupportTicketMapper supportTicketMapper;
    private final SupportAccessValidator supportAccessValidator;
    private final AuthUtil authUtil;

    public SupportMessageService(SupportMessageRepository messageRepository,
                                 SupportTicketRepository ticketRepository,
                                 UserRepository userRepository,
                                 SupportTicketMapper supportTicketMapper,
                                 SupportAccessValidator supportAccessValidator,
                                 AuthUtil authUtil) {
        this.messageRepository = messageRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.supportTicketMapper = supportTicketMapper;
        this.supportAccessValidator = supportAccessValidator;
        this.authUtil = authUtil;
    }

    @Transactional(readOnly = true)
    public List<TicketMessageResponse> findMessages(UUID ticketId) {
        supportAccessValidator.validateTicketAccess(ticketId);
        return messageRepository.findByTicketIdOrderByCreatedAtAsc(ticketId)
                .stream()
                .map(supportTicketMapper::toMessageResponse)
                .toList();
    }

    @Transactional
    public TicketMessageResponse addMessage(UUID ticketId, TicketMessageRequest request) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        SupportTicket supportTicket = supportAccessValidator.validateTicketAccess(ticketId);
        supportAccessValidator.validateTicketOpen(supportTicket);

        User user = userRepository.findById(auth.id())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        SupportMessage supportMessage = new SupportMessage();
        supportMessage.setTicket(supportTicket);
        supportMessage.setUser(user);
        supportMessage.setMessage(request.message());
        supportMessage.setAttachments(request.attachments() != null ? request.attachments() : List.of());
        supportMessage.setInternal(false);

        if (supportTicket.getSupportTicketStatus() == SupportTicketStatus.OPEN) {
            supportTicket.setSupportTicketStatus(SupportTicketStatus.IN_PROGRESS);
            ticketRepository.save(supportTicket);
        }

        return supportTicketMapper.toMessageResponse(messageRepository.save(supportMessage));
    }
}