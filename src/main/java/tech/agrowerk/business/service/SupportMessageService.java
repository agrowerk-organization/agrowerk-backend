package tech.agrowerk.business.service;

import org.springframework.stereotype.Service;
import tech.agrowerk.infrastructure.repository.SupportMessageRepository;

@Service
public class SupportMessageService {

    private final SupportMessageRepository supportMessageRepository;

    public SupportMessageService(SupportMessageRepository supportMessageRepository) {
        this.supportMessageRepository = supportMessageRepository;
    }
}
