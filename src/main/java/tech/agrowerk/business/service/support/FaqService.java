package tech.agrowerk.business.service.support;

import org.springframework.stereotype.Service;
import tech.agrowerk.infrastructure.repository.support.FaqRepository;

@Service
public class FaqService {
    private final FaqRepository faqRepository;

    public FaqService(FaqRepository faqRepository) {
        this.faqRepository = faqRepository;
    }
}
