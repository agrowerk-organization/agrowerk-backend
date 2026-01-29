package tech.agrowerk.business.service.support;

import org.springframework.stereotype.Service;
import tech.agrowerk.infrastructure.repository.support.SupportMessageRepository;

@Service
public class SupportMessageService {

    private final SupportMessageRepository supportMessageRepository;

    public SupportMessageService(SupportMessageRepository supportMessageRepository) {
        this.supportMessageRepository = supportMessageRepository;
    }
}
