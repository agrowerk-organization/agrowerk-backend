package tech.agrowerk.application.dto.crud.update;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

public record UpdateUserRequest(
        @Size(min = 3, max = 255)
        String name,

        @Email
        @Size(max = 255)
        String email,

        @Pattern(regexp = "^\\(?\\d{2}\\)?\\s?\\d{4,5}-?\\d{4}$")
        String telephone,

        @CPF
        String cpf
) {
}
