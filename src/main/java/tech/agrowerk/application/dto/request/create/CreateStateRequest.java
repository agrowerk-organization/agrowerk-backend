package tech.agrowerk.application.dto.request.create;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateStateRequest(
        @NotNull
        @Size(max = 2)
        String code,

        @NotNull
        String name
) {
}
