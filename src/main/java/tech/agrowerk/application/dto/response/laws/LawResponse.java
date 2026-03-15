package tech.agrowerk.application.dto.response.laws;

import java.util.Map;

public record LawResponse(
        String slug,
        Map<String, String> metadata,
        String htmlContent
) {
}
