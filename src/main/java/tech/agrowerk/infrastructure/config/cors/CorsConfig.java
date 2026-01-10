package tech.agrowerk.infrastructure.config.cors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tech.agrowerk.infrastructure.exception.local.IllegalArgumentException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${security.cors.allowed-origins")
    private String[] allowedOrigins;

    @Value("${security.cors.allowed-methods:GET,POST,PUT,PATCH,DELETE,OPTIONS}")
    private String[] allowedMethods;

    @Value("${security.cors.allowed-headers:Authorization,Content-Type,X-Requested-With, Accept, Origin}")
    private String[] allowedHeaders;

    @Value("${security.cors.exposed-headers:Authorization, Set-Cookie}")
    private String[] exposedHeaders;

    @Value("${security.cors.allow-credentials:true}")
    private boolean allowCredentials;

    @Value("${security.cors.max-age:3600}")
    private long maxAge;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        List<String> originsList = Arrays.asList(allowedOrigins);
        if (allowCredentials && originsList.contains("*")) {
            throw new IllegalArgumentException("CORS: Allowed origins cannot be '*' when allowCredentials is true. Use explicit origins like 'http://localhost:4200'.");
        }

        configuration.setAllowedOrigins(originsList);
        configuration.setAllowedMethods(Arrays.asList(allowedMethods));
        configuration.setAllowedHeaders(Arrays.asList(allowedHeaders));
        configuration.setExposedHeaders(Arrays.asList(exposedHeaders));
        configuration.setAllowCredentials(allowCredentials);
        configuration.setMaxAge(maxAge);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}