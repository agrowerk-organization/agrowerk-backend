package tech.agrowerk.business.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Field;

public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, Object> {

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        if (obj == null) return true;

        if (obj instanceof PasswordConfirmable pc) {
            String password = pc.getPassword();
            String confirm = pc.getConfirmPassword();
            return password != null && password.equals(confirm);
        }

        try {
            String password = getFieldValue(obj, "newPassword");
            if (password == null) password = getFieldValue(obj, "password");

            String confirm = getFieldValue(obj, "confirmPassword");

            if (password == null || confirm == null) return false;
            return password.equals(confirm);
        } catch (Exception e) {
            return false;
        }
    }

    private String getFieldValue(Object obj, String fieldName) throws Exception {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return (String) field.get(obj);
        } catch (NoSuchFieldException e) {
            return null;
        }
    }
}