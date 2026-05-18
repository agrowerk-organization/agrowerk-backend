package tech.agrowerk.application.controller.farming;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.agrowerk.application.dto.views.FieldProductivityResponse;
import tech.agrowerk.business.service.farming.FieldProductivityViewService;

import java.util.UUID;

@RestController
@RequestMapping("/field-productivity")
public class FieldProductivityViewController {

    private final FieldProductivityViewService fieldProductivityViewService;

    public FieldProductivityViewController(FieldProductivityViewService fieldProductivityViewService) {
        this.fieldProductivityViewService = fieldProductivityViewService;
    }

    @GetMapping("/get-by-field/{fieldId}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<FieldProductivityResponse> getByField(@PathVariable UUID fieldId) {
        return fieldProductivityViewService.findFieldProductivityViewById(fieldId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}
