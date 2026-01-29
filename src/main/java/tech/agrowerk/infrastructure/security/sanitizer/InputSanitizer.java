package tech.agrowerk.infrastructure.security.sanitizer;

import lombok.extern.slf4j.Slf4j;
import org.owasp.encoder.Encode;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
@Slf4j
public class InputSanitizer {

    private static final int MAX_INPUT_LENGTH = 10000;
    private static final Pattern SCRIPT_PATTERN = Pattern.compile("<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE);
    private static final Pattern HTML_PATTERN = Pattern.compile("<[^>]+>");


    public String sanitizeForHtml(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }

        input = truncate(input, MAX_INPUT_LENGTH);
        return Encode.forHtml(input);
    }


    public String sanitizeForJavaScript(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }

        input = truncate(input, MAX_INPUT_LENGTH);
        return Encode.forJavaScript(input);
    }

    public String stripHtml(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }

        input = truncate(input, MAX_INPUT_LENGTH);
        input = SCRIPT_PATTERN.matcher(input).replaceAll("");
        input = HTML_PATTERN.matcher(input).replaceAll("");

        return input.trim();
    }


    public String sanitizeUsername(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }

        String sanitized = input.replaceAll("[^a-zA-ZÀ-ÿ0-9\\s\\-_]", "");
        return truncate(sanitized.trim(), 255);
    }

    public String sanitizeEmail(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }

        String sanitized = input.trim().toLowerCase();

        if (!sanitized.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Invalid email format");
        }

        return truncate(sanitized, 255);
    }

    public String sanitizePhone(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }

        String sanitized = input.replaceAll("[^0-9]", "");

        if (sanitized.length() < 10 || sanitized.length() > 15) {
            throw new IllegalArgumentException("Invalid phone number length");
        }

        return sanitized;
    }

    public String sanitizeCpf(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }

        String sanitized = input.replaceAll("[^0-9]", "");

        if (sanitized.length() != 11) {
            throw new IllegalArgumentException("Invalid CPF length");
        }

        return sanitized;
    }


    public String sanitizePath(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }

        String sanitized = input.replaceAll("\\.\\.", "")
                .replaceAll("[/\\\\]", "")
                .trim();

        return truncate(sanitized, 255);
    }

    private String truncate(String input, int maxLength) {
        if (input == null) {
            return null;
        }
        return input.length() > maxLength ? input.substring(0, maxLength) : input;
    }

}