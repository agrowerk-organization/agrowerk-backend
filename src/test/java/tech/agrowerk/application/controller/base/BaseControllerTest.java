package tech.agrowerk.application.controller.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import tech.agrowerk.infrastructure.config.TestSecurityConfig;
import tech.agrowerk.infrastructure.exception.global.AdvancedGlobalExceptionHandler;
import tech.agrowerk.infrastructure.security.services.CookieService;
import tech.agrowerk.infrastructure.security.services.RateLimitService;
import tech.agrowerk.infrastructure.security.services.TokenBlacklistService;
import tech.agrowerk.infrastructure.security.validator.JwtUserValidator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebMvcTest
@Import({TestSecurityConfig.class, AdvancedGlobalExceptionHandler.class})
public abstract class BaseControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected WebApplicationContext context;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockitoBean
    protected RateLimitService rateLimitService;

    @MockitoBean
    protected TokenBlacklistService tokenBlacklistService;

    @MockitoBean
    protected JwtUserValidator jwtUserValidator;

    @MockitoBean
    protected JwtDecoder jwtDecoder;

    @MockitoBean
    protected CookieService cookieService;

    @BeforeEach
    protected void setUpSecurity() {
        when(rateLimitService.isAllowedForPublicEndpoint(anyString())).thenReturn(true);
        when(rateLimitService.isAllowedByIp(anyString())).thenReturn(true);
        when(rateLimitService.isAllowedForSensitiveEndpoint(anyString(), anyString())).thenReturn(true);
        when(rateLimitService.isAllowedByUser(anyString())).thenReturn(true);
        when(jwtUserValidator.validate(any(), any())).thenReturn(null);
        when(cookieService.extractAccessToken(any())).thenReturn(null);
    }

    @BeforeEach
    protected abstract void setUp();
}