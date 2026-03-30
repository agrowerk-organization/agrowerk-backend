package tech.agrowerk.application.dto.request.support;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import tech.agrowerk.infrastructure.model.support.enums.FaqCategory;

public record FaqRequest(
        @NotBlank
        String question,

        @NotBlank
        String answer,

        @NotNull
        FaqCategory faqCategory,

        Integer displayOrder
) {
}
