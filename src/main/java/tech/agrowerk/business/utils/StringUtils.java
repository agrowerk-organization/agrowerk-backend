package tech.agrowerk.business.utils;

import org.springframework.stereotype.Component;

import java.text.Normalizer;

@Component
public class StringUtils {
    public static String normalizeCommodity(String value) {
        if (value == null) return null;
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replaceAll("[^a-zA-Z0-9]", "")
                .toUpperCase();
    }
}
