package tech.agrowerk.business.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import tech.agrowerk.application.dto.auth.ChangePassword;

public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, ChangePassword> {

    @Override
    public boolean isValid(ChangePassword changePassword, ConstraintValidatorContext context) {
        if (changePassword == null) {
            return true;
        }

        String newPassword = changePassword.newPassword();
        String confirmPassword = changePassword.confirmPassword();

        if (newPassword == null || confirmPassword == null) {
            return false;
        }

        return newPassword.equals(confirmPassword);
    }
}