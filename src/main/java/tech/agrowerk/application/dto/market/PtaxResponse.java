package tech.agrowerk.application.dto.market;

import java.util.List;

public record PtaxResponse(
        List<PtaxEntry> value
) {
}
