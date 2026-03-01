package tech.agrowerk.application.controller.law;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.agrowerk.application.dto.response.LawResponse;
import tech.agrowerk.business.service.law.LawService;


@RestController
@RequestMapping("/laws")
public class LawController implements LawApi {

    private final LawService lawService;

    public LawController(LawService lawService) {
        this.lawService = lawService;
    }

    @Override
    @GetMapping("/{slug}")
    public ResponseEntity<LawResponse> getLawContent(@PathVariable String slug) {
        LawResponse response = lawService.getLawContent(slug);
        return ResponseEntity.ok(response);
    }
}