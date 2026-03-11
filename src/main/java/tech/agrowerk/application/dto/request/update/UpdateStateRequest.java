package tech.agrowerk.application.dto.request.update;

import jakarta.validation.constraints.Size;

public record UpdateStateRequest(

        @Size(max = 2)
        String code,

        String name
) {
}
