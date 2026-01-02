package tech.agrowerk.business.service.security;

import org.owasp.encoder.Encode;
import org.springframework.stereotype.Service;

@Service
public class InputSanitizer {
    public String sanitizeForHtml(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }

        return Encode.forHtml(input);
    }

    public String sanitizeForJavaScript(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }

        return Encode.forJavaScript(input);
    }

    public String sanitizeForCSS(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }

        return Encode.forCssString(input);
    }

    public String sanitizeForSQL(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }

        return input.replaceAll("[';\"\\\\]", "");
    }
}
