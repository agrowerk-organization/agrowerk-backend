package tech.agrowerk.application.controller.farming;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import tech.agrowerk.application.dto.request.farming.CreateAgriculturalPracticeRequest;
import tech.agrowerk.application.dto.response.farming.AgriculturalPracticeResponse;
import tech.agrowerk.infrastructure.model.farming.enums.PracticeType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Tag(name = "Agricultural Practices", description = "Management of farming activities like planting, fertilization, and harvesting")
public interface AgriculturalPracticeApi {

    @Operation(summary = "Register practice", description = "Records a new agricultural activity (Input, Labor, etc).")
    ResponseEntity<AgriculturalPracticeResponse> createPractice(@Valid @RequestBody CreateAgriculturalPracticeRequest request);

    @Operation(summary = "List by planting", description = "Retrieves all practices linked to a specific planting ID.")
    ResponseEntity<Page<AgriculturalPracticeResponse>> findByPlanting(@PathVariable UUID plantingId, Pageable pageable);

    @Operation(summary = "List by practice type", description = "Retrieves all practices linked to a practice type in a specific planting ID")
    ResponseEntity<AgriculturalPracticeResponse> findByType(@PathVariable UUID plantingId, PracticeType type, Pageable pageable);

    @Operation(summary = "Get total planting cost", description = "Calculates the sum of all costs for a specific planting.")
    ResponseEntity<BigDecimal> getTotalCost(@PathVariable UUID plantingId);

    @Operation(summary = "Get property cost by period", description = "Calculates total spending for a property within a date range.")
    ResponseEntity<BigDecimal> getCostByPeriod(@PathVariable UUID propertyId, @RequestParam LocalDate start, @RequestParam LocalDate end);
}
