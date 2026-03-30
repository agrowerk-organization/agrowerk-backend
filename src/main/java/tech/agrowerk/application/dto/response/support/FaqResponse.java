package tech.agrowerk.application.dto.response.support;

import tech.agrowerk.infrastructure.model.support.enums.FaqCategory;

import java.util.UUID;

public record FaqResponse(
        UUID id,
        String question,
        String answer,
        FaqCategory faqCategory,
        Integer displayOrder,
        Integer viewCount,
        boolean isActive
) {
}
