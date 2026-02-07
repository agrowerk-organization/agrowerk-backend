package tech.agrowerk.infrastructure.config.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.cors.CorsConfigurationSource;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.SecurityContext;

import jakarta.annotation.PostConstruct;
import tech.agrowerk.infrastructure.security.filter.JwtBlacklistFilter;
import tech.agrowerk.infrastructure.security.filter.PermissionsPolicyFilter;
import tech.agrowerk.infrastructure.security.services.CookieService;
import tech.agrowerk.infrastructure.security.validator.JwtUserValidator;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    @Value("${security.rsa.public-key}")
    private RSAPublicKey publicKey;

    @Value("${security.rsa.private-key}")
    private RSAPrivateKey privateKey;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    private final CorsConfigurationSource corsConfigurationSource;
    private final JwtUserValidator jwtUserValidator;
    private final CookieService cookieService;

    public static final String[] PUBLIC_ENDPOINTS = {
            "/auth/login",
            "/users/**",
            "/auth/forgot-password",
            "/auth/reset-password",
            "/auth/verify-email",
            "/auth/oauth2/**",
            "/actuator/health",
            "/actuator/info",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/webjars/**",
            "/favicon.ico",
            "/error"
    };

    @PostConstruct
    public void validateKeys() {
        if (publicKey == null || privateKey == null) {
            log.error("RSA keys not properly configured!");
            throw new IllegalStateException("RSA keys must be configured");
        }
        log.info("RSA keys successfully loaded");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtBlacklistFilter jwtBlacklistFilter,
            PermissionsPolicyFilter permissionsPolicyFilter) throws Exception {

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .addFilterBefore(permissionsPolicyFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtBlacklistFilter, UsernamePasswordAuthenticationFilter.class)

                .headers(headers -> headers
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000)
                                .preload(true)
                        )
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives(
                                        "default-src 'self'; " +
                                                "script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net; " +
                                                "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net; " +
                                                "img-src 'self' data: https:; " +
                                                "font-src 'self' data: https://cdn.jsdelivr.net; " +
                                                "connect-src 'self' " + frontendUrl + "; " +
                                                "frame-ancestors 'none'; " +
                                                "base-uri 'self'; " +
                                                "form-action 'self'; " +
                                                "upgrade-insecure-requests"
                                )
                        )
                        .frameOptions(frameOptions -> frameOptions.deny())
                        .xssProtection(xss -> xss
                                .headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)
                        )
                        .contentTypeOptions(Customizer.withDefaults())
                        .referrerPolicy(referrer -> referrer
                                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                        )
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers("/actuator/prometheus", "/actuator/metrics/**")
                        .hasRole("SYSTEM_ADMIN")
                        .requestMatchers("/login/oauth2/code/**").permitAll()
                        .requestMatchers("/admin/**", "/api/admin/**")
                        .hasRole("SYSTEM_ADMIN")
                        .requestMatchers("/suppliers/**", "/api/suppliers/**")
                        .hasAnyRole("SYSTEM_ADMIN", "SUPPLIER_ADMIN")
                        .requestMatchers("/producers/**", "/api/producers/**")
                        .hasAnyRole("SYSTEM_ADMIN", "PRODUCER")
                        .anyRequest().authenticated()
                )

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                        .sessionFixation().none()
                )

                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder())
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                        .bearerTokenResolver(customBearerTokenResolver())
                        .authenticationEntryPoint(customAuthenticationEntryPoint())
                )

                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/auth/login")
                        .defaultSuccessUrl("/auth/oauth2/success", true)
                        .failureUrl("/auth/oauth2/failure")
                )

                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(customAuthenticationEntryPoint())
                        .accessDeniedHandler(customAccessDeniedHandler())
                )

                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                        })
                        .deleteCookies("accessToken", "refreshToken", "JSESSIONID")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                )

                .build();
    }

    @Bean
    public BearerTokenResolver customBearerTokenResolver() {
        return new BearerTokenResolver() {

            private final DefaultBearerTokenResolver defaultResolver = new DefaultBearerTokenResolver();

            {
                defaultResolver.setAllowUriQueryParameter(false);
                defaultResolver.setAllowFormEncodedBodyParameter(false);
            }

            @Override
            public String resolve(HttpServletRequest request) {
                String path = request.getRequestURI();

                if (isPublicEndpoint(path)) {
                    log.debug("Public endpoint accessed: {}", path);
                    return null;
                }

                String headerToken = defaultResolver.resolve(request);
                if (headerToken != null) {
                    log.debug("Token found in Authorization header for: {}", path);
                    return headerToken;
                }

                String cookieToken = cookieService.extractAccessToken(request);
                if (cookieToken != null) {
                    log.debug("Token found in cookie for: {}", path);
                    return cookieToken;
                }

                log.debug("No token found for: {}", path);
                return null;
            }

            private boolean isPublicEndpoint(String path) {
                return Arrays.stream(PUBLIC_ENDPOINTS)
                        .anyMatch(pattern -> {
                            String regex = pattern
                                    .replace("/**", ".*")
                                    .replace("/*", "/[^/]*");
                            return path.matches(regex);
                        });
            }
        };
    }

    @Bean
    public AuthenticationEntryPoint customAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            log.warn("Unauthorized access: {} - Path: {}",
                    authException.getMessage(), request.getRequestURI());

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            String errorResponse = String.format(
                    "{\"error\":\"Unauthorized\",\"message\":\"%s\",\"path\":\"%s\",\"timestamp\":\"%s\"}",
                    "Authentication required",
                    request.getRequestURI(),
                    Instant.now().toString()
            );

            response.getWriter().write(errorResponse);
        };
    }

    @Bean
    public AccessDeniedHandler customAccessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            log.warn("Access denied: {} - User: {} - Path: {}",
                    accessDeniedException.getMessage(),
                    request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "anonymous",
                    request.getRequestURI());

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            String errorResponse = String.format(
                    "{\"error\":\"Forbidden\",\"message\":\"%s\",\"path\":\"%s\",\"timestamp\":\"%s\"}",
                    "Access denied",
                    request.getRequestURI(),
                    Instant.now().toString()
            );

            response.getWriter().write(errorResponse);
        };
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        try {
            return NimbusJwtDecoder.withPublicKey(publicKey).build();
        } catch (Exception e) {
            log.error("Error creating JWT decoder", e);
            throw new IllegalStateException("Failed to create JWT decoder", e);
        }
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        try {
            RSAKey jwk = new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .build();
            ImmutableJWKSet<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
            return new NimbusJwtEncoder(jwks);
        } catch (Exception e) {
            log.error("Error creating JWT encoder", e);
            throw new IllegalStateException("Failed to create JWT encoder", e);
        }
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthorityPrefix("");
        authoritiesConverter.setAuthoritiesClaimName("role");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);

        return converter;
    }
}