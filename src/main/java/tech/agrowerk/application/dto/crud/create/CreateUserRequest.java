package tech.agrowerk.application.dto.crud.create;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.br.CPF;
import tech.agrowerk.business.validators.PasswordMatch;

import java.util.UUID;

@PasswordMatch
public record CreateUserRequest(

        @NotBlank(message = "Name is required")
        @Size(min = 3, max = 255, message = "Name must be between 3 and 255 characters")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
                message = "Password must contain at least one uppercase letter, one lowercase letter, one digit and one special character (@$!%*?&)"
        )
        String password,

        @NotBlank(message = "Password confirmation is required")
        String confirmPassword,

        @Pattern(
                regexp = "^\\(?\\d{2}\\)?\\s?\\d{4,5}-?\\d{4}$",
                message = "Invalid telephone format. Use: (99) 99999-9999 or (99) 9999-9999"
        )
        String telephone,

        @NotBlank(message = "CPF is required")
        @CPF(message = "Invalid CPF")
        String cpf,

        @NotNull(message = "Role is required")
        UUID roleId
) {}