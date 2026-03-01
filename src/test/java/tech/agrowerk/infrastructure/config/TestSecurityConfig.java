package tech.agrowerk.infrastructure.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import tech.agrowerk.infrastructure.security.filter.JwtBlacklistFilter;
import tech.agrowerk.infrastructure.security.services.CookieService;
import tech.agrowerk.infrastructure.security.services.TokenBlacklistService;
import tech.agrowerk.infrastructure.security.validator.JwtUserValidator;

@TestConfiguration
@EnableMethodSecurity
public class TestSecurityConfig {

    @Bean
    @Primary
    public JwtBlacklistFilter jwtBlacklistFilter(
            TokenBlacklistService tokenBlacklistService,
            JwtUserValidator jwtUserValidator,
            JwtDecoder jwtDecoder,
            CookieService cookieService) {

        return new JwtBlacklistFilter(tokenBlacklistService, jwtUserValidator, jwtDecoder, cookieService) {
            @Override
            protected boolean shouldNotFilter(HttpServletRequest request) {
                return true;
            }
        };
    }

    @Bean
    @Primary
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/login", "/auth/refresh", "/users/register").permitAll()
                        .requestMatchers("/weather/health").permitAll()
                        .requestMatchers("/users/get-user-by-email/**").hasAnyAuthority("SYSTEM_ADMIN", "SUPPLIER_ADMIN")
                        .requestMatchers("/users/get-user-by-id/**").hasAnyAuthority("SYSTEM_ADMIN", "SUPPLIER_ADMIN")
                        .requestMatchers("/users/list-users").hasAnyAuthority("SYSTEM_ADMIN", "SUPPLIER_ADMIN")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                );
        return http.build();
    }
}

