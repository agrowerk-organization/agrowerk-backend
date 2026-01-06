package tech.agrowerk.infrastructure.config.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.cors.CorsConfigurationSource;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.SecurityContext;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.agrowerk.business.filter.JwtBlacklistFilter;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Value("${security.rsa.public-key}")
    private RSAPublicKey publicKey;

    @Value("${security.rsa.private-key}")
    private RSAPrivateKey privateKey;

    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(CorsConfigurationSource corsConfigurationSource) {
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @PostConstruct
    public void validateKeys() {
        if (publicKey == null || privateKey == null) {
            logger.error("RSA keys not properly configured!");
            throw new IllegalStateException("RSA keys must be configured in application properties");
        }
        logger.info("RSA keys successfully loaded");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtBlacklistFilter jwtBlacklistFilter) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)

                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                .addFilterBefore(jwtBlacklistFilter,
                        UsernamePasswordAuthenticationFilter.class)

                .headers(headers -> headers
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000))
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; frame-ancestors 'none';"))
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                        .xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                        .contentTypeOptions(Customizer.withDefaults())
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-ui/index.html",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/favicon.ico"
                        ).permitAll()

                        .requestMatchers("/auth/login", "/users/register").permitAll()

                        .requestMatchers("/actuator/**", "/admin/**").hasRole("SYSTEM_ADMIN")

                        .requestMatchers("/suppliers/**").hasAnyRole("SYSTEM_ADMIN", "SUPPLIER_ADMIN")

                        .anyRequest().authenticated()
                )

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder())
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                        .bearerTokenResolver(customBearerTokenResolver())
                )

                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            logger.warn("Unauthorized access attempt: {}", authException.getMessage());
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write(
                                    "{\"error\":\"Unauthorized\",\"message\":\"Authentication required\"}"
                            );
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            logger.warn("Access denied: {}", accessDeniedException.getMessage());
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.getWriter().write(
                                    "{\"error\":\"Forbidden\",\"message\":\"Access denied\"}"
                            );
                        })
                )

                .build();
    }

    @Bean
    public BearerTokenResolver customBearerTokenResolver() {
        DefaultBearerTokenResolver defaultResolver = new DefaultBearerTokenResolver();
        defaultResolver.setAllowUriQueryParameter(false);

        return new BearerTokenResolver() {

            private final List<String> publicPatterns = List.of(
                    "/v3/api-docs",
                    "/swagger-ui",
                    "/swagger-ui.html",
                    "/swagger-resources",
                    "/webjars/",
                    "/favicon.ico",
                    "/actuator/health",
                    "/actuator/info",
                    "/auth/",
                    "/users/register"
            );

            @Override
            public String resolve(HttpServletRequest request) {
                String path = request.getRequestURI();
                String authHeader = request.getHeader("Authorization");

                boolean isPublicEndpoint = publicPatterns.stream()
                        .anyMatch(path::startsWith);

                if (isPublicEndpoint && authHeader == null) {
                    logger.debug("Public endpoint accessed without token: {}", path);
                    return null;
                }

                if (authHeader != null) {
                    logger.debug("Token present for path: {} - will validate", path);
                }

                return defaultResolver.resolve(request);
            }
        };
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        try {
            return NimbusJwtDecoder.withPublicKey(publicKey).build();
        } catch (Exception e) {
            logger.error("Error creating JWT decoder", e);
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
            logger.error("Error creating JWT encoder", e);
            throw new IllegalStateException("Failed to create JWT encoder", e);
        }
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        grantedAuthoritiesConverter.setAuthoritiesClaimName("role");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);

        return converter;
    }
}