package tech.agrowerk.application.controller.support;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.agrowerk.application.dto.request.support.FaqRequest;
import tech.agrowerk.application.dto.response.support.FaqResponse;
import tech.agrowerk.business.service.support.FaqService;
import tech.agrowerk.infrastructure.model.support.enums.FaqCategory;

import java.util.UUID;

@RestController
@RequestMapping("/faqs")
public class FaqController {

    private final FaqService faqService;

    public FaqController(FaqService faqService) {
        this.faqService = faqService;
    }

    @GetMapping("list-active")
    public ResponseEntity<Page<FaqResponse>> list(
            @RequestParam(required = false) FaqCategory category,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(faqService.listActive(category, pageable));
    }

    @GetMapping("/get-one/{faqId}")
    public ResponseEntity<FaqResponse> getOne(@PathVariable UUID faqId) {
        return ResponseEntity.ok(faqService.getAndCount(faqId));
    }

    @PostMapping("/create-faq")
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
    public ResponseEntity<FaqResponse> createFaq(@Valid @RequestBody FaqRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(faqService.createFaq(request));
    }

    @PutMapping("/update-faq/{faqId}")
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
    public ResponseEntity<FaqResponse> updateFaq(@PathVariable UUID faqId, @Valid @RequestBody FaqRequest request) {
        return ResponseEntity.ok(faqService.updateFaq(faqId, request));
    }

    @DeleteMapping("/deactivate-faq/{faqId}")
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
    public ResponseEntity<Void> deactivate(@PathVariable UUID faqId) {
        faqService.deactivate(faqId);
        return ResponseEntity.noContent().build();
    }
}

