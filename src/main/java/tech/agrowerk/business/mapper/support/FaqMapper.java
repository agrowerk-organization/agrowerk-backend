package tech.agrowerk.business.mapper.support;

import org.springframework.stereotype.Component;
import tech.agrowerk.application.dto.request.support.FaqRequest;
import tech.agrowerk.application.dto.response.support.FaqResponse;
import tech.agrowerk.infrastructure.model.support.Faq;

@Component
public class FaqMapper {

    public Faq toEntity(FaqRequest request) {
        Faq faq = new Faq();
        faq.setQuestion(request.question());
        faq.setAnswer(request.answer());
        faq.setFaqCategory(request.faqCategory());
        faq.setDisplayOrder(request.displayOrder());
        faq.setActive(true);
        faq.setViewCount(0);
        return faq;
    }

    public FaqResponse toResponse(Faq faq) {
        return new FaqResponse(
                faq.getId(),
                faq.getQuestion(),
                faq.getAnswer(),
                faq.getFaqCategory(),
                faq.getDisplayOrder(),
                faq.getViewCount(),
                faq.isActive()
        );
    }
}