package tech.agrowerk.application.dto.request.inventory;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import tech.agrowerk.infrastructure.model.farming.enums.ToxicologicalClass;
import tech.agrowerk.infrastructure.model.shared_enums.UnitOfMeasure;

import java.math.BigDecimal;

public record UpdateInputRequest(
        @Size(max = 150)
        String name,

        String description,

        UnitOfMeasure unitOfMeasure,

        @Size(max = 100)
        String activeIngredient,

        @Size(max = 100)
        String formulation,

        @Size(max = 50)
        String concentration,

        @Size(max = 50)
        String mapaRegistration,

        ToxicologicalClass toxicologicalClass,

        Integer gracePeriod,

        @Positive
        BigDecimal minimumStock,

        @Positive
        BigDecimal maximumStock
) {}