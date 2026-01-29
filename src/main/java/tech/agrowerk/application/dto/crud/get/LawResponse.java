package tech.agrowerk.application.dto.crud.get;

import java.util.Map;

public record LawResponse(
        String slug,
        Map<String, String> metadata,
        String htmlContent
) {
}
